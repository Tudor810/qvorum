// Design tokens shared by the web and mobile apps. Sizes are numbers: px on the web,
// density-independent points on mobile. Text colors were checked for WCAG AA contrast (≥ 4.5:1).
import { palette } from './palette'

export const spacing = {
  none: 0,
  xxs: 2,
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
  xxxl: 48,
} as const

export const radius = {
  sm: 6,
  md: 10,
  lg: 14,
  xl: 20,
  full: 999,
} as const

export const fontSize = {
  xs: 12,
  sm: 14,
  md: 16,
  lg: 18,
  xl: 20,
  xxl: 24,
  display: 30,
} as const

// Strings, because React Native only accepts font weights as strings.
export const fontWeight = {
  regular: '400',
  medium: '500',
  semibold: '600',
  bold: '700',
} as const

/** Web font stack; on mobile the platform font is used. */
export const fontFamily = {
  sans: "'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
  mono: "ui-monospace, 'SF Mono', Menlo, Consolas, monospace",
} as const

/** Text styles: use these instead of setting sizes by hand. lineHeight is absolute. */
export const typography = {
  display: { fontSize: fontSize.display, lineHeight: 38, fontWeight: fontWeight.bold },
  title: { fontSize: fontSize.xxl, lineHeight: 32, fontWeight: fontWeight.bold },
  heading: { fontSize: fontSize.lg, lineHeight: 26, fontWeight: fontWeight.semibold },
  body: { fontSize: fontSize.md, lineHeight: 24, fontWeight: fontWeight.regular },
  bodyStrong: { fontSize: fontSize.md, lineHeight: 24, fontWeight: fontWeight.semibold },
  label: { fontSize: fontSize.sm, lineHeight: 20, fontWeight: fontWeight.medium },
  caption: { fontSize: fontSize.xs, lineHeight: 16, fontWeight: fontWeight.regular },
} as const

export type TypographyVariant = keyof typeof typography

/** Web layout breakpoints (min-width, px). */
export const breakpoints = {
  sm: 640,
  md: 768,
  lg: 1024,
  xl: 1280,
} as const

/** Animation durations, ms. */
export const duration = {
  fast: 120,
  normal: 200,
  slow: 320,
} as const

/** Component sizes, so buttons and inputs line up on both platforms. */
export const controlHeight = {
  sm: 32,
  md: 44, // also the minimum touch target on mobile
  lg: 52,
} as const

/**
 * Tones color the status badges and messages. Each tone has a background, a text color and a
 * border, so a badge is readable in both light and dark mode.
 */
export type Tone = 'neutral' | 'brand' | 'success' | 'warning' | 'danger' | 'info'
export interface ToneColors {
  bg: string
  fg: string
  border: string
}

export interface ThemeColors {
  background: string
  surface: string
  surfaceMuted: string
  border: string
  borderStrong: string
  text: string
  textMuted: string
  textSubtle: string
  primary: string
  primaryHover: string
  primaryPressed: string
  onPrimary: string
  accent: string
  onAccent: string
  focus: string
  overlay: string
  success: string
  warning: string
  danger: string
  info: string
}

export interface Shadow {
  /** CSS value for the web. */
  css: string
  /** React Native shadow props (iOS) and elevation (Android). */
  native: {
    shadowColor: string
    shadowOpacity: number
    shadowRadius: number
    shadowOffset: { width: number; height: number }
    elevation: number
  }
}

export interface Theme {
  mode: 'light' | 'dark'
  colors: ThemeColors
  tones: Record<Tone, ToneColors>
  shadow: { sm: Shadow; md: Shadow }
}

export const lightTheme: Theme = {
  mode: 'light',
  colors: {
    background: palette.slate[50],
    surface: palette.white,
    surfaceMuted: palette.slate[100],
    border: palette.slate[200],
    borderStrong: palette.slate[300],
    text: palette.slate[900],
    textMuted: palette.slate[600],
    textSubtle: palette.slate[500],
    primary: palette.teal[700],
    primaryHover: palette.teal[800],
    primaryPressed: palette.teal[900],
    onPrimary: palette.white,
    accent: palette.amber[300],
    onAccent: palette.amber[900],
    focus: palette.teal[500],
    overlay: 'rgba(15, 23, 42, 0.45)',
    success: palette.green[700],
    warning: palette.amber[700],
    danger: palette.red[700],
    info: palette.blue[700],
  },
  tones: {
    neutral: { bg: palette.slate[200], fg: palette.slate[700], border: palette.slate[300] },
    brand: { bg: palette.teal[100], fg: palette.teal[800], border: palette.teal[200] },
    success: { bg: palette.green[100], fg: palette.green[800], border: '#BBF7D0' },
    warning: { bg: palette.amber[100], fg: palette.amber[800], border: '#FDE68A' },
    danger: { bg: palette.red[100], fg: palette.red[800], border: '#FECACA' },
    info: { bg: palette.blue[100], fg: palette.blue[800], border: '#BFDBFE' },
  },
  shadow: {
    sm: {
      css: '0 1px 2px rgba(15, 23, 42, 0.06), 0 1px 3px rgba(15, 23, 42, 0.08)',
      native: { shadowColor: '#0F172A', shadowOpacity: 0.08, shadowRadius: 3, shadowOffset: { width: 0, height: 1 }, elevation: 1 },
    },
    md: {
      css: '0 4px 12px rgba(15, 23, 42, 0.08), 0 2px 4px rgba(15, 23, 42, 0.06)',
      native: { shadowColor: '#0F172A', shadowOpacity: 0.1, shadowRadius: 10, shadowOffset: { width: 0, height: 4 }, elevation: 4 },
    },
  },
}

export const darkTheme: Theme = {
  mode: 'dark',
  colors: {
    background: palette.slate[950],
    surface: palette.slate[850],
    surfaceMuted: palette.slate[800],
    border: palette.slate[800],
    borderStrong: palette.slate[700],
    text: palette.slate[100],
    textMuted: palette.slate[400],
    textSubtle: palette.slate[500],
    primary: palette.teal[400],
    primaryHover: palette.teal[300],
    primaryPressed: palette.teal[200],
    onPrimary: palette.teal[950],
    accent: palette.amber[400],
    onAccent: palette.amber[900],
    focus: palette.teal[300],
    overlay: 'rgba(2, 6, 23, 0.7)',
    success: palette.green[400],
    warning: palette.amber[400],
    danger: palette.red[400],
    info: palette.blue[400],
  },
  tones: {
    neutral: { bg: palette.slate[800], fg: palette.slate[200], border: palette.slate[700] },
    brand: { bg: palette.teal[950], fg: palette.teal[300], border: palette.teal[800] },
    success: { bg: palette.green[950], fg: palette.green[400], border: palette.green[800] },
    warning: { bg: '#451A03', fg: palette.amber[400], border: palette.amber[800] },
    danger: { bg: palette.red[950], fg: palette.red[400], border: palette.red[800] },
    info: { bg: palette.blue[950], fg: palette.blue[400], border: palette.blue[800] },
  },
  shadow: {
    sm: {
      css: '0 1px 2px rgba(0, 0, 0, 0.4)',
      native: { shadowColor: '#000000', shadowOpacity: 0.4, shadowRadius: 2, shadowOffset: { width: 0, height: 1 }, elevation: 1 },
    },
    md: {
      css: '0 6px 16px rgba(0, 0, 0, 0.45)',
      native: { shadowColor: '#000000', shadowOpacity: 0.45, shadowRadius: 12, shadowOffset: { width: 0, height: 6 }, elevation: 6 },
    },
  },
}

export const themes = { light: lightTheme, dark: darkTheme } as const
