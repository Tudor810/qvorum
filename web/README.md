# Qvorum web app

Web application for the building administrator and the association president. Built with React, TypeScript and Vite.

```bash
npm run dev        # development server on http://localhost:5173
npm run build      # type-check and build into dist/
npm run preview    # serve the production build locally
npm run lint       # Oxlint
```

Set the server address in `.env` (see `.env.example`). `src/api.ts` creates the API client from `@qvorum/shared` and stores the auth token in `localStorage`.
