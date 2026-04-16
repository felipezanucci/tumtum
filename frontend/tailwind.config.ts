import type { Config } from 'tailwindcss'
import plugin from 'tailwindcss/plugin'

const config: Config = {
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

        // Spotify Wrapped-inspired vibrant palette
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
      },

      fontFamily: {
        brand: ['Georgia', 'serif'],
        display: [
          'system-ui',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'sans-serif',
        ],
        body: [
          'system-ui',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'sans-serif',
        ],
      },

      fontSize: {
        'display-xl': ['5rem', { lineHeight: '0.9', letterSpacing: '-0.03em', fontWeight: '800' }],
        'display-lg': ['3.75rem', { lineHeight: '0.92', letterSpacing: '-0.02em', fontWeight: '800' }],
        'display-md': ['2.5rem', { lineHeight: '0.95', letterSpacing: '-0.02em', fontWeight: '700' }],
        'display-sm': ['1.875rem', { lineHeight: '1', letterSpacing: '-0.01em', fontWeight: '700' }],
        'stat-hero': ['8rem', { lineHeight: '0.85', letterSpacing: '-0.04em', fontWeight: '900' }],
        'stat-lg': ['5rem', { lineHeight: '0.9', letterSpacing: '-0.03em', fontWeight: '900' }],
        'stat-md': ['3rem', { lineHeight: '0.95', letterSpacing: '-0.02em', fontWeight: '800' }],
        'label-lg': ['0.875rem', { lineHeight: '1.2', letterSpacing: '0.1em', fontWeight: '700' }],
        'label-sm': ['0.75rem', { lineHeight: '1.2', letterSpacing: '0.12em', fontWeight: '600' }],
      },

      spacing: {
        '18': '4.5rem',
        '22': '5.5rem',
        '30': '7.5rem',
      },

      borderRadius: {
        '4xl': '2rem',
        '5xl': '2.5rem',
      },

      backgroundImage: {
        // Vibrant dual-tone gradients (Wrapped signature effect)
        'gradient-coral-magenta': 'linear-gradient(135deg, #FF6B4A 0%, #FF2D78 100%)',
        'gradient-magenta-purple': 'linear-gradient(135deg, #FF2D78 0%, #7B2FBE 100%)',
        'gradient-purple-blue': 'linear-gradient(135deg, #7B2FBE 0%, #3D5AFE 100%)',
        'gradient-blue-cyan': 'linear-gradient(135deg, #3D5AFE 0%, #00E5FF 100%)',
        'gradient-cyan-lime': 'linear-gradient(135deg, #00BFA5 0%, #76FF03 100%)',
        'gradient-lime-yellow': 'linear-gradient(135deg, #76FF03 0%, #FFD600 100%)',
        'gradient-orange-coral': 'linear-gradient(135deg, #FF6D00 0%, #FF6B4A 100%)',
        'gradient-red-orange': 'linear-gradient(135deg, #C0392B 0%, #FF6D00 100%)',

        // Multi-stop gradients (complex Wrapped backgrounds)
        'gradient-wrapped-warm': 'linear-gradient(135deg, #FF6B4A 0%, #FF2D78 40%, #7B2FBE 100%)',
        'gradient-wrapped-cool': 'linear-gradient(135deg, #7B2FBE 0%, #3D5AFE 40%, #00E5FF 100%)',
        'gradient-wrapped-neon': 'linear-gradient(135deg, #00E5FF 0%, #76FF03 40%, #FFD600 100%)',
        'gradient-wrapped-fire': 'linear-gradient(135deg, #FFD600 0%, #FF6D00 40%, #FF2D78 100%)',
        'gradient-wrapped-night': 'linear-gradient(135deg, #08080C 0%, #4A0E78 50%, #FF2D78 100%)',
        'gradient-wrapped-ocean': 'linear-gradient(135deg, #08080C 0%, #3D5AFE 50%, #00E5FF 100%)',

        // Radial gradients for glow effects
        'glow-red': 'radial-gradient(circle, rgba(192,57,43,0.4) 0%, transparent 70%)',
        'glow-magenta': 'radial-gradient(circle, rgba(255,45,120,0.4) 0%, transparent 70%)',
        'glow-cyan': 'radial-gradient(circle, rgba(0,229,255,0.3) 0%, transparent 70%)',
        'glow-purple': 'radial-gradient(circle, rgba(123,47,190,0.4) 0%, transparent 70%)',
      },

      boxShadow: {
        'glow-sm': '0 0 15px rgba(192, 57, 43, 0.3)',
        'glow-md': '0 0 30px rgba(192, 57, 43, 0.4)',
        'glow-lg': '0 0 60px rgba(192, 57, 43, 0.5)',
        'glow-cyan-sm': '0 0 15px rgba(0, 210, 255, 0.3)',
        'glow-cyan-md': '0 0 30px rgba(0, 210, 255, 0.4)',
        'glow-magenta-sm': '0 0 15px rgba(255, 45, 120, 0.3)',
        'glow-magenta-md': '0 0 30px rgba(255, 45, 120, 0.4)',
        'glow-purple-sm': '0 0 15px rgba(123, 47, 190, 0.3)',
        'glow-purple-md': '0 0 30px rgba(123, 47, 190, 0.4)',
        'card-elevated': '0 8px 32px rgba(0, 0, 0, 0.5)',
        'card-hover': '0 12px 48px rgba(0, 0, 0, 0.6)',
      },

      keyframes: {
        'gradient-shift': {
          '0%, 100%': { backgroundPosition: '0% 50%' },
          '50%': { backgroundPosition: '100% 50%' },
        },
        'pulse-glow': {
          '0%, 100%': { opacity: '0.6' },
          '50%': { opacity: '1' },
        },
        'float': {
          '0%, 100%': { transform: 'translateY(0px)' },
          '50%': { transform: 'translateY(-10px)' },
        },
        'scale-in': {
          '0%': { transform: 'scale(0.8)', opacity: '0' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
        'slide-up': {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        'slide-down': {
          '0%': { transform: 'translateY(-20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        },
        'rotate-slow': {
          '0%': { transform: 'rotate(0deg)' },
          '100%': { transform: 'rotate(360deg)' },
        },
        'morph': {
          '0%, 100%': { borderRadius: '60% 40% 30% 70% / 60% 30% 70% 40%' },
          '25%': { borderRadius: '30% 60% 70% 40% / 50% 60% 30% 60%' },
          '50%': { borderRadius: '50% 60% 30% 60% / 30% 60% 70% 40%' },
          '75%': { borderRadius: '60% 40% 60% 30% / 70% 40% 50% 60%' },
        },
        'heartbeat': {
          '0%, 100%': { transform: 'scale(1)' },
          '14%': { transform: 'scale(1.1)' },
          '28%': { transform: 'scale(1)' },
          '42%': { transform: 'scale(1.1)' },
          '70%': { transform: 'scale(1)' },
        },
        'count-pulse': {
          '0%': { transform: 'scale(1.15)', opacity: '0.7' },
          '100%': { transform: 'scale(1)', opacity: '1' },
        },
      },

      animation: {
        'gradient-shift': 'gradient-shift 6s ease infinite',
        'pulse-glow': 'pulse-glow 3s ease-in-out infinite',
        'float': 'float 4s ease-in-out infinite',
        'scale-in': 'scale-in 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
        'slide-up': 'slide-up 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
        'slide-down': 'slide-down 0.5s cubic-bezier(0.16, 1, 0.3, 1)',
        'rotate-slow': 'rotate-slow 20s linear infinite',
        'morph': 'morph 8s ease-in-out infinite',
        'heartbeat': 'heartbeat 1.2s ease-in-out infinite',
        'count-pulse': 'count-pulse 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
      },

      transitionTimingFunction: {
        'spring': 'cubic-bezier(0.16, 1, 0.3, 1)',
      },
    },
  },
  plugins: [
    plugin(function ({ addUtilities }) {
      addUtilities({
        '.text-gradient': {
          '-webkit-background-clip': 'text',
          '-webkit-text-fill-color': 'transparent',
          'background-clip': 'text',
        },
        '.bg-size-200': {
          'background-size': '200% 200%',
        },
        '.perspective-1000': {
          'perspective': '1000px',
        },
        '.backface-hidden': {
          'backface-visibility': 'hidden',
        },
      })
    }),
  ],
}

export default config
