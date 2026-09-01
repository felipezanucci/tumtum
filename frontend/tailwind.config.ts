export default {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        // Brand manual MVP v0.4: two neutrals and a proprietary pop duo.
        // Acid Lime #C6FF00 was the primary accent through v0.1 and does not
        // survive — anything still lime is out of date, not a variant.
        'tumtum-black': '#000000',
        'tumtum-white': '#FFFFFF',
        // Pink is 7.93:1 on black against Lime's 17.7:1 — it still passes AA
        // everywhere and AAA at large sizes, but it is roughly half as loud,
        // so emphasis comes from scale now rather than from the colour.
        // Never white on it: 2.65:1. Black on it is 7.93:1.
        'tumtum-pink': '#FF6F91', // primary accent
        'tumtum-yellow': '#EFFF00', // secondary accent

        // UI-only neutrals. The palette has no greys, so separation comes from
        // white at low alpha over the black canvas rather than from a new hue.
        'tumtum-surface': 'rgb(255 255 255 / 0.05)',
        'tumtum-border': 'rgb(255 255 255 / 0.14)',
        'tumtum-muted': 'rgb(255 255 255 / 0.60)',
        // Quieter than muted: legal lines, captions, and the labels on the
        // landing page's empty video slots.
        'tumtum-faint': 'rgb(255 255 255 / 0.34)',
      },
      keyframes: {
        // The landing page's video slots are empty until real footage exists.
        // A slow brightness swell keeps them reading as "a video goes here"
        // rather than as a broken image.
        breathe: {
          '0%, 100%': { filter: 'brightness(0.9)' },
          '50%': { filter: 'brightness(1.15)' },
        },
      },
      animation: {
        breathe: 'breathe 5s ease-in-out infinite',
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
