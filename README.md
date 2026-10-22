# Qvorum

Qvorum is a management platform for apartment buildings and homeowners' associations. It replaces the chat groups that building administrators usually rely on with one place for announcements, maintenance bills, online payments, meter readings, repair requests and association votes.

The user interface is in Romanian.

## Applications

| Application | Users | Main features |
| --- | --- | --- |
| **Mobile app** (`mobile/`) | Residents | digital notice board, maintenance list and payment history, online payment, meter readings, issue reports with status tracking, votes and fundraisers, notifications |
| **Web app** (`web/`) | Building administrator, association president | apartments and residents, announcements, invoices and maintenance list generation, repair requests, administrative deadline calendar, arrears and penalties, statistics, list export; the president also approves expenses and manages users |
| **Server** (`server/`) | System administrator (console) | server start/stop, connected clients (list, force disconnect), request history with CSV export |

## Architecture

```
 mobile (Expo)  ─┐
                 ├──  HTTP / JSON  ──►  server (console app)  ──►  database
 web (React)    ─┘
```

Both clients are written in TypeScript and share a common package, `shared/`, which contains the API client, the data types, formatting helpers, design tokens and the UI texts. Client code follows the MVVM pattern: views are React components, view models are hooks, and models are the types and API calls.

## Repository structure

```
qvorum/
├─ web/       React + Vite web app
├─ mobile/    React Native (Expo) mobile app
├─ shared/    TypeScript code used by both clients (@qvorum/shared)
├─ server/    server application
└─ docs/      requirements, UML diagrams, documentation
```

`web`, `mobile` and `shared` are npm workspaces, so all JavaScript dependencies are installed once from the repository root.

## Tech stack

- **Web:** React 19, TypeScript, Vite, Oxlint
- **Mobile:** React Native, Expo, Expo Router, TypeScript
- **Server:** Java, Spring Boot, Spring Data JPA (see [`server/README.md`](server/README.md))

## Getting started

### Prerequisites

- Node.js 22 or newer, and npm
- For the server: JDK 21 or newer and PostgreSQL (see [`server/README.md`](server/README.md))
- For the mobile app: an Android phone with [Expo Go](https://expo.dev/go), or an Android emulator

### Install

```bash
npm install
```

### Configure

Each application reads the server address from its own `.env` file. Copy the examples and adjust them:

```bash
cp web/.env.example web/.env
cp mobile/.env.example mobile/.env
cp server/.env.example server/.env
```

| Variable | File | Description |
| --- | --- | --- |
| `VITE_API_URL` | `web/.env` | server URL used by the web app |
| `EXPO_PUBLIC_API_URL` | `mobile/.env` | server URL used by the mobile app |
| `SERVER_HOST`, `SERVER_PORT` | `server/.env` | address and port the server listens on |

### Run

```bash
npm run web        # web app on http://localhost:5173
npm run mobile     # Expo dev server; scan the QR code with Expo Go
npm run typecheck  # type-check all TypeScript projects
```

## Running on separate machines

The server and the clients can run on different computers on the same network:

1. Start the server listening on `0.0.0.0` instead of `localhost`.
2. Find the server machine's local IP address (for example `192.168.1.10`).
3. Set `VITE_API_URL` and `EXPO_PUBLIC_API_URL` to `http://<that IP>:<port>`.

A phone can never reach the server through `localhost`, because on the phone `localhost` is the phone itself. If the network blocks traffic between devices, a phone hotspot or a tunnel such as ngrok works as an alternative.
