/**
 * Live heart rate over Web Bluetooth, using the standard Bluetooth SIG
 * Heart Rate Service (0x180D).
 *
 * This is Path 2, phase 1: it reaches live capture with no native code at all,
 * and works with anything that speaks the standard — chest straps (Polar H10),
 * and watches with a heart-rate broadcast mode.
 *
 * Platform reality: Web Bluetooth is available in Chrome on Android and on
 * desktop, and is NOT available in Safari on iOS. iPhone users fall back to the
 * file import at /import until the native watch apps land. The API also
 * requires a secure context (HTTPS or localhost).
 */

/** Bluetooth SIG assigned numbers. */
const HEART_RATE_SERVICE = 0x180d
const HEART_RATE_MEASUREMENT = 0x2a37
const BODY_SENSOR_LOCATION = 0x2a38

/** Physiological bounds — same as the backend's HRDataPointInput. */
const MIN_BPM = 30
const MAX_BPM = 250

/** The backend stores a single R-R per point and constrains it to this range. */
const MIN_RR_MS = 200
const MAX_RR_MS = 2000

export type BleState =
  | 'unsupported'
  | 'idle'
  | 'connecting'
  | 'connected'
  | 'reconnecting'
  | 'disconnected'
  | 'error'

export interface HRReading {
  /** ISO 8601, UTC. */
  time: string
  bpm: number
  /** All R-R intervals carried by this notification, in milliseconds. */
  rrIntervalsMs: number[]
  /** true/false when the sensor reports contact status, null when it does not. */
  contact: boolean | null
}

export interface MonitorHandlers {
  onReading: (reading: HRReading) => void
  onStateChange: (state: BleState, detail?: string) => void
}

/** Web Bluetooth needs a secure context and a browser that implements it. */
export function isWebBluetoothAvailable(): boolean {
  return (
    typeof navigator !== 'undefined' &&
    typeof navigator.bluetooth !== 'undefined' &&
    typeof window !== 'undefined' &&
    window.isSecureContext
  )
}

/**
 * Decode a Heart Rate Measurement (0x2A37) characteristic value.
 *
 * Layout: one flags byte, then the rate as uint8 or uint16 depending on flag
 * bit 0, then an optional uint16 of energy expended (bit 3), then zero or more
 * uint16 R-R intervals (bit 4) in units of 1/1024 s. All little endian.
 */
export function parseHeartRateMeasurement(view: DataView): Omit<HRReading, 'time'> | null {
  if (view.byteLength < 2) return null

  const flags = view.getUint8(0)
  const is16Bit = (flags & 0x01) !== 0
  const contactSupported = (flags & 0x04) !== 0
  const contactDetected = (flags & 0x02) !== 0
  const hasEnergy = (flags & 0x08) !== 0
  const hasRR = (flags & 0x10) !== 0

  let offset = 1
  let bpm: number
  if (is16Bit) {
    if (view.byteLength < offset + 2) return null
    bpm = view.getUint16(offset, true)
    offset += 2
  } else {
    bpm = view.getUint8(offset)
    offset += 1
  }

  if (hasEnergy) offset += 2

  const rrIntervalsMs: number[] = []
  if (hasRR) {
    while (offset + 1 < view.byteLength) {
      // Reported in 1/1024 of a second, not milliseconds.
      const rr = (view.getUint16(offset, true) * 1000) / 1024
      const rounded = Math.round(rr)
      if (rounded >= MIN_RR_MS && rounded <= MAX_RR_MS) rrIntervalsMs.push(rounded)
      offset += 2
    }
  }

  if (bpm < MIN_BPM || bpm > MAX_BPM) return null

  return {
    bpm,
    rrIntervalsMs,
    contact: contactSupported ? contactDetected : null,
  }
}

/**
 * A connection to one heart rate sensor, with reconnection.
 *
 * A BLE link will drop during a two-hour event — crowd, RF noise, the phone in
 * a pocket. Reconnecting is the normal case, not the exception, so a drop costs
 * samples rather than the session.
 */
export class HeartRateMonitor {
  private device: BluetoothDevice | null = null
  private characteristic: BluetoothRemoteGATTCharacteristic | null = null
  private state: BleState = 'idle'
  private reconnectAttempts = 0
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private stopped = false

  /** Give up after this many consecutive failures. */
  private static readonly MAX_RECONNECT_ATTEMPTS = 12

  constructor(private readonly handlers: MonitorHandlers) {}

  getState(): BleState {
    return this.state
  }

  getDeviceName(): string | null {
    return this.device?.name ?? null
  }

  private setState(state: BleState, detail?: string) {
    this.state = state
    this.handlers.onStateChange(state, detail)
  }

  /**
   * Ask the user to pick a sensor and start streaming.
   * Must be called from a user gesture — the browser requires one to show the
   * device chooser.
   */
  async connect(): Promise<void> {
    if (!isWebBluetoothAvailable()) {
      this.setState('unsupported')
      return
    }

    this.stopped = false
    this.setState('connecting')

    try {
      this.device = await navigator.bluetooth.requestDevice({
        filters: [{ services: [HEART_RATE_SERVICE] }],
        optionalServices: [HEART_RATE_SERVICE],
      })
      this.device.addEventListener('gattserverdisconnected', this.handleDisconnect)
      await this.openStream()
    } catch (error) {
      // The chooser being dismissed is a normal outcome, not a failure.
      const name = error instanceof DOMException ? error.name : ''
      if (name === 'NotFoundError') {
        this.setState('idle')
        return
      }
      this.setState('error', error instanceof Error ? error.message : String(error))
    }
  }

  /** Connect GATT, subscribe to notifications, and start delivering readings. */
  private async openStream(): Promise<void> {
    if (!this.device?.gatt) throw new Error('Dispositivo sem GATT disponível')

    const server = await this.device.gatt.connect()
    const service = await server.getPrimaryService(HEART_RATE_SERVICE)
    const characteristic = await service.getCharacteristic(HEART_RATE_MEASUREMENT)

    characteristic.addEventListener('characteristicvaluechanged', this.handleValue)
    await characteristic.startNotifications()

    this.characteristic = characteristic
    this.reconnectAttempts = 0
    this.setState('connected')
  }

  private handleValue = (event: Event) => {
    const target = event.target as BluetoothRemoteGATTCharacteristic
    const value = target.value
    if (!value) return

    const parsed = parseHeartRateMeasurement(value)
    if (!parsed) return

    this.handlers.onReading({ ...parsed, time: new Date().toISOString() })
  }

  private handleDisconnect = () => {
    this.characteristic = null
    if (this.stopped) {
      this.setState('disconnected')
      return
    }
    this.scheduleReconnect()
  }

  /**
   * Reconnect with backoff. No user gesture is needed here: the permission
   * granted for this device survives, so gatt.connect() can be called directly.
   */
  private scheduleReconnect(): void {
    if (this.stopped) return

    if (this.reconnectAttempts >= HeartRateMonitor.MAX_RECONNECT_ATTEMPTS) {
      this.setState('error', 'Não foi possível reconectar ao sensor.')
      return
    }

    const delay = Math.min(1000 * 2 ** this.reconnectAttempts, 15_000)
    this.reconnectAttempts += 1
    this.setState('reconnecting', `tentativa ${this.reconnectAttempts}`)

    this.reconnectTimer = setTimeout(() => {
      this.openStream().catch(() => this.scheduleReconnect())
    }, delay)
  }

  /** Stop streaming and release the device. */
  async disconnect(): Promise<void> {
    this.stopped = true
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }

    if (this.characteristic) {
      this.characteristic.removeEventListener('characteristicvaluechanged', this.handleValue)
      try {
        await this.characteristic.stopNotifications()
      } catch {
        // The link may already be gone; nothing to clean up in that case.
      }
      this.characteristic = null
    }

    if (this.device) {
      this.device.removeEventListener('gattserverdisconnected', this.handleDisconnect)
      if (this.device.gatt?.connected) this.device.gatt.disconnect()
    }

    this.setState('disconnected')
  }
}

/** Where on the body the sensor says it sits, when it reports it at all. */
export const SENSOR_LOCATIONS = [
  'Outro',
  'Peito',
  'Punho',
  'Dedo',
  'Mão',
  'Lóbulo da orelha',
  'Pé',
] as const

export { HEART_RATE_SERVICE, HEART_RATE_MEASUREMENT, BODY_SENSOR_LOCATION }
