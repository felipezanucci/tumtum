'use client'

import { useEffect, useState } from 'react'

import {
  BIRTH_DAYS,
  MONTHS_PT,
  assembleBirthDate,
  birthYears,
  isImpossibleBirthDate,
} from '@/lib/birth-date'

interface BirthDatePickerProps {
  /** Called with `YYYY-MM-DD`, or `''` while incomplete or impossible. */
  onChange: (isoDate: string) => void
  label?: string
  /** The line under the lists while the date is not impossible. */
  hint?: string
  required?: boolean
}

/**
 * The birth date as three lists in pt-BR order — dia, mês, ano — and nothing
 * typed (28/09). The native date field drew mm/dd/yyyy on Felipe's machine.
 * Sign-up and /consentimento both render this one, so the two cannot drift.
 * Years run from 18 back (a hint; the server decides) to 120 back, and a date
 * that does not exist (31 de fevereiro) is said, not silently refused.
 */
export default function BirthDatePicker({
  onChange,
  label = 'Data de nascimento',
  hint = 'A TumTum é só para quem tem 18 anos ou mais.',
  required = false,
}: BirthDatePickerProps) {
  const [day, setDay] = useState('')
  const [month, setMonth] = useState('')
  const [year, setYear] = useState('')
  const [years] = useState(() => birthYears())
  const impossible = isImpossibleBirthDate(day, month, year)

  useEffect(() => {
    onChange(assembleBirthDate(day, month, year))
  }, [day, month, year, onChange])

  return (
    <fieldset>
      <legend className="mb-1.5 text-sm font-medium text-tumtum-white">{label}</legend>
      <div className="grid grid-cols-[1fr_2fr_1.4fr] gap-2">
        <select aria-label="Dia" value={day} onChange={(e) => setDay(e.target.value)} required={required} className={field}>
          <option value="">Dia</option>
          {BIRTH_DAYS.map((d) => (
            <option key={d} value={String(d)}>
              {d}
            </option>
          ))}
        </select>
        <select aria-label="Mês" value={month} onChange={(e) => setMonth(e.target.value)} required={required} className={field}>
          <option value="">Mês</option>
          {MONTHS_PT.map((m, i) => (
            <option key={m} value={String(i + 1)}>
              {m}
            </option>
          ))}
        </select>
        <select aria-label="Ano" value={year} onChange={(e) => setYear(e.target.value)} required={required} className={field}>
          <option value="">Ano</option>
          {years.map((y) => (
            <option key={y} value={String(y)}>
              {y}
            </option>
          ))}
        </select>
      </div>
      {impossible ? (
        <p role="alert" className="mt-1.5 text-xs text-red-500">
          Essa data não existe. Confere o dia e o mês.
        </p>
      ) : (
        hint && <p className="mt-1.5 text-xs text-tumtum-muted">{hint}</p>
      )}
    </fieldset>
  )
}

/** The same field every Input wears. */
const field =
  'w-full rounded-lg border border-tumtum-border bg-tumtum-surface px-3 py-2.5 text-tumtum-white transition-colors duration-150 hover:border-tumtum-muted focus:outline-none focus:ring-2 focus:ring-tumtum-pink focus:ring-offset-1 focus:ring-offset-tumtum-black'
