import { describe, expect, it } from 'vitest'
import { parseHRFile } from '../import-parsers'

describe('parseHRFile', () => {
  it('reads a plain CSV', () => {
    const csv = 'time,bpm\n2026-08-26T10:00:00Z,72\n2026-08-26T10:00:05Z,75\n'
    const result = parseHRFile('export.csv', csv)
    expect(result.samples).toEqual([
      { time: '2026-08-26T10:00:00.000Z', bpm: 72 },
      { time: '2026-08-26T10:00:05.000Z', bpm: 75 },
    ])
  })

  it('reads accented Brazilian Portuguese headers', () => {
    // Regression: header matching compared raw strings, so the column a
    // Portuguese-locale export actually writes went unrecognised and the file
    // parsed to zero readings.
    const csv = 'Data e hora,Frequência cardíaca\n2026-08-26T10:00:00Z,88\n'
    expect(parseHRFile('exportação.csv', csv).samples).toEqual([
      { time: '2026-08-26T10:00:00.000Z', bpm: 88 },
    ])
  })

  it('reads a file saved with Windows line endings', () => {
    const csv = 'time,bpm\r\n2026-08-26T10:00:00Z,64\r\n2026-08-26T10:00:05Z,66\r\n'
    expect(parseHRFile('export.csv', csv).samples).toHaveLength(2)
  })

  it('does not mistake a namespaced start_time column for a heart rate', () => {
    // Samsung Health prefixes its columns; matching on the prefix rather than
    // the final segment would read the timestamp column as the BPM column.
    const csv =
      'com.samsung.health.heart_rate.start_time,com.samsung.health.heart_rate.heart_rate\n' +
      '2026-08-26T10:00:00Z,91\n'
    expect(parseHRFile('samsung.csv', csv).samples).toEqual([
      { time: '2026-08-26T10:00:00.000Z', bpm: 91 },
    ])
  })

  it('throws with the columns it saw, rather than parsing to a silent zero', () => {
    // Naming the headers it actually read is what turns "0 leituras" into
    // something the person holding the file can act on.
    expect(() => parseHRFile('random.csv', 'foo,bar\n1,2\n')).toThrowError(/foo, bar/)
  })
})
