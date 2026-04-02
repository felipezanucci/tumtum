/**
 * Apple HealthKit integration via Web API.
 *
 * In Phase 0, HealthKit data is read on the device (iOS/watchOS) and sent to our backend.
 * This module provides the client-side interface for:
 * 1. Checking HealthKit availability
 * 2. Requesting authorization
 * 3. Reading HR data from the device
 * 4. Formatting data for upload to the backend
 *
 * Note: HealthKit is only available natively on iOS. In a PWA context, we rely on
 * a companion iOS app or Apple Health export. For MVP, users can manually export
 * their health data and upload it.
 */

export interface HealthKitHRSample {
  startDate: string // ISO 8601
  endDate: string
  value: number // BPM
  sourceName: string
}

/**
 * Check if the app is running in an environment that supports HealthKit.
 * In a PWA, this will typically be false unless using a native bridge.
 */
export function isHealthKitAvailable(): boolean {
  if (typeof window === 'undefined') return false
  // Check for native bridge (future: Capacitor/React Native bridge)
  return 'webkit' in window && 'messageHandlers' in (window as any).webkit
}

/**
 * Convert Apple HealthKit HR samples to our backend format.
 */
export function formatHealthKitData(
  samples: HealthKitHRSample[],
): Array<{ time: string; bpm: number; source: string }> {
  return samples
    .filter((s) => s.value >= 30 && s.value <= 250)
    .map((sample) => ({
      time: sample.startDate,
      bpm: Math.round(sample.value),
      source: 'apple_health',
    }))
}

/**
 * Parse HR data from an Apple Health XML export file.
 * Users can export their health data from the Health app on iOS.
 */
export function parseHealthKitExport(xmlContent: string): HealthKitHRSample[] {
  const parser = new DOMParser()
  const doc = parser.parseFromString(xmlContent, 'text/xml')
  const records = doc.querySelectorAll('Record[type="HKQuantityTypeIdentifierHeartRate"]')

  const samples: HealthKitHRSample[] = []
  records.forEach((record) => {
    const value = parseFloat(record.getAttribute('value') || '0')
    const startDate = record.getAttribute('startDate') || ''
    const endDate = record.getAttribute('endDate') || ''
    const sourceName = record.getAttribute('sourceName') || 'Apple Watch'

    if (value >= 30 && value <= 250 && startDate) {
      samples.push({ startDate, endDate, value, sourceName })
    }
  })

  return samples
}
