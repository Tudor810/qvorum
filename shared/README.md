# @qvorum/shared

TypeScript code shared by the web and mobile clients. Both apps import it directly from source:

```ts
import { createApiClient, formatMoney, strings } from '@qvorum/shared'
```

There is no build step: Vite and Metro compile the `.ts` files as part of each app.

## Contents

| Path | Contents |
| --- | --- |
| `src/api/client.ts` | `createApiClient`: HTTP client that adds the auth token, sends and parses JSON, and throws `ApiError` on failure |
| `src/api/*.ts` | endpoint functions, one file per feature (e.g. `auth.ts`) |
| `src/types/` | domain entities and API request/response types |
| `src/constants.ts` | user roles and other fixed values |
| `src/format.ts` | formatting for money (RON) and dates |
| `src/theme/` | brand palette, light and dark themes, spacing, radius, typography, status tones, CSS variables for the web |
| `src/strings.ts` | user interface texts |

## Rules

- **No React and no platform-specific code.** Nothing that only exists in the browser (`window`, `localStorage`, `import.meta.env`) or only in React Native. Each app passes those in, for example the base URL and the token getter given to `createApiClient`. Keeping React out of this package also prevents duplicate React copies across the two apps.
- **No TypeScript `enum`.** Use `as const` objects with a derived union type, as in `constants.ts`.
- **Money is stored in bani** (1 RON = 100 bani) as integers, to avoid floating-point rounding errors.

## Theme

The brand color is **Qvorum Teal** (`#0F766E`), with an amber accent (`#FCD34D`) for highlights and slate grays as neutrals. Red, green, amber and blue are kept for statuses only. All text colors meet WCAG AA contrast (at least 4.5:1) in both themes.

- Use the **semantic** colors (`theme.colors.primary`, `theme.colors.textMuted`...), never the raw `palette`, so light and dark mode both work.
- Use `typography` variants (`title`, `body`, `label`, `caption`...) instead of setting font sizes by hand.
- Show statuses with `toneFor(status)`, so `NEW`, `PAID`, `PENDING_APPROVAL`... look the same on every screen.
- **Web:** `applyTheme()` (in `web/src/lib/theme.ts`) turns the theme into CSS variables such as `var(--color-primary)` and `var(--space-md)`; `web/src/assets/global.css` builds the classes `.btn`, `.input`, `.card`, `.badge`, `.alert` on top of them.
- **Mobile:** `useTheme()` and `makeStyles()` (in `mobile/src/lib/theme.ts`) give screens the current theme; `mobile/src/components/ui.tsx` has `AppText`, `Button`, `Card`, `Field`, `Badge` and `StatusBadge`.
