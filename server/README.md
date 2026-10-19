# Qvorum server

Console application that serves the web and mobile clients. Its language and framework have not been chosen yet.

Requirements so the clients can connect from other machines:

- listen on `0.0.0.0`, not only on `localhost`;
- allow cross-origin requests (CORS) from the web app;
- read the host and port from `.env` (see `.env.example`).
