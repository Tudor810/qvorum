# Qvorum mobile app

Mobile application for residents. Built with React Native, Expo and Expo Router.

## Run the mobile app

Dependencies are installed once, from the **repository root** (`web`, `mobile` and `shared` are npm workspaces):

```bash
npm install
```

Create your `.env` from the example (once):

```bash
cp mobile/.env.example mobile/.env
```

Then, from the repository root:

```bash
npm run mobile     # Expo dev server; scan the QR code with Expo Go
npm run typecheck  # type-check web, mobile and shared
```

Or from the `mobile/` folder:

```bash
npm run start      # Expo dev server; scan the QR code with Expo Go
npm run android    # open directly on a connected device or emulator
npm run web        # run the same app in a browser
npm run lint       # Expo lint
```

`EXPO_PUBLIC_API_URL` in `mobile/.env` is the server address. It must be the **local network IP** of the computer running the server, never `localhost` (see [Connecting the phone to the server](#5-connect-the-phone-to-the-server)). After changing `.env`, stop Expo and start it again with `npx expo start --clear`, because Expo caches the value.

The app needs a running server for anything beyond the login screen: see [Running the server](#running-the-server) below. The endpoints, request and response formats and error messages are listed in [`docs/api-contract.md`](../docs/api-contract.md).

## Source structure

```
assets/           app icon and splash images (referenced by app.json), other static files
src/
├─ app/           Expo Router routes: every file is a screen; keep them thin
├─ components/    reusable UI components used by several features
├─ hooks/         reusable hooks not tied to a feature
├─ lib/           setup of libraries and services (API client in lib/api.ts, theme in lib/theme.ts)
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

Code that is identical for web and mobile and has no React Native dependency (data types, API endpoints, formatting) belongs in [`shared/`](../shared) instead.

---

## Running the server

This section is for working on the mobile app: it explains how to start the server on your computer and reach it from your phone, without knowing Java or Spring. The full server documentation is in [`server/README.md`](../server/README.md).

### What runs where

```
 phone (Expo Go)  ──── Wi-Fi ────►  your computer: server (port 8080)  ──►  PostgreSQL (Docker)
                                    Java, started with mvnw                  docker compose
                                         │
                                         └──►  Mailpit (Docker): catches the emails the server sends
```

The phone and the computer must be on the **same network**. You need three things running on the computer: the database and Mailpit (both in Docker), the server, and the Expo dev server.

### 1. Install the tools (once)

| Tool | Why | Check |
| --- | --- | --- |
| **JDK 21 or newer** (e.g. [Eclipse Temurin 21](https://adoptium.net/)) | the server is a Java program | `java -version` shows 21 or higher |
| **Docker Desktop** | runs PostgreSQL and Mailpit without installing them | `docker ps` answers without an error |
| **Expo Go** on the phone | runs the app during development | from Google Play / App Store |

You do **not** need to install Maven: the `mvnw` script in `server/` downloads the right version by itself.

If `java -version` shows an older version (for example `1.8`), point `JAVA_HOME` at the JDK 21 folder for the current terminal before starting the server. In PowerShell, replace the path with yours:

```bash
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21"
```

IntelliJ users can instead pick the JDK under *File → Project Structure → SDK* and start the server from the IDE (step 4).

### 2. Start the database and Mailpit

Start **Docker Desktop** and wait until it says it is running. Then, from the `server/` folder:

```bash
docker compose up -d
```

This starts two containers in the background. They stop when Docker Desktop or the computer restarts; run the same command again to start them.

- **PostgreSQL** on port 5432, with the database `qvorum`;
- **Mailpit** on ports 1025 and 8025: every email the server sends (temporary passwords, notifications) lands in the inbox at http://localhost:8025 on the computer, instead of a real mailbox.

### 3. Configure the server (optional)

The defaults work out of the box. In particular `SERVER_HOST=0.0.0.0` makes the server accept connections from other devices on the network, which the phone needs. To change a setting, create `server/.env` from the example:

```bash
cp server/.env.example server/.env
```

### 4. Start the server

From the `server/` folder:

```bash
.\mvnw.cmd spring-boot:run
```

On macOS, Linux or Git Bash use `./mvnw spring-boot:run`.

The first start downloads Maven and all libraries and takes a few minutes; later starts take about 10 seconds. The server is ready when the terminal shows:

```
Started QvorumServerApplication in ... seconds
Qvorum server console. Type 'help' for commands.
```

In IntelliJ: open the **`server/` folder** (not the repository root) as a project, wait for Maven to import, then press the green arrow next to `main` in `QvorumServerApplication.java`.

On every start the server updates the database by itself (Flyway migrations), so after pulling new server code you only need to restart it.

The first time, **Windows Firewall** may ask whether Java can communicate on networks: allow it on **private networks**, otherwise the phone cannot reach the server.

### 5. Connect the phone to the server

On the phone, `localhost` means the phone itself, so the app must use the computer's address on the network.

1. Find the computer's local IP address. On Windows, run `ipconfig` and take the **IPv4 Address** of the Wi-Fi adapter (for example `192.168.1.10`). On macOS: `ipconfig getifaddr en0`.
2. Put it in `mobile/.env`, with the server port:
   ```
   EXPO_PUBLIC_API_URL=http://192.168.1.10:8080
   ```
3. Check from the **phone's browser** that http://192.168.1.10:8080/auth/me answers `{"message":"Please log in to continue."}`. If it does, the app will reach the server too.
4. Restart Expo with `npx expo start --clear` (from `mobile/`) and scan the QR code with Expo Go.

Other ways to run the app:

| Where the app runs | Server address to use |
| --- | --- |
| a real phone on the same Wi-Fi | `http://<computer IP>:8080` |
| the Android emulator on the same computer | `http://10.0.2.2:8080` (the emulator's name for the computer) |
| the browser, with `npm run web` | `http://localhost:8080` |

If the Wi-Fi blocks traffic between devices (common on university and public networks), connect the computer to the phone's **hotspot** and use the computer's IP on that network.

### 6. Log in from the app

Use the test resident account; the password is `Test1234!`.

| Email | Role | Notes |
| --- | --- | --- |
| `resident@qvorum.test` | RESIDENT | lives in apartment 1 of the demo association "Bloc M3" |
| `president@qvorum.test` | PRESIDENT and RESIDENT | also a resident (apartment 2), so it can use the mobile app too |
| `admin@qvorum.test` | ADMINISTRATOR | web app only; the mobile app should refuse it |

### 7. While you work

- **Which endpoints exist now:** `POST /auth/login`, `POST /auth/logout` and `GET /auth/me`. The other endpoints in `docs/api-contract.md` are added phase by phase; until then they answer `404` (or `401` when the request has no valid token, because the login check comes first).
- **Authentication:** after login, the API client in `src/lib/api.ts` sends the token on every request. The token is kept in memory for now, so reloading the app logs you out. A `401` means the session is over (logout, expired token, or the server was restarted, which ends all sessions): go back to the login screen.
- **Errors** always have the body `{ "message": "..." }`, ready to show to the user. A `400` can also contain `fields`, e.g. `{ "email": "Enter a valid email address." }`, to show next to the inputs. The `ApiErrorBody` type in `@qvorum/shared` describes it.
- **Photo uploads** (meter readings, issue reports) are sent as `multipart/form-data`: JPEG or PNG, at most 5 MB.
- **The server console:** the terminal where the server runs accepts commands. `clients` lists who is logged in, with their IP address, which is a quick way to check that the phone reaches the server; `disconnect <id>` ends a session (the next request gets `401 "Your session was closed by the administrator."`); `exit` stops the server.

### 8. Stop everything

- the server: `Ctrl+C` in its terminal, or type `exit`;
- the database and Mailpit: from `server/`, `docker compose stop` (the data is kept). `docker compose down -v` deletes the database as well; the next server start creates it again with the test accounts.

### Common problems

| Symptom | Cause and fix |
| --- | --- |
| The app cannot reach the server, but the computer's browser can | The phone is on another network, the address is `localhost`, or the firewall blocks Java. Do the browser check from step 5 on the phone, and allow Java in Windows Firewall for private networks. |
| The app still uses the old server address | Expo caches `.env`: restart with `npx expo start --clear`. |
| The computer's IP changed (another Wi-Fi, a hotspot) | Run `ipconfig` again and update `EXPO_PUBLIC_API_URL`. |
| `Connection to localhost:5432 refused` when the server starts | The database is not running: start Docker Desktop, then `docker compose up -d` in `server/`. |
| `release version 21 not supported` or `UnsupportedClassVersionError` | The JDK is older than 21: install JDK 21 and set `JAVA_HOME` (step 1). |
| `Port 8080 was already in use` | Another server is still running: stop it, or set `SERVER_PORT` in `server/.env` and update `EXPO_PUBLIC_API_URL`. |
| `Migration checksum mismatch` | A database migration was edited after it ran. Reset the local database: `docker compose down -v`, then `docker compose up -d`. |
| Every request answers `401` after restarting the server | Expected: sessions live in memory, so a restart logs everyone out. Log in again. |
