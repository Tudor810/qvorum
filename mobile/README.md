# Qvorum mobile app

Mobile application for residents. Built with React Native, Expo and Expo Router.

```bash
npm run start      # Expo dev server; scan the QR code with Expo Go
npm run android    # open directly on a connected device or emulator
npm run web        # run the same app in a browser
```

Set the server address in `.env` (see `.env.example`). Use the server machine's local IP address, not `localhost`. Screens live in `src/app/` (one file per route), and `src/api.ts` creates the API client from `@qvorum/shared`.
