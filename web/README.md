# Qvorum web app

Web application for the building administrator and the association president. Built with React, TypeScript and Vite.

## Run the web app

Dependencies are installed once, from the **repository root** (`web`, `mobile` and `shared` are npm workspaces):

```bash
npm install
```

Create your `.env` from the example (once):

```bash
cp web/.env.example web/.env
```

Then, from the repository root:

```bash
npm run web        # development server on http://localhost:5173
npm run typecheck  # type-check web, mobile and shared
```

Or from the `web/` folder:

```bash
npm run dev        # development server on http://localhost:5173
npm run build      # type-check and build into dist/
npm run preview    # serve the production build locally
npm run lint       # Oxlint
```

`VITE_API_URL` in `web/.env` is the server address. The default, `http://localhost:8080`, is right when the server runs on the same computer. After changing `.env`, restart `npm run web`.

The web app needs a running server for anything beyond the login screen: see [Running the server](#running-the-server) below. The endpoints, request and response formats and error messages are listed in [`docs/api-contract.md`](../docs/api-contract.md).

## Source structure

```
src/
├─ main.tsx       entry point
├─ app/           root component, routing and app-wide providers
├─ assets/        global CSS, images, fonts
├─ components/    reusable UI components used by several features
├─ hooks/         reusable hooks not tied to a feature
├─ lib/           setup of libraries and services (API client in lib/api.ts, theme in lib/theme.ts)
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

---

## Running the server

This section is for working on the web app: it explains how to start the server on your own computer, without knowing Java or Spring. The full server documentation is in [`server/README.md`](../server/README.md).

### What runs where

```
 browser (http://localhost:5173)  ──►  server (http://localhost:8080)  ──►  PostgreSQL (Docker)
        npm run web                     Java, started with mvnw              docker compose
                                              │
                                              └──►  Mailpit (Docker): catches the emails the server sends
```

You need three things running: the database and Mailpit (both in Docker), the server, and the web app.

### 1. Install the tools (once)

| Tool | Why | Check |
| --- | --- | --- |
| **JDK 21 or newer** (e.g. [Eclipse Temurin 21](https://adoptium.net/)) | the server is a Java program | `java -version` shows 21 or higher |
| **Docker Desktop** | runs PostgreSQL and Mailpit without installing them | `docker ps` answers without an error |

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
- **Mailpit** on ports 1025 and 8025: every email the server sends (temporary passwords, notifications) lands in the inbox at http://localhost:8025 instead of a real mailbox.

### 3. Configure the server (optional)

The defaults work out of the box. To change them, create `server/.env` from the example:

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

### 5. Log in from the web app

With the server and `npm run web` running, open http://localhost:5173 and log in with one of the test accounts. All of them use the password `Test1234!`.

| Email | Role | Use it to test |
| --- | --- | --- |
| `admin@qvorum.test` | ADMINISTRATOR | the administrator screens (manages the demo association "Bloc M3") |
| `president@qvorum.test` | PRESIDENT and RESIDENT | the president screens (read-only views, approvals) |
| `resident@qvorum.test` | RESIDENT | the mobile app; the web app should refuse or redirect this role |

To check the server without the web app, open http://localhost:8080/auth/me in the browser: the answer `{"message":"Please log in to continue."}` means the server is up.

### 6. While you work

- **Which endpoints exist now:** `POST /auth/login`, `POST /auth/logout` and `GET /auth/me`. The other endpoints in `docs/api-contract.md` are added phase by phase; until then they answer `404` (or `401` when the request has no valid token, because the login check comes first).
- **Authentication:** after login, the API client in `src/lib/api.ts` sends the token on every request. A `401` means the session is over (logout, expired token, or the server was restarted, which ends all sessions): go back to the login screen.
- **Errors** always have the body `{ "message": "..." }`, ready to show to the user. A `400` can also contain `fields`, e.g. `{ "email": "Enter a valid email address." }`, to show next to the inputs. The `ApiErrorBody` type in `@qvorum/shared` describes it.
- **The server console:** the terminal where the server runs accepts commands. `clients` lists who is logged in; `disconnect <id>` ends a session (the next request of that user gets `401 "Your session was closed by the administrator."`); `exit` stops the server.
- **Emails** sent by the server appear at http://localhost:8025.

### 7. Stop everything

- the server: `Ctrl+C` in its terminal, or type `exit`;
- the database and Mailpit: from `server/`, `docker compose stop` (the data is kept). `docker compose down -v` deletes the database as well; the next server start creates it again with the test accounts.

### Common problems

| Symptom | Cause and fix |
| --- | --- |
| The login form says the server cannot be reached | The server is not running, or `VITE_API_URL` is wrong. Check http://localhost:8080/auth/me, and restart `npm run web` after editing `.env`. |
| `Connection to localhost:5432 refused` when the server starts | The database is not running: start Docker Desktop, then `docker compose up -d` in `server/`. |
| `release version 21 not supported` or `UnsupportedClassVersionError` | The JDK is older than 21: install JDK 21 and set `JAVA_HOME` (step 1). |
| `Port 8080 was already in use` | Another server is still running: stop it, or set `SERVER_PORT` in `server/.env` and update `VITE_API_URL`. |
| `Migration checksum mismatch` | A database migration was edited after it ran. Reset the local database: `docker compose down -v`, then `docker compose up -d`. |
| Every request answers `401` after restarting the server | Expected: sessions live in memory, so a restart logs everyone out. Log in again. |
| The browser blocks the request with a CORS error | The server allows any origin, so the request most likely did not reach the server at all: check the URL in `VITE_API_URL`. |
