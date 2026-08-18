export default {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        // Core — see shared/brand/BRAND.md
        'tumtum-red': '#FF2E3C', // Batida
        'tumtum-red-hover': '#FF5561', // Batida, lifted — hover and active states on dark
        'tumtum-red-deep': '#B31220', // Batida on light or uncoated stock
        'tumtum-accent': '#00E5FF', // Palco — artist line only, never decorative
        'tumtum-dark': '#0A0A0F', // Preto Palco
        'tumtum-surface': '#15141C',
        'tumtum-raised': '#1F1D2A',
        'tumtum-border': '#2A2838',
        'tumtum-text-muted': '#8A87A0', // Névoa
        'tumtum-text-primary': '#F4F2F7', // Luz
        // Carnival — rotates by event, artist or campaign. One per surface.
        'tumtum-acido': '#D4FF3D',
        'tumtum-choque': '#FF3DBE',
        'tumtum-sol': '#FFCC00',
        'tumtum-uv': '#7A3DFF',
        'tumtum-campo': '#00E676',
      },
      fontFamily: {
        display: ['var(--font-display)', 'Arial Black', 'system-ui', 'sans-serif'],
        sans: ['var(--font-sans)', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['var(--font-mono)', 'ui-monospace', 'SFMono-Regular', 'monospace'],
      },
    },
  },
  plugins: [],
}
