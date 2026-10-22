# Qvorum web app

Web application for the building administrator and the association president. Built with React, TypeScript and Vite.

```bash
npm run dev        # development server on http://localhost:5173
npm run build      # type-check and build into dist/
npm run preview    # serve the production build locally
npm run lint       # Oxlint
```

Set the server address in `.env` (see `.env.example`).

## Source structure

```
src/
├─ main.tsx       entry point
├─ app/           root component, routing and app-wide providers
├─ assets/        global CSS, images, fonts
├─ components/    reusable UI components used by several features
├─ hooks/         reusable hooks not tied to a feature
├─ lib/           setup of libraries and services (API client in lib/api.ts)
├─ types/         app-wide types that are not shared with the mobile app
├─ utils/         app-wide helper functions
└─ features/      one folder per feature (e.g. auth/)
```

Each feature follows the MVVM pattern:

| Folder | Role |
| --- | --- |
| `api/` | Model: calls to the server, built on the endpoint functions from `@qvorum/shared` |
| `hooks/` | ViewModel: state and actions exposed to the view |
| `components/` | View: rendering only; never calls the API directly |
| `types/`, `utils/` | types and helpers used only inside the feature |
| `index.ts` | the feature's public exports; other code imports from here |

Code that is identical for web and mobile and has no React or browser dependency (data types, API endpoints, formatting) belongs in [`shared/`](../shared) instead.
