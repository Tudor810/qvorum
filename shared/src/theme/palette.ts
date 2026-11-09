// Raw brand colors. Components should not use these directly: use the semantic colors of the
// theme (theme.colors.primary, theme.colors.text...), so light and dark mode both work.

export const palette = {
  teal: {
    50: '#F0FDFA',
    100: '#CCFBF1',
    200: '#99F6E4',
    300: '#5EEAD4',
    400: '#2DD4BF',
    500: '#14B8A6',
    600: '#0D9488',
    700: '#0F766E', // Qvorum Teal, the brand color
    800: '#115E59',
    900: '#134E4A',
    950: '#042F2E',
  },
  amber: {
    100: '#FEF3C7',
    300: '#FCD34D',
    400: '#FBBF24',
    700: '#B45309',
    800: '#92400E',
    900: '#78350F',
  },
  slate: {
    50: '#F8FAFC',
    100: '#F1F5F9',
    200: '#E2E8F0',
    300: '#CBD5E1',
    400: '#94A3B8',
    500: '#64748B',
    600: '#475569',
    700: '#334155',
    800: '#1E293B',
    850: '#111A2E',
    900: '#0F172A',
    950: '#0B1220',
  },
  green: { 100: '#DCFCE7', 400: '#4ADE80', 700: '#15803D', 800: '#166534', 950: '#052E16' },
  red: { 100: '#FEE2E2', 400: '#F87171', 700: '#B91C1C', 800: '#991B1B', 950: '#450A0A' },
  blue: { 100: '#DBEAFE', 400: '#60A5FA', 700: '#1D4ED8', 800: '#1E40AF', 950: '#172554' },
  white: '#FFFFFF',
} as const
