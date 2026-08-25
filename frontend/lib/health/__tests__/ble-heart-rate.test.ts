import { describe, expect, it } from 'vitest'
import { parseHeartRateMeasurement } from '../ble-heart-rate'

/** Build a Heart Rate Measurement characteristic value, as a strap sends it. */
function frame(bytes: number[]): DataView {
  return new DataView(Uint8Array.from(bytes).buffer)
}

describe('parseHeartRateMeasurement', () => {
  it('reads an 8-bit rate with no optional fields', () => {
    // flags 0x00: uint8 format, no contact bits, no energy, no R-R
    expect(parseHeartRateMeasurement(frame([0x00, 72]))).toEqual({
      bpm: 72,
      rrIntervalsMs: [],
      contact: null,
    })
  })

  it('reads a 16-bit rate, little endian', () => {
    // flags 0x01: uint16 format. 0x00C8 = 200.
    expect(parseHeartRateMeasurement(frame([0x01, 0xc8, 0x00]))?.bpm).toBe(200)
  })

  it('reports contact only when the sensor says it supports it', () => {
    // 0x04 = supported, not detected; 0x06 = supported and detected
    expect(parseHeartRateMeasurement(frame([0x04, 70]))?.contact).toBe(false)
    expect(parseHeartRateMeasurement(frame([0x06, 70]))?.contact).toBe(true)
    // Unsupported must be null, not false: "no contact" and "cannot tell"
    // are different things to a quality score.
    expect(parseHeartRateMeasurement(frame([0x00, 70]))?.contact).toBeNull()
  })

  it('converts R-R intervals from 1/1024 s to milliseconds', () => {
    // flags 0x10: R-R present. 1024 units = 1000 ms; 512 = 500 ms.
    const view = frame([0x10, 60, 0x00, 0x04, 0x00, 0x02])
    expect(parseHeartRateMeasurement(view)?.rrIntervalsMs).toEqual([1000, 500])
  })

  it('skips the energy-expended field before reading R-R', () => {
    // flags 0x18: energy present (2 bytes) AND R-R present.
    const view = frame([0x18, 60, 0xff, 0x00, 0x00, 0x04])
    expect(parseHeartRateMeasurement(view)?.rrIntervalsMs).toEqual([1000])
  })

  it('rejects a physiologically impossible rate', () => {
    expect(parseHeartRateMeasurement(frame([0x00, 0]))).toBeNull()
    expect(parseHeartRateMeasurement(frame([0x01, 0xff, 0x00]))).toBeNull()
  })

  it('rejects a truncated frame instead of reading past the end', () => {
    expect(parseHeartRateMeasurement(frame([0x00]))).toBeNull()
  })
})
