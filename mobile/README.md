# Qvorum mobile app

Mobile application for residents. Built with React Native, Expo and Expo Router.

```bash
npm run start      # Expo dev server; scan the QR code with Expo Go
npm run android    # open directly on a connected device or emulator
npm run web        # run the same app in a browser
```

Set the server address in `.env` (see `.env.example`). Use the server machine's local IP address, not `localhost`.

## Source structure

```
assets/           app icon and splash images (referenced by app.json), other static files
src/
├─ app/           Expo Router routes: every file is a screen; keep them thin
├─ components/    reusable UI components used by several features
├─ hooks/         reusable hooks not tied to a feature
├─ lib/           setup of libraries and services (API client in lib/api.ts)
├─ types/         app-wide types that are not shared with the web app
├─ utils/         app-wide helper functions
└─ features/      one folder per feature (e.g. auth/)
```

Only route files may live in `src/app/`, because Expo Router turns every file there into a screen. Route files import their content from `features/`. Static files stay in the top-level `assets/` folder, which `app.json` and the `@/assets/*` import alias point to.

Each feature follows the MVVM pattern:

| Folder | Role |
| --- | --- |
| `api/` | Model: calls to the server, built on the endpoint functions from `@qvorum/shared` |
| `hooks/` | ViewModel: state and actions exposed to the view |
| `components/` | View: rendering only; never calls the API directly |
| `types/`, `utils/` | types and helpers used only inside the feature |
| `index.ts` | the feature's public exports; other code imports from here |
