// Applies the shared theme to the page as CSS variables (--color-primary, --space-md...) and follows
// the system light / dark setting. Call applyTheme() once, before the first render.
import { cssVariables, themes, type Theme } from '@qvorum/shared'

export type ThemeMode = 'light' | 'dark'

const query = window.matchMedia('(prefers-color-scheme: dark)')

function apply(theme: Theme) {
  const root = document.documentElement
  for (const [name, value] of Object.entries(cssVariables(theme))) root.style.setProperty(name, value)
  root.dataset.theme = theme.mode
  root.style.colorScheme = theme.mode
}

/** Applies the system theme and keeps it in sync; pass a mode to force light or dark. */
export function applyTheme(mode?: ThemeMode) {
  if (mode) {
    apply(themes[mode])
    return
  }
  apply(query.matches ? themes.dark : themes.light)
  query.onchange = (event) => apply(event.matches ? themes.dark : themes.light)
}
