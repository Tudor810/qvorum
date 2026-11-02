# Qvorum server

This folder contains the server of Qvorum, a Spring Boot application written in Java. This guide explains **what the server is for**, **how a Spring Boot project works**, **why the code is organized the way it is**, and **how to run it and add new features**. It is written for someone who has not used Spring Boot before.

---

## 1. What the server is for

Qvorum has three applications:

| Application | Who uses it | Where it runs |
| --- | --- | --- |
| Mobile app | residents | Android phone |
| Web app | building administrator, association president | browser |
| **Server** | both apps above + the system administrator | a computer / laptop |

The two client apps only **show** data and **collect** input. They do not store anything permanently and never talk to the database. Everything else happens on the server:

1. **It is the single source of truth.** Apartments, residents, invoices, maintenance lists, payments, announcements, votes, issue reports and deadlines all live in one PostgreSQL database that only the server can access. When the administrator publishes an announcement on the web app, the residents see it on their phones because both apps ask the same server.
2. **It enforces the rules.** The clients can be modified or bypassed, so every rule must be checked on the server: who is allowed to do what (a resident cannot generate the maintenance list, only the president approves expenses), whether input is valid (a meter reading cannot be lower than the previous one), how penalties for arrears are calculated, whether a vote is still open.
3. **It authenticates users.** It checks email and password at login and gives the client a token (JWT). Every later request carries that token, so the server knows who is asking.
4. **It talks to external services.** Online payments go through Stripe, and repair requests are forwarded to service companies by email. API keys for these services must stay secret, so only the server can hold them.
5. **It notifies clients.** When something relevant happens (a new announcement, a status change of an issue report), the server pushes a notification to the connected clients.
6. **It offers a console for the system administrator.** The project requirements ask for a console application. While the server runs, the system administrator can type commands in its terminal to see the server state (UC23), list and force-disconnect connected clients (UC24), and view the request history and save it to a CSV file (UC25).

The clients and the server communicate over **HTTP with JSON**: the client sends a request such as `POST /auth/login` with a JSON body, the server answers with a status code (200 OK, 401 Unauthorized, …) and a JSON body. This style of API is called **REST**.

```
 mobile app ──┐
              ├──  HTTP + JSON  ──►  SERVER  ──►  PostgreSQL database
 web app    ──┘                        │
                                       ├──►  Stripe (payments)
 system admin ── console (keyboard) ───┤
                                       └──►  email (service companies)
```

---

## 2. Spring Boot in a few minutes

**Spring** is a Java framework for building server applications. **Spring Boot** is Spring with sensible defaults: you add a dependency (for example *Spring Web*) and it configures itself. Running the application starts an embedded web server (Tomcat) inside your program, so there is nothing to install or deploy separately.

### Beans and dependency injection

The most important idea in Spring: **you do not create your main objects with `new`**. You mark classes with an annotation, Spring creates one instance of each when the application starts (these instances are called **beans**), and it passes them to whoever needs them through the constructor. This is **dependency injection**.

```java
@Service
public class AuthService {
    private final UserRepository userRepository;

    // Spring sees that AuthService needs a UserRepository and passes the bean in.
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

Why it matters: classes do not depend on how their dependencies are built, there is exactly one shared instance of each service, and in tests you can pass in a fake repository instead of the real one.

### Annotations used in this project

| Annotation | Meaning |
| --- | --- |
| `@SpringBootApplication` | the entry point; Spring scans this package and all sub-packages for beans |
| `@RestController` | a bean whose methods answer HTTP requests and return JSON |
| `@RequestMapping("/auth")`, `@PostMapping("/login")`, `@GetMapping` … | which URL and HTTP method a class or method handles |
| `@RequestBody` | convert the JSON body of the request into a Java object |
| `@Valid` | check the validation annotations (`@NotBlank`, `@Email`, …) on that object |
| `@Service` | a bean containing business logic |
| `@Component` | a general-purpose bean |
| `@Configuration` + `@Bean` | a class whose `@Bean` methods create beans manually (used for library objects) |
| `@Entity`, `@Table`, `@Id`, `@Column` … | JPA: map a class to a database table |
| `@Value("${jwt.secret}")` | inject a value from `application.properties` |
| `@RestControllerAdvice` + `@ExceptionHandler` | central place that turns exceptions into HTTP error responses |
| `@EventListener` | run a method when something happens (here: when the application is ready) |

Because Spring scans sub-packages of `com.qvorum.server`, **every class must be inside that package** (or a sub-package), otherwise Spring will not find it.

### Spring Data JPA

**JPA** maps Java classes to database tables (an *entity* = a table, an *object* = a row). **Spring Data JPA** goes further: you only write an interface, and Spring generates the implementation from the method names:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);   // SELECT * FROM users WHERE email = ?
}
```

`JpaRepository` already provides `findAll()`, `findById(id)`, `save(entity)`, `deleteById(id)` and more.

---

## 3. Project files

```
server/
├─ pom.xml                     dependencies and build settings (Maven)
├─ mvnw, mvnw.cmd, .mvn/       Maven wrapper: downloads the right Maven version automatically
├─ docker-compose.yml          a ready-made PostgreSQL database for development
├─ .env.example                template for .env (local settings, not committed)
└─ src/
   ├─ main/java/com/qvorum/server/   the application code (section 4)
   ├─ main/resources/
   │  ├─ application.properties      configuration
   │  └─ db/migration/               SQL scripts that create the database (Flyway)
   └─ test/java/com/qvorum/server/   tests
```

- **`pom.xml`** is the equivalent of `package.json` in the JavaScript apps. It declares the Spring Boot version and the dependencies: Spring Web MVC (REST API), Spring Data JPA (database), PostgreSQL driver, Flyway (database migrations), Validation, WebSocket (notifications), Spring Security (authentication) and jjwt (JSON Web Tokens).
- **`mvnw` / `mvnw.cmd`** run Maven without installing it: `./mvnw` on Linux/macOS/Git Bash, `mvnw.cmd` in Windows cmd/PowerShell.
- **`application.properties`** holds all settings. Values like `${DB_URL:jdbc:postgresql://localhost:5432/qvorum}` mean "use the variable `DB_URL` from `.env` or the environment, otherwise this default".

---

## 4. Package structure and why it looks like this

```
com.qvorum.server
├─ QvorumServerApplication.java   entry point
├─ controller/    HTTP endpoints
├─ service/       business logic
├─ repository/    database access
├─ model/         database entities
├─ dto/           JSON objects exchanged with clients
├─ security/      login, tokens, password hashing, access rules
├─ exception/     errors and how they become HTTP responses
├─ config/        application-wide configuration
├─ monitoring/    connected clients and request history
└─ console/       system administrator commands
```

### 4.1 Why layers

The code is organized **by layer**: each package has one kind of responsibility, and a request travels through the layers in one direction.

```
 HTTP request
     │
     ▼
 controller   receives the request, validates the input shape, calls a service
     │
     ▼
 service      applies the business rules, decides what happens
     │
     ▼
 repository   reads / writes the database
     │
     ▼
 database
```

Results travel back the same way: the repository returns **entities**, the service converts them into **DTOs**, the controller returns the DTOs, Spring turns them into JSON.

The reasons for this split:

1. **Each class has one job, so it is easy to find things.** A bug in a penalty calculation is in a service; a wrong URL is in a controller; a wrong query is in a repository. You never need to read HTTP code to understand a business rule.
2. **Rules are written once.** If the maintenance list can be generated both from an HTTP request and from a scheduled task, both call the same service method. Logic placed inside a controller would have to be copied.
3. **Testing is simpler.** A service can be tested by giving it fake repositories, without starting a web server or a database.
4. **Changes stay local.** Renaming a URL only touches a controller; changing a table only touches the entity and the repository.
5. **It matches the documentation.** The project requires package and class diagrams; this structure maps one-to-one onto a layered package diagram (controller → service → repository → model).
6. **It is the most common Spring Boot layout**, so tutorials, examples and Stack Overflow answers will look familiar.

**The dependency rule:** a layer may only use the layers below it. A controller uses services, a service uses repositories, never the other way around. A repository never calls a service, and a service never knows about HTTP.

### 4.2 What goes in each package

#### `controller/`: the HTTP entry points

One class per area of the API (`AuthController`, later `AnnouncementController`, `InvoiceController`, …). A controller method:
- declares the URL and HTTP method (`@PostMapping("/login")`);
- receives input (`@RequestBody`, `@PathVariable`, `@RequestParam`) as DTOs and validates it with `@Valid`;
- calls **one** service method and returns its result.

It contains **no business logic and no database access**. If a controller method is longer than a few lines, the extra code belongs in a service.

*Example:* [`AuthController`](src/main/java/com/qvorum/server/controller/AuthController.java) maps `POST /auth/login` to `AuthService.login`.

#### `service/`: the business logic

This is where the application really lives: calculating the maintenance list from invoices, checking that a vote is still open, computing penalties, deciding who can approve an expense. Services:
- use repositories to load and save data;
- throw an `ApiException` when something is not allowed or not found;
- return **DTOs**, never entities.

*Example:* [`AuthService`](src/main/java/com/qvorum/server/service/AuthService.java) finds the user by email, checks the password hash and creates a token.

#### `repository/`: database access

One interface per entity, extending `JpaRepository<Entity, IdType>`. Usually you only declare method names (`findByEmail`, `findByApartamentIdOrderByDataDesc`, …). For complex queries you can write `@Query("…")` on a method.

*Example:* [`UserRepository`](src/main/java/com/qvorum/server/repository/UserRepository.java).

#### `model/`: the entities

One class per database table, matching the ERD. Fields map to columns (`passwordHash` → `password_hash` automatically), relationships are expressed with `@ManyToOne`, `@OneToMany`, `@ElementCollection`. Enums that are stored in the database (like `Role`) also live here.

*Example:* [`User`](src/main/java/com/qvorum/server/model/User.java) and [`Role`](src/main/java/com/qvorum/server/model/Role.java).

#### `dto/`: what the clients send and receive

**DTO** = Data Transfer Object: a small class (here a Java `record`) that describes exactly the JSON of a request or response. Why not return entities directly?
- **Security:** `User` contains the password hash; `UserDto` does not. With DTOs, nothing leaks by accident.
- **A stable API:** the database can change (a column is split, a table is added) without changing the JSON the apps expect.
- **No serialization problems:** entities often reference each other (an apartment has residents, a resident has an apartment). Converting them straight to JSON can loop forever or trigger many hidden queries.
- **One shape per use case:** creating an announcement needs different fields than listing announcements.

The DTOs must match the TypeScript types in [`shared/src/types`](../shared/src/types) **field by field**, because the web and mobile apps read exactly those names. When you add or rename a DTO field, the matching TypeScript type changes too.

*Examples:* [`LoginRequest`](src/main/java/com/qvorum/server/dto/LoginRequest.java), [`LoginResponse`](src/main/java/com/qvorum/server/dto/LoginResponse.java), [`UserDto`](src/main/java/com/qvorum/server/dto/UserDto.java) (with a `from(entity)` method that does the conversion).

#### `security/`: authentication and authorization

- [`SecurityConfig`](src/main/java/com/qvorum/server/security/SecurityConfig.java) configures Spring Security. Adding Spring Security without a configuration locks every endpoint behind a generated password, which is why this class exists from the start. It makes the API **stateless** (no sessions or cookies; each request carries a token), allows `/auth/**` without a token, and provides the BCrypt `PasswordEncoder`.
- [`JwtService`](src/main/java/com/qvorum/server/security/JwtService.java) creates and reads **JSON Web Tokens**. A JWT is a signed string containing the user id and an expiry date. Because it is signed with `jwt.secret`, the server can trust it without remembering anything about the user between requests.
- Passwords are stored only as **BCrypt hashes**. BCrypt is one-way: the server can check whether a password matches a hash but can never recover the password.

#### `exception/`: one consistent error format

Services throw [`ApiException`](src/main/java/com/qvorum/server/exception/ApiException.java) with an HTTP status and a message. [`GlobalExceptionHandler`](src/main/java/com/qvorum/server/exception/GlobalExceptionHandler.java) catches it anywhere in the application and returns:

```json
{ "message": "Invalid email or password." }
```

Every error looks the same, so the clients can show `message` to the user directly. Messages are therefore written as complete sentences. Without the handler, Spring returns its own generic error JSON, which does not include a readable message.

#### `config/`: application-wide setup

Configuration that is not tied to one feature: [`CorsConfig`](src/main/java/com/qvorum/server/config/CorsConfig.java) allows the web app (which runs on a different address/port) to call the API; browsers block such calls unless the server allows them (**CORS**). The WebSocket configuration for notifications and beans for external libraries (Stripe client, email sender) will also go here.

#### `monitoring/`: what the system administrator observes

State that exists only while the server runs, for UC24 and UC25: [`ClientRegistry`](src/main/java/com/qvorum/server/monitoring/ClientRegistry.java) keeps the list of connected clients. The request/response history and connection/disconnection events will be recorded here as well.

#### `console/`: the system administrator's commands

[`AdminConsole`](src/main/java/com/qvorum/server/console/AdminConsole.java) starts when the server is ready and reads commands typed in the terminal (`help`, `status`, `clients`, `exit`). It runs on its own thread, so the server keeps answering HTTP requests while the console waits for input. It reads from `monitoring/` and never accesses the database directly.

---

## 5. Walkthrough: what happens when someone logs in

1. The user types an email and password in the web or mobile app and presses *Log in*. The app sends:
   ```
   POST http://<server>:8080/auth/login
   { "email": "admin@qvorum.test", "password": "…" }
   ```
2. **Spring Security** lets the request through, because `SecurityConfig` permits `/auth/**`.
3. Spring finds the method mapped to `POST /auth/login`: `AuthController.login`. It converts the JSON into a `LoginRequest` and, because of `@Valid`, checks `@NotBlank` and `@Email`. If they fail, `GlobalExceptionHandler` answers `400 {"message": "Invalid data."}`.
4. `AuthController` calls `AuthService.login(request)`.
5. `AuthService` calls `userRepository.findByEmail(...)`. Spring Data runs `SELECT … FROM users WHERE email = ?` and Hibernate turns the row into a `User` object.
6. `AuthService` checks the password with `passwordEncoder.matches(plain, hash)`. If the user does not exist or the password is wrong, it throws `ApiException(401, "Invalid email or password.")`, which the handler turns into a 401 response.
7. If everything matches, `JwtService.generateToken` creates the token, and `UserDto.from(...)` converts the entity into a DTO without the password hash.
8. The controller returns the `LoginResponse`; Spring converts it to JSON:
   ```json
   { "token": "eyJhbGciOi…", "user": { "id": 2, "firstName": "Mihai", "lastName": "Ionescu", "email": "admin@qvorum.test", "roles": ["ADMINISTRATOR"] } }
   ```
9. The app stores the token and sends it on every following request as `Authorization: Bearer <token>`.

---

## 6. Database and migrations

The database is **PostgreSQL**. Its tables are created by **Flyway** from the SQL files in [`src/main/resources/db/migration`](src/main/resources/db/migration):

- `V1__create_utilizator.sql` creates the user tables (with the original Romanian names);
- `V2__seed_test_users.sql` inserts test accounts;
- `V3__rename_schema_to_english.sql` renames the tables, columns and roles to English (`users`, `user_roles`, `RESIDENT` / `ADMINISTRATOR` / `PRESIDENT`).

On every start, Flyway checks which scripts have already run (it records them in the table `flyway_schema_history`) and runs only the new ones, in version order. Hibernate is set to `ddl-auto=validate`: it never creates or changes tables, it only checks at start-up that the entities match the tables and stops with an error if they do not.

Why Flyway instead of letting Hibernate create tables: the schema is written explicitly in SQL, it is versioned in Git, every machine ends up with exactly the same database, and the scripts double as the database deliverable of the project.

**Rules:**
- To change the database, add a new file `V4__description.sql`, `V5__…`. **Never edit a script that has already run**; Flyway detects the change and refuses to start.
- Each entity must match its table, otherwise the validation fails at start-up.
- During development, if the database gets into a bad state, delete it and start again (see section 7.3); all scripts run again from V1.

**Test accounts** (all with the password written at the top of `V2__seed_test_users.sql`):

| Email | Roles |
| --- | --- |
| `resident@qvorum.test` | RESIDENT |
| `admin@qvorum.test` | ADMINISTRATOR |
| `president@qvorum.test` | PRESIDENT, RESIDENT |

---

## 7. Getting started

### 7.1 Install

- **JDK 21 or newer** (Spring Boot 4 does not run on Java 8 or 11). Check with `java -version`.
- **An IDE:**
  - **IntelliJ IDEA** (recommended for Spring Boot): it understands beans, shows all endpoints, and runs the app with one click. Spring support is in the commercial edition, which is free for students with the JetBrains educational licence. Open the `server/` folder; IntelliJ detects `pom.xml` and imports the project. It can also download a JDK for you (*File → Project Structure → SDK*).
  - **VS Code** with *Extension Pack for Java* and *Spring Boot Extension Pack*. Open the `server/` folder or the whole repository; the *Spring Boot Dashboard* in the sidebar starts the app.
- **PostgreSQL**, in one of two ways:
  - **Docker** (easiest): install Docker Desktop, then from `server/` run `docker compose up -d`. This starts a database already configured with the values from `.env.example`.
  - **Local install:** install PostgreSQL from postgresql.org, then in *psql* or *pgAdmin* run:
    ```sql
    CREATE USER qvorum WITH PASSWORD 'qvorum';
    CREATE DATABASE qvorum OWNER qvorum;
    ```

### 7.2 Configure and run

```bash
cp .env.example .env      # once; adjust values if needed
./mvnw spring-boot:run    # Windows cmd/PowerShell: mvnw.cmd spring-boot:run
```

The first run downloads Maven and all dependencies, which takes a few minutes. When you see `Started QvorumServerApplication` and `Qvorum server console. Type 'help' for commands.`, the server is listening on port 8080 and the console accepts commands.

Alternatively, run `QvorumServerApplication` from the IDE (the green arrow next to `main`). In VS Code, the console only receives keyboard input when the program runs in the integrated terminal (`"console": "integratedTerminal"` in the launch configuration).

Try the login from another terminal:

```bash
curl -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"email":"admin@qvorum.test","password":"<password from V2__seed_test_users.sql>"}'
```

Other useful commands:

```bash
./mvnw test                     # run the tests (the database must be running)
./mvnw package -DskipTests      # build target/server-0.0.1-SNAPSHOT.jar
java -jar target/server-0.0.1-SNAPSHOT.jar   # run the built jar
```

### 7.3 Common problems

| Symptom | Cause and fix |
| --- | --- |
| `Connection to localhost:5432 refused` | PostgreSQL is not running. Start it (`docker compose up -d`) or check `DB_URL`. |
| `password authentication failed for user "qvorum"` | `DB_USER` / `DB_PASSWORD` in `.env` do not match the database. |
| `Schema-validation: missing table [...]` or `missing column` | An entity does not match the tables. Add a migration that creates/changes the table. |
| `Validate failed: Migration checksum mismatch` | A migration that already ran was edited. Revert the edit and add a new migration, or reset the development database (`docker compose down -v`, then `up -d`). |
| `Port 8080 was already in use` | Another server is still running. Stop it or change `SERVER_PORT`. |
| `UnsupportedClassVersionError` / `release version 21 not supported` | The JDK is older than 21. Point the IDE / `JAVA_HOME` to JDK 21+. |
| The phone cannot reach the server | Use the laptop's local IP in the app's `.env`, keep `SERVER_HOST=0.0.0.0`, and allow Java through the Windows firewall. |
| `Using generated security password` warning at start-up | Harmless: Spring Security creates a default user nobody uses. Login goes through `/auth/login`, not this password. |
| A new class is ignored by Spring | It is outside the `com.qvorum.server` package or is missing its annotation (`@Service`, `@RestController`, …). |

---

## 8. How to add a feature

Example: announcements (UC14, "Manage announcements").

1. **Database:** add `V4__create_announcements.sql` with the `CREATE TABLE announcements (...)` statement from the ERD.
2. **Entity:** `model/Announcement.java` with `@Entity`, fields matching the columns, relationships (e.g. `@ManyToOne` to the author).
3. **Repository:** `repository/AnnouncementRepository.java` extending `JpaRepository<Announcement, Long>`, plus query methods you need (`findAllByOrderByPublishedAtDesc()`).
4. **DTOs:** `dto/AnnouncementDto.java` for responses and `dto/CreateAnnouncementRequest.java` for input, with validation annotations. Add the matching TypeScript types to `shared/src/types`.
5. **Service:** `service/AnnouncementService.java` with methods like `list()`, `create(request, author)`; check permissions and rules here and throw `ApiException` when something is not allowed.
6. **Controller:** `controller/AnnouncementController.java` with `@RequestMapping("/announcements")`, `@GetMapping`, `@PostMapping`, each calling one service method.
7. **Client side:** add the endpoint functions in `shared/src/api/` so the web and mobile apps can call them.

Keep URLs as plural nouns (`/announcements`, `/invoices`, `/issues/{id}`) and use the HTTP method for the action: `GET` read, `POST` create, `PUT` update, `DELETE` delete.

---

## 9. What is not implemented yet

The skeleton runs and login works end to end. These pieces are the next steps:

- **JWT filter:** a filter in `security/` that reads `Authorization: Bearer <token>` on every request, validates it with `JwtService.extractUserId`, and tells Spring Security who the current user is. After that, change `.anyRequest().permitAll()` to `.authenticated()` in `SecurityConfig`, and restrict endpoints by role.
- **Notifications and connected clients:** a WebSocket endpoint (configuration in `config/`) that the apps connect to after login. Connections and disconnections update `ClientRegistry`; the console can then list and force-disconnect clients (UC24), and services push notifications through it (UC4).
- **Request history (UC25):** a filter in `monitoring/` that records each request and its response, plus a console command that prints the history and saves it to CSV.
- **Server state (UC23):** console commands to stop and resume accepting client requests (e.g. answer 503 while stopped) and to show the server status.
- **Domain features:** apartments, announcements, invoices and maintenance lists, payments (Stripe test mode), meter readings, issue reports, votes, deadline calendar, statistics, following the ERD.
- **Tests:** the generated `QvorumServerApplicationTests` only checks that the application starts (it needs the database). Service tests with fake repositories are the most valuable next ones.
