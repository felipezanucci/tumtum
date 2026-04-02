export default {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        'tumtum-red': '#C0392B',
        'tumtum-red-secondary': '#E74C3C',
        'tumtum-accent': '#00D2FF',
        'tumtum-dark': '#08080C',
        'tumtum-surface': '#111118',
        'tumtum-border': '#1A1A24',
        'tumtum-text-muted': '#6B6B80',
        'tumtum-text-primary': '#F0F0F5',
      },
    },
  },
  plugins: [],
}