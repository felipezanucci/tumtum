export default {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        // Brand manual MVP v0.1: two neutrals and one acid pair. Nothing from
        // the previous red/cyan palette survives.
        'tumtum-black': '#000000',
        'tumtum-white': '#FFFFFF',
        'tumtum-lime': '#C6FF00', // primary accent
        'tumtum-yellow': '#EFFF00', // secondary accent

        // UI-only neutrals. The palette has no greys, so separation comes from
        // white at low alpha over the black canvas rather than from a new hue.
        'tumtum-surface': 'rgb(255 255 255 / 0.05)',
        'tumtum-border': 'rgb(255 255 255 / 0.14)',
        'tumtum-muted': 'rgb(255 255 255 / 0.60)',
      },
      fontFamily: {
        // Instrument Sans carries everything except the wordmark, which is
        // Chosmos and ships as an asset rather than a webfont.
        sans: ['var(--font-instrument-sans)', 'system-ui', 'sans-serif'],
      },
      fontWeight: {
        // Named for the roles the manual assigns them.
        body: '400',
        label: '500',
        headline: '600',
        hero: '700',
      },
    },
  },
  plugins: [],
}
