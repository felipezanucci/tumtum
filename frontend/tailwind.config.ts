export default {
  content: [
    './pages/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        'tumtum-red': '#F82407',
        'tumtum-yellow': '#E7F502',
        'tumtum-ice': '#E6FCFF',
        'tumtum-indigo': '#261B8C',
        'tumtum-beige': '#E0DBC6',
        'tumtum-surface': '#F5F5F5',
        'tumtum-border': '#E5E5E5',
        'tumtum-text-muted': '#6B7280',
        'tumtum-text-primary': '#1A1A1A',
      },
    },
  },
  plugins: [],
}