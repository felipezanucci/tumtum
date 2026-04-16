export const colors = {
  brand: {
    red: '#C0392B',
    redSecondary: '#E74C3C',
    accent: '#00D2FF',
  },

  wrapped: {
    coral: '#FF6B4A',
    salmon: '#FF8566',
    magenta: '#FF2D78',
    hotpink: '#FF1493',
    purple: '#7B2FBE',
    deepviolet: '#4A0E78',
    electric: '#5B3FFF',
    blue: '#3D5AFE',
    teal: '#00BFA5',
    cyan: '#00E5FF',
    lime: '#76FF03',
    green: '#1DB954',
    yellow: '#FFD600',
    orange: '#FF6D00',
    peach: '#FFAB91',
    cream: '#FFF3E0',
  },

  surface: {
    base: '#08080C',
    elevated: '#111118',
    border: '#1A1A24',
    overlay: '#222230',
    raised: '#2A2A3C',
  },

  text: {
    primary: '#F0F0F5',
    secondary: '#B0B0C0',
    muted: '#6B6B80',
    disabled: '#44445A',
  },
} as const

export const gradients = {
  coralMagenta: ['#FF6B4A', '#FF2D78'],
  magentaPurple: ['#FF2D78', '#7B2FBE'],
  purpleBlue: ['#7B2FBE', '#3D5AFE'],
  blueCyan: ['#3D5AFE', '#00E5FF'],
  cyanLime: ['#00BFA5', '#76FF03'],
  limeYellow: ['#76FF03', '#FFD600'],
  orangeCoral: ['#FF6D00', '#FF6B4A'],
  redOrange: ['#C0392B', '#FF6D00'],

  wrappedWarm: ['#FF6B4A', '#FF2D78', '#7B2FBE'],
  wrappedCool: ['#7B2FBE', '#3D5AFE', '#00E5FF'],
  wrappedNeon: ['#00E5FF', '#76FF03', '#FFD600'],
  wrappedFire: ['#FFD600', '#FF6D00', '#FF2D78'],
  wrappedNight: ['#08080C', '#4A0E78', '#FF2D78'],
  wrappedOcean: ['#08080C', '#3D5AFE', '#00E5FF'],
  wrappedSunset: ['#FF6B4A', '#FF2D78', '#7B2FBE', '#3D5AFE'],
  wrappedAurora: ['#4A0E78', '#00BFA5', '#76FF03', '#FFD600'],
} as const

export type GradientName = keyof typeof gradients

export const gradientPairs = [
  { name: 'warm', colors: gradients.wrappedWarm, css: 'bg-wrapped-warm' },
  { name: 'cool', colors: gradients.wrappedCool, css: 'bg-wrapped-cool' },
  { name: 'neon', colors: gradients.wrappedNeon, css: 'bg-wrapped-neon' },
  { name: 'fire', colors: gradients.wrappedFire, css: 'bg-wrapped-fire' },
  { name: 'night', colors: gradients.wrappedNight, css: 'bg-wrapped-night' },
  { name: 'ocean', colors: gradients.wrappedOcean, css: 'bg-wrapped-ocean' },
  { name: 'sunset', colors: gradients.wrappedSunset, css: 'bg-wrapped-sunset' },
  { name: 'aurora', colors: gradients.wrappedAurora, css: 'bg-wrapped-aurora' },
] as const

export const typography = {
  display: {
    xl: 'text-display-xl font-display',
    lg: 'text-display-lg font-display',
    md: 'text-display-md font-display',
    sm: 'text-display-sm font-display',
  },
  stat: {
    hero: 'text-stat-hero font-display',
    lg: 'text-stat-lg font-display',
    md: 'text-stat-md font-display',
  },
  label: {
    lg: 'text-label-lg uppercase font-display',
    sm: 'text-label-sm uppercase font-display',
  },
  brand: 'font-brand uppercase tracking-[3px]',
} as const

export const shapes = {
  blob: 'shape-blob',
  blobAlt: 'shape-blob-alt',
  spike: 'shape-spike',
  diamond: 'shape-diamond',
  hexagon: 'shape-hexagon',
  arch: 'shape-arch',
  squircle: 'shape-squircle',
} as const

export const elevation = {
  card: 'bg-tumtum-surface border border-tumtum-border rounded-2xl',
  cardHover: 'bg-tumtum-surface border border-tumtum-border rounded-2xl hover:border-wrapped-magenta/30 transition-colors',
  modal: 'bg-tumtum-surface border border-tumtum-border rounded-3xl shadow-card-elevated',
  overlay: 'bg-[#222230] border border-tumtum-border rounded-xl',
} as const

export const motion = {
  spring: 'transition-all duration-300 ease-spring',
  fast: 'transition-all duration-150 ease-out',
  medium: 'transition-all duration-300 ease-out',
  slow: 'transition-all duration-500 ease-out',
  enter: 'animate-scale-in',
  slideUp: 'animate-slide-up',
  slideDown: 'animate-slide-down',
  float: 'animate-float',
  heartbeat: 'animate-heartbeat',
  glow: 'animate-pulse-glow',
} as const
