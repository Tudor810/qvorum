// Turns a theme into CSS custom properties (--color-primary, --space-md...). Platform-neutral:
// the web app applies the result to the document; nothing here touches the DOM.
import { controlHeight, duration, fontFamily, fontSize, radius, spacing, type Theme } from './tokens'

const kebab = (name: string) => name.replace(/[A-Z]/g, (c) => `-${c.toLowerCase()}`)

export function cssVariables(theme: Theme): Record<string, string> {
  const vars: Record<string, string> = {}
  for (const [name, value] of Object.entries(theme.colors)) vars[`--color-${kebab(name)}`] = value
  for (const [tone, colors] of Object.entries(theme.tones)) {
    vars[`--tone-${tone}-bg`] = colors.bg
    vars[`--tone-${tone}-fg`] = colors.fg
    vars[`--tone-${tone}-border`] = colors.border
  }
  vars['--shadow-sm'] = theme.shadow.sm.css
  vars['--shadow-md'] = theme.shadow.md.css
  for (const [name, value] of Object.entries(spacing)) vars[`--space-${name}`] = `${value}px`
  for (const [name, value] of Object.entries(radius)) vars[`--radius-${name}`] = `${value}px`
  for (const [name, value] of Object.entries(fontSize)) vars[`--font-size-${name}`] = `${value}px`
  for (const [name, value] of Object.entries(controlHeight)) vars[`--control-${name}`] = `${value}px`
  for (const [name, value] of Object.entries(duration)) vars[`--duration-${name}`] = `${value}ms`
  vars['--font-sans'] = fontFamily.sans
  vars['--font-mono'] = fontFamily.mono
  return vars
}
