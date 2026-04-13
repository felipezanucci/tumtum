export default {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        'tumtum-red': '#FE2401',
        'tumtum-red-deep': '#DB123C',
        'tumtum-burgundy': '#80011F',
        'tumtum-red-dark': '#8B0100',
        'tumtum-lime': '#C6F908',
        'tumtum-surface': '#F5F5F5',
        'tumtum-border': '#E5E5E5',
        'tumtum-text-muted': '#6B7280',
        'tumtum-text-primary': '#1A1A1A',
      },
    },
  },
  plugins: [],
}