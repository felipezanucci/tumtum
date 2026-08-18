/**
 * TumTum logo. See shared/brand/BRAND.md.
 *
 * The mark is a container: the Bloco geometry never changes, the fill always can.
 * Pass any SVG paint to `fill` — a hex, or a `url(#id)` referencing a gradient or
 * pattern you defined elsewhere on the page (the user's own HR curve, a crowd photo,
 * a texture). That is the whole variation system.
 */

const BLOCO =
  'M0 0H190V58H0ZM66 0H124V260H66ZM210 0H268V260H210ZM342 0H400V260H342ZM210 202H400V260H210Z' +
  'M420 0H478V260H420ZM602 0H660V260H602ZM478 0L540 166L602 0V58L540 224L478 58Z'

const RISCO = [
  'M14 -78C10 -50 7 -24 9 -9 10 1 19 5 31 -2',
  'M-6 -44L32 -52',
  'M42 -47C38 -25 37 -11 39 -4 42 3 57 5 64 -6 67 -13 69 -30 71 -47',
  'M84 1C87 -13 88 -27 90 -41 94 -51 106 -51 109 -41 110 -29 109 -15 108 -1',
  'M109 -41C114 -51 127 -51 130 -41 131 -27 130 -13 129 1',
]

type Variant = 'primary' | 'icon' | 'horizontal' | 'seal'

interface LogoProps {
  variant?: Variant
  /** Any SVG paint: a hex, or `url(#id)` pointing at a gradient or pattern. */
  fill?: string
  /** Colour of the hand-drawn second beat. */
  risco?: string
  /** Knockout behind the Risco. Keep it matched to the surface underneath. */
  knockout?: string
  className?: string
  title?: string
}

function Risco({
  transform,
  color,
  knockout,
}: {
  transform: string
  color: string
  knockout?: string
}) {
  return (
    <g transform={transform} fill="none" strokeLinecap="round" strokeLinejoin="round">
      {knockout && (
        <g stroke={knockout} strokeWidth={20}>
          {RISCO.map((d) => (
            <path key={d} d={d} />
          ))}
        </g>
      )}
      <g stroke={color} strokeWidth={11}>
        {RISCO.map((d) => (
          <path key={d} d={d} />
        ))}
      </g>
    </g>
  )
}

export default function Logo({
  variant = 'primary',
  fill = '#FF2E3C',
  risco = '#F4F2F7',
  knockout = '#0A0A0F',
  className = '',
  title = 'TumTum',
}: LogoProps) {
  const shared = { role: 'img' as const, className, 'aria-label': title }

  if (variant === 'icon') {
    return (
      <svg viewBox="0 0 260 260" {...shared}>
        <rect width="260" height="260" rx="56" fill={fill} />
        <Risco transform="translate(38 182) rotate(-4) scale(1.34)" color={risco} />
      </svg>
    )
  }

  if (variant === 'horizontal') {
    return (
      <svg viewBox="0 0 780 260" {...shared}>
        <g transform="translate(0 26) scale(.7)">
          <path d={BLOCO} fill={fill} />
        </g>
        <Risco
          transform="translate(500 208) rotate(-5) scale(1.22)"
          color={risco}
          knockout={knockout}
        />
      </svg>
    )
  }

  if (variant === 'seal') {
    return (
      <svg viewBox="0 0 260 260" {...shared}>
        <circle cx="130" cy="130" r="126" fill="none" stroke={fill} strokeWidth={7} />
        <Risco transform="translate(48 168) rotate(-4) scale(1.05)" color={risco} />
      </svg>
    )
  }

  return (
    <svg viewBox="0 0 660 372" {...shared}>
      <path d={BLOCO} fill={fill} />
      <Risco
        transform="translate(408 337) rotate(-5) scale(1.5)"
        color={risco}
        knockout={knockout}
      />
    </svg>
  )
}
