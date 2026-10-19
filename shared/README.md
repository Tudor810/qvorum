# @qvorum/shared

TypeScript code shared by the web and mobile clients. Both apps import it directly from source:

```ts
import { createApiClient, formatLei, strings } from '@qvorum/shared'
```

There is no build step: Vite and Metro compile the `.ts` files as part of each app.

## Contents

| Path | Contents |
| --- | --- |
| `src/api/client.ts` | `createApiClient`: HTTP client that adds the auth token, sends and parses JSON, and throws `ApiError` on failure |
| `src/api/*.ts` | endpoint functions, one file per feature (e.g. `auth.ts`) |
| `src/types/` | domain entities and API request/response types |
| `src/constants.ts` | user roles and other fixed values |
| `src/format.ts` | Romanian formatting for money (RON) and dates |
| `src/tokens.ts` | design tokens: colors, spacing, radius, font sizes |
| `src/strings.ts` | user interface texts |

## Rules

- **No React and no platform-specific code.** Nothing that only exists in the browser (`window`, `localStorage`, `import.meta.env`) or only in React Native. Each app passes those in, for example the base URL and the token getter given to `createApiClient`. Keeping React out of this package also prevents duplicate React copies across the two apps.
- **No TypeScript `enum`.** Use `as const` objects with a derived union type, as in `constants.ts`.
- **Money is stored in bani** (1 RON = 100 bani) as integers, to avoid floating-point rounding errors.
