/**
 * CSV a spreadsheet will open without mangling.
 *
 * The rule is RFC 4180's: a field containing a comma, a quote or a newline is
 * wrapped in quotes, and quotes inside it are doubled. Skipping this does not
 * fail loudly — it shifts one row's columns and looks like a data problem
 * days later.
 */
export function toCsv(headers: string[], rows: string[][]): string {
  const escape = (value: string) =>
    /[",\n\r]/.test(value) ? `"${value.replace(/"/g, '""')}"` : value
  return [headers, ...rows].map((row) => row.map(escape).join(',')).join('\n')
}
