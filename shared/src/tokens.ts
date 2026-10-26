// Provisional values until the visual direction is settled.
// Numbers are px on the web and density-independent units on mobile.

export const colors = {
  primary: '#1F5FAD',
  primaryText: '#FFFFFF',
  background: '#F7F8FA',
  surface: '#FFFFFF',
  border: '#E2E5EA',
  text: '#1A1D23',
  textMuted: '#5F6673',
  success: '#1E8E5A',
  warning: '#B7791F',
  danger: '#C53030',
} as const

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
} as const

export const radius = {
  sm: 4,
  md: 8,
  lg: 12,
  full: 999,
} as const

export const fontSize = {
  sm: 13,
  md: 15,
  lg: 18,
  xl: 22,
  xxl: 28,
} as const
