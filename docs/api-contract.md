# Qvorum – frontend / backend contract

This document is the agreement between the clients (web app, mobile app) and the server: every endpoint, what it receives, what it returns and which errors it can produce. The server implements it, the clients call it through `@qvorum/shared`, and the field names below are used unchanged in the Java DTOs (`server/.../dto`) and the TypeScript types (`shared/src/types`).

**Changing the contract:** any change (a new field, a renamed path, a new error) is made here first, in a pull request reviewed by the other side, and only then in the code.

---

## 1. General rules

### 1.1 Transport

| Rule | Value |
| --- | --- |
| Base URL | from the client's `.env`: `VITE_API_URL` (web), `EXPO_PUBLIC_API_URL` (mobile), e.g. `http://192.168.1.10:8080` |
| Format | JSON in both directions, `Content-Type: application/json; charset=utf-8` |
| File uploads | `multipart/form-data` (meter and issue photos, max 5 MB, JPEG or PNG) |
| File downloads | the file itself with `Content-Disposition: attachment; filename="..."` (CSV exports) |
| Paths | plural English nouns in kebab-case: `/apartments`, `/service-companies/{id}` |
| Methods | `GET` read · `POST` create or trigger an action · `PUT` update · `DELETE` delete |

### 1.2 Authentication

- `POST /auth/login` returns a **JWT**. Every other request (except the public ones marked *public*) sends it as `Authorization: Bearer <token>`.
- The token is valid for 12 hours. A `401` on any request means the session is over: the client clears the token and shows the login screen.
- Tokens are kept by the client: web in memory + `sessionStorage`, mobile in `expo-secure-store`.

### 1.3 Roles

| Role | Client | Scope |
| --- | --- | --- |
| `RESIDENT` | mobile | their own apartment |
| `PRESIDENT` | web | their own association, read-only views and approvals |
| `ADMINISTRATOR` | web | every association they manage |

Each endpoint lists the roles allowed to call it. Any other role receives `403`.

### 1.4 Selected association (administrator)

An administrator can manage several associations (blocks). The web app keeps the selected one in a React context, and the API client adds it to **every request**:

```
X-Association-Id: 12
```

| Who | Rule |
| --- | --- |
| `ADMINISTRATOR` | the header is **required** on every endpoint marked *association*. Missing → `400 "Select an association."`; an association the administrator does not manage → `403`. |
| `RESIDENT`, `PRESIDENT` | the header is not needed and is ignored: the server uses the association the user belongs to. |

Endpoints that are not tied to one association (`/auth/*`, `/me*`, `/associations`, `/notifications`) never need the header.

### 1.5 Data formats

| Kind | Format | Example |
| --- | --- | --- |
| Field names | camelCase, English | `firstName`, `dueDate` |
| IDs | integer numbers | `42` |
| Money | **integer in bani** (1 RON = 100 bani) | `12345` = 123.45 RON |
| Date | ISO date | `"2026-10-26"` |
| Month | ISO year-month | `"2026-10"` |
| Date and time | ISO 8601 in UTC | `"2026-10-26T14:30:00Z"` |
| Enumerations | uppercase strings | `"IN_PROGRESS"` |
| Missing value | `null` (never an empty string, never omitted) | `"eventDate": null` |
| Lists | a plain JSON array, newest first unless stated | `[ {...}, {...} ]` |

### 1.6 Errors

Every error response has the same body, with a message written for the user (shown as is by the clients):

```json
{ "message": "The reading cannot be lower than the previous one." }
```

Validation errors (`400`) can also say which fields are wrong:

```json
{ "message": "Invalid data.", "fields": { "email": "Enter a valid email address." } }
```

| Status | Meaning | Client reaction |
| --- | --- | --- |
| `200 OK` | success with a body | show the data |
| `201 Created` | a resource was created; the body is the new resource | show it / go to it |
| `204 No Content` | success without a body | refresh the view |
| `400 Bad Request` | invalid input | show `message`, highlight `fields` |
| `401 Unauthorized` | not logged in, wrong credentials or session ended | go to the login screen (except on the login form itself) |
| `403 Forbidden` | logged in, but this role or association may not do it | show `message` |
| `404 Not Found` | the resource does not exist or is not visible to the user | show `message` |
| `409 Conflict` | the action conflicts with the current state (duplicate, already done, already published) | show `message` |
| `413 Payload Too Large` | file over 5 MB | show `message` |
| `422 Unprocessable Entity` | the data is valid but incomplete for this action (e.g. bill without all readings) | show `message` and the details |
| `502 Bad Gateway` | an external service failed (email, SMS, Stripe) | show `message`, allow a retry |
| `503 Service Unavailable` | the server was stopped from the console (UC23) | show `"The server is temporarily stopped."` |

### 1.7 Live updates (WebSocket)

After login the clients open one WebSocket connection, used for notifications (UC4) and listed in the console (UC24).

| Item | Value |
| --- | --- |
| Endpoint | `ws://<server>/ws?token=<JWT>` (STOMP) |
| Subscription | `/user/queue/notifications`: each message is a `Notification` |
| Close codes | `1001` the server was stopped (UC23) · `4001` disconnected by the system administrator (UC24); the next HTTP request then returns `401 "Your session was closed by the administrator."` |

---

## 2. Data types

Types are written in TypeScript notation; `| null` marks optional values. Money fields are in bani.

```ts
type Role = 'RESIDENT' | 'PRESIDENT' | 'ADMINISTRATOR'

interface User {
  id: number
  firstName: string
  lastName: string
  email: string
  phone: string | null
  roles: Role[]
  associationId: number | null      // null for administrators
  apartmentId: number | null        // null for administrators
  mustChangePassword: boolean       // true until the temporary password is changed
}

interface AuthResponse { token: string; user: User }

interface Association {
  id: number
  name: string
  address: string
  fiscalCode: string
  iban: string
  dueDay: number                    // day of the month the bill is due, 1-28
  readingStartDay: number           // meter readings accepted from this day...
  readingEndDay: number             // ...until this day of the month
  penaltyRate: number               // percent per day late, e.g. 0.2
  graceDays: number
  approvalThreshold: number         // invoices above it need the president's approval
  staircases: Staircase[]
}

interface Staircase { id: number; name: string }

type MeterType = 'COLD_WATER' | 'HOT_WATER' | 'GAS'
interface Meter {
  id: number
  type: MeterType
  serialNumber: string
  lastReading: MeterReading | null
}

interface Apartment {
  id: number
  staircaseId: number
  number: string
  floor: number
  area: number                      // m²
  share: number                     // cotă-parte, e.g. 0.0234
  occupants: number                 // current declaration
  meters: Meter[]
  residentName: string | null       // owner of the account, if any
}

interface MeterReading {
  id: number
  meterId: number
  month: string                     // "2026-10"
  value: number
  consumption: number
  photoUrl: string
  submittedAt: string
}

interface OccupancyDeclaration { apartmentId: number; occupants: number; validFrom: string; declaredAt: string }

type AnnouncementType = 'ANNOUNCEMENT' | 'PLANNED_WORK' | 'BUILDING_CHANGE'
interface Announcement {
  id: number
  title: string
  content: string
  type: AnnouncementType
  eventDate: string | null
  publishedAt: string
  read: boolean                     // for the resident calling
  readCount: number                 // for the administrator / president
}

interface AnnouncementReads {
  totalResidents: number
  reads: { userId: number; name: string; apartmentNumber: string; readAt: string }[]
}

type NotificationType = 'DUE_DATE' | 'NEW_VOTE' | 'NEW_ANNOUNCEMENT' | 'ISSUE_UPDATE' | 'APPROVAL_RESULT' | 'DEADLINE'
interface Notification {
  id: number
  type: NotificationType
  title: string
  message: string
  referenceId: number | null        // id of the announcement, vote, issue, bill...
  createdAt: string
  read: boolean
}

type VoteStatus = 'ACTIVE' | 'CLOSED' | 'APPROVED' | 'REJECTED'
interface Vote {
  id: number
  title: string
  description: string
  options: { id: number; text: string }[]
  deadline: string
  validationThreshold: number       // minimum turnout in percent
  status: VoteStatus
  myOptionId: number | null         // the resident's apartment ballot, if any
}

interface VoteResults {
  voteId: number
  eligible: number                  // apartments
  ballots: number
  turnout: number                   // percent
  thresholdMet: boolean
  options: { id: number; text: string; ballots: number; percent: number }[]
  status: VoteStatus
}

type SplitMethod = 'CONSUMPTION' | 'OCCUPANTS' | 'SHARE'
type InvoiceStatus = 'VALID' | 'PENDING_APPROVAL' | 'REJECTED'
interface Invoice {
  id: number
  supplier: string
  number: string
  issueDate: string
  month: string
  category: string                  // e.g. "Water", "Electricity", "Cleaning"
  amount: number
  splitMethod: SplitMethod
  status: InvoiceStatus
}

type BillStatus = 'DRAFT' | 'PUBLISHED'
interface MaintenanceBill {
  id: number
  month: string
  status: BillStatus
  dueDate: string
  publishedAt: string | null
  items: BillItem[]
}

type BillItemStatus = 'UNPAID' | 'PAID'
interface BillItem {                // one apartment's part of a monthly bill
  id: number
  billId: number
  month: string
  apartmentId: number
  apartmentNumber: string
  lines: { category: string; splitMethod: SplitMethod; amount: number }[]
  arrears: number                   // unpaid amounts from previous months
  penalties: number
  total: number
  dueDate: string
  status: BillItemStatus
}

type PaymentMethod = 'CARD' | 'CASH'
type PaymentStatus = 'PENDING' | 'SUCCEEDED' | 'FAILED'
interface Payment {
  id: number
  billItemId: number
  month: string
  amount: number
  method: PaymentMethod
  status: PaymentStatus
  receiptNumber: string | null      // set when the payment succeeds
  paidAt: string | null
}

interface Receipt {
  receiptNumber: string
  associationName: string
  fiscalCode: string
  apartmentNumber: string
  payerName: string
  month: string
  amount: number
  method: PaymentMethod
  paidAt: string
}

type IssueCategory = 'PLUMBING' | 'ELECTRICAL' | 'ELEVATOR' | 'OTHER'
type IssueStatus = 'NEW' | 'IN_PROGRESS' | 'RESOLVED'
interface IssueReport {
  id: number
  apartmentNumber: string
  category: IssueCategory
  description: string
  photoUrl: string | null
  status: IssueStatus
  serviceCompany: ServiceCompany | null
  createdAt: string
  history: { status: IssueStatus; comment: string | null; changedAt: string }[]
}

interface ServiceCompany { id: number; name: string; domain: IssueCategory; email: string; phone: string }

type DeadlineType = 'INSPECTION' | 'CONTRACT' | 'PAYMENT' | 'OTHER'
interface Deadline {
  id: number
  title: string
  description: string | null
  dueDate: string
  type: DeadlineType
  remindDaysBefore: number
}

interface ArrearsRow {
  apartmentId: number
  apartmentNumber: string
  residentName: string | null
  amountDue: number
  daysLate: number                  // for the oldest unpaid month
  penalties: number
  total: number
}

type ApprovalType = 'EXPENSE' | 'VOTE_RESULT'
type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
interface ApprovalRequest {
  id: number
  type: ApprovalType
  invoice: Invoice | null           // for EXPENSE
  voteResults: VoteResults | null   // for VOTE_RESULT
  status: ApprovalStatus
  reason: string | null             // required when rejected
  createdAt: string
  decidedAt: string | null
}
```

---

## 3. Endpoints by use case

Columns: **Roles** who may call it; **Assoc.** = needs the selected association (§1.4) when the caller is an administrator.

### UC1 – Log in

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Log in | `POST /auth/login` | *public* | `{ email, password }` | `200 AuthResponse` | `400` invalid fields · `401 "Invalid email or password."` |
| Change the temporary password (first login) | `PUT /me/password` | all | `{ currentPassword, newPassword }` | `204` | `400` weak password · `401 "Current password is wrong."` |
| Current user (app restart) | `GET /auth/me` | all | – | `200 User` | `401` |
| Log out | `POST /auth/logout` | all | – | `204` (the WebSocket closes) | – |

When `user.mustChangePassword` is `true`, the clients show the change-password form before anything else; every other endpoint answers `403 "Change your temporary password first."` until it is changed. Passwords: at least 8 characters, with a letter and a digit.

### UC2 – Manage own profile

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| View profile | `GET /me` | all | – | `200 User` | – |
| Edit details | `PUT /me` | all | `{ firstName, lastName, phone }` | `200 User` | `400` |
| Change email or password | `PUT /me/credentials` | all | `{ currentPassword, newEmail \| null, newPassword \| null }` | `204`; an email **and** an SMS confirm the change; other sessions end | `401` wrong current password · `409 "This email is already used."` · `502` notification failed (the change is undone) |

### UC3 – View notice board and confirm reading

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| List announcements | `GET /announcements?type=` | all · Assoc. | `type` optional | `200 Announcement[]` | – |
| Announcement details | `GET /announcements/{id}` | all · Assoc. | – | `200 Announcement` | `404 "Announcement not found."` |
| Confirm reading | `POST /announcements/{id}/read` | RESIDENT | – | `204` (a second call keeps the first date) | `404` |

### UC4 – View notifications

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Live notification | WebSocket `/user/queue/notifications` | all | – | `Notification` | – |
| List notifications | `GET /notifications?unreadOnly=` | all | `unreadOnly` optional, default `false` | `200 Notification[]` | – |
| Mark one as read | `PUT /notifications/{id}/read` | all | – | `204` | `404` |
| Mark all as read | `PUT /notifications/read-all` | all | – | `204` | – |

### UC5 – Submit meter readings

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Meters of the apartment | `GET /apartments/{id}/meters` | RESIDENT (own) · ADMINISTRATOR · Assoc. | – | `200 Meter[]` | `403` another apartment · `404` |
| Submit a reading | `POST /meters/{id}/readings` (multipart) | RESIDENT (own) | `value` (number), `photo` (file) | `201 MeterReading` | `400 "The reading cannot be lower than the previous one."` · `409 "The reading period is closed."` · `413` |
| Reading history | `GET /meters/{id}/readings` | RESIDENT (own) · ADMINISTRATOR · Assoc. | – | `200 MeterReading[]` | `404` |

### UC6 – Declare number of occupants

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Current declaration | `GET /apartments/{id}/occupancy` | RESIDENT (own) · ADMINISTRATOR · Assoc. | – | `200 OccupancyDeclaration` | `403` · `404` |
| Declare | `PUT /apartments/{id}/occupancy` | RESIDENT (own) | `{ occupants, validFrom }` (`validFrom` = first day of a month) | `200 OccupancyDeclaration` | `400 "The number of occupants cannot be negative."` · `409 "This month is already billed."` |

### UC7 – View maintenance bill, payment history and receipts

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Bill of a month | `GET /apartments/{id}/bills/{month}` | RESIDENT (own) · ADMINISTRATOR · PRESIDENT · Assoc. | `month` = `"2026-10"` | `200 BillItem` | `404 "The bill is not published yet."` |
| Bills of the apartment | `GET /apartments/{id}/bills` | same | – | `200 BillItem[]` | – |
| Payment history | `GET /apartments/{id}/payments` | same | – | `200 Payment[]` | – |
| Receipt | `GET /payments/{id}/receipt` | same | – | `200 Receipt` | `404` (payment not succeeded) |
| Export history | `GET /apartments/{id}/payments/export` | same | – | `200 text/csv` | – |

### UC8 – Pay maintenance online («extend» UC7)

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Start the payment | `POST /payments/online` | RESIDENT (own) | `{ billItemId }` (pays the whole amount) | `201 { paymentId, clientSecret, amount }` | `409 "This bill is already paid."` · `502` Stripe unavailable |
| Confirm the card | *Stripe SDK in the app:* `confirmPayment(clientSecret)` | – | card details go **directly to Stripe**, never to the Qvorum server | `succeeded` / `requires_action` (3-D Secure) | card declined |
| Confirm the payment | `POST /payments/{id}/confirm` | RESIDENT (own) | – | `200 Payment` (the server asks Stripe for the real status; on success the bill item becomes `PAID` and the receipt is issued) | `404` · `502` |

### UC9 – Take part in votes

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| List votes | `GET /votes?status=` | all · Assoc. | `status` optional | `200 Vote[]` | – |
| Vote | `POST /votes/{id}/ballots` | RESIDENT | `{ optionId }` (one ballot per apartment) | `201 Vote` | `409 "Your apartment has already voted."` · `409 "The vote is closed."` · `400` unknown option |
| Results | `GET /votes/{id}/results` | all · Assoc. | – | `200 VoteResults` | `404` |

### UC10 – Report an issue and track its status

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Report an issue | `POST /issues` (multipart) | RESIDENT | `category`, `description`, `photo` (optional file) | `201 IssueReport`; the administrator is notified | `400 "The description is required."` · `413` |
| My issues | `GET /issues` | RESIDENT (own apartment) | – | `200 IssueReport[]` | – |
| Issue details | `GET /issues/{id}` | RESIDENT (own) · ADMINISTRATOR · PRESIDENT · Assoc. | – | `200 IssueReport` | `403` · `404` |
| Status change | WebSocket notification `ISSUE_UPDATE` with `referenceId` = issue id | – | – | – | – |

### UC11 – Register administrator account and add associations

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Register | `POST /auth/register` | *public* | `{ administrator: { firstName, lastName, email, phone, password }, association: { name, address, fiscalCode, iban, dueDay, readingStartDay, readingEndDay, penaltyRate, graceDays, approvalThreshold, staircases: string[] } }` | `201 { message: "Check your email to activate the account." }` | `400` · `409 "This email is already registered."` · `409 "This fiscal code is already registered."` · `502` email failed |
| Confirm the email | `POST /auth/verify-email` | *public* | `{ token }` (from the email link) | `200 AuthResponse` | `400 "The link is invalid or has expired."` |
| My associations | `GET /associations` | ADMINISTRATOR | – | `200 Association[]` | – |
| Add another association | `POST /associations` | ADMINISTRATOR | same fields as `association` above | `201 Association` | `400` · `409` fiscal code |
| Edit an association and its settings | `PUT /associations/{id}` | ADMINISTRATOR (own) | same fields, `staircases` as `{ id \| null, name }[]` | `200 Association` | `400 "The penalty cannot exceed 0.2% per day."` · `403` · `409` staircase still has apartments |

### UC12 – Manage apartments and meters

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| List apartments | `GET /apartments` | ADMINISTRATOR · PRESIDENT · Assoc. | – | `200 Apartment[]` | – |
| Add an apartment | `POST /apartments` | ADMINISTRATOR · Assoc. | `{ staircaseId, number, floor, area, share, meters: { type, serialNumber }[] }` | `201 Apartment` | `400` · `409 "Apartment 12 already exists."` · `409` duplicate meter serial |
| Edit an apartment | `PUT /apartments/{id}` | ADMINISTRATOR · Assoc. | same, meters as `{ id \| null, type, serialNumber }[]` (missing meters are removed) | `200 Apartment` | `400` · `404` · `409` |
| Delete an apartment | `DELETE /apartments/{id}` | ADMINISTRATOR · Assoc. | – | `204` | `409 "The apartment has payments, bills or an account."` |

### UC13 – Manage user accounts

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| List accounts | `GET /users?role=` | ADMINISTRATOR · Assoc. | `role` optional | `200 User[]` | – |
| Create an account | `POST /users` | ADMINISTRATOR · Assoc. | `{ firstName, lastName, email, phone, role: "RESIDENT" \| "PRESIDENT", apartmentId \| null }` | `201 User`; a temporary password is emailed | `400` · `409 "This email is already used."` · `409 "The apartment already has an account."` · `409 "The association already has a president."` · `502` |
| Edit an account | `PUT /users/{id}` | ADMINISTRATOR · Assoc. | `{ firstName, lastName, phone, apartmentId \| null }` | `200 User` | `400` · `404` · `409` |
| Change email / reset password | `PUT /users/{id}/credentials` | ADMINISTRATOR · Assoc. | `{ newEmail \| null, resetPassword: boolean }` | `204`; email **and** SMS sent to the user; a reset sends a new temporary password; the user's sessions end | `409` email used · `502` notification failed (change undone) |
| Delete an account | `DELETE /users/{id}` | ADMINISTRATOR · Assoc. | – | `204` | `404` |

### UC14 – Manage announcements

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Publish | `POST /announcements` | ADMINISTRATOR · Assoc. | `{ title, content, type, eventDate \| null }` | `201 Announcement`; residents receive `NEW_ANNOUNCEMENT` | `400` |
| Edit | `PUT /announcements/{id}` | ADMINISTRATOR · Assoc. | same | `200 Announcement` | `400` · `404` |
| Delete | `DELETE /announcements/{id}` | ADMINISTRATOR · Assoc. | – | `204` | `404` |
| Read confirmations | `GET /announcements/{id}/reads` | ADMINISTRATOR · PRESIDENT · Assoc. | – | `200 AnnouncementReads` | `404` |

The list and details are the UC3 endpoints.

### UC15 – Create votes and view results

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Create a vote | `POST /votes` | ADMINISTRATOR · Assoc. | `{ title, description, options: string[], deadline, validationThreshold }` | `201 Vote`; residents receive `NEW_VOTE` | `400 "The deadline must be in the future."` · `400 "Add at least 2 options."` |
| Close a vote | `POST /votes/{id}/close` | ADMINISTRATOR · Assoc. | – | `200 Vote` (`CLOSED`); an `ApprovalRequest` of type `VOTE_RESULT` is created for the president | `409 "The vote is already closed."` |

The list and results are the UC9 endpoints.

### UC16 – Enter invoices and generate maintenance bill

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| List invoices | `GET /invoices?month=` | ADMINISTRATOR · PRESIDENT · Assoc. | `month` optional | `200 Invoice[]` | – |
| Add an invoice | `POST /invoices` | ADMINISTRATOR · Assoc. | `{ supplier, number, issueDate, month, category, amount, splitMethod }` | `201 Invoice` (`PENDING_APPROVAL` above the threshold, then an `EXPENSE` approval request is created) | `400` · `409 "The bill for this month is already published."` |
| Edit an invoice | `PUT /invoices/{id}` | ADMINISTRATOR · Assoc. | same | `200 Invoice` | `400` · `404` · `409` |
| Delete an invoice | `DELETE /invoices/{id}` | ADMINISTRATOR · Assoc. | – | `204` | `409` bill published |
| Generate the bill (preview) | `POST /bills` | ADMINISTRATOR · Assoc. | `{ month }` | `201 MaintenanceBill` (`DRAFT`; generating again replaces the draft) | `422 { message: "Some apartments have no meter readings.", apartments: ["4", "12"] }` · `409 "Some expenses are waiting for approval."` · `409 "This month is already published."` |
| View a month's bill | `GET /bills/{month}` | ADMINISTRATOR · PRESIDENT · Assoc. | – | `200 MaintenanceBill` | `404` |
| Publish | `POST /bills/{id}/publish` | ADMINISTRATOR · Assoc. | – | `200 MaintenanceBill`; residents receive `DUE_DATE` | `409 "The bill is already published."` |

### UC17 – Manage issue reports

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| List issues | `GET /issues?status=&category=` | ADMINISTRATOR · PRESIDENT · Assoc. | filters optional | `200 IssueReport[]` | – |
| Assign a company | `PUT /issues/{id}/assignment` | ADMINISTRATOR · Assoc. | `{ serviceCompanyId, note \| null }` | `200 IssueReport` (`IN_PROGRESS`); the company is emailed, the resident notified | `404` · `502 "The email to the company could not be sent."` |
| Change the status | `PUT /issues/{id}/status` | ADMINISTRATOR · Assoc. | `{ status, comment \| null }` | `200 IssueReport`; the resident receives `ISSUE_UPDATE` | `409 "This status change is not allowed."` (only `NEW → IN_PROGRESS → RESOLVED`) |
| List companies | `GET /service-companies` | ADMINISTRATOR · Assoc. | – | `200 ServiceCompany[]` | – |
| Add / edit / delete a company | `POST /service-companies` · `PUT /service-companies/{id}` · `DELETE /service-companies/{id}` | ADMINISTRATOR · Assoc. | `{ name, domain, email, phone }` | `201` / `200 ServiceCompany` / `204` | `400` · `409 "The company has issues in progress."` |

### UC18 – Manage administrative deadline calendar

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Deadlines in a period | `GET /deadlines?from=&to=` | ADMINISTRATOR · Assoc. | ISO dates | `200 Deadline[]` | – |
| Add | `POST /deadlines` | ADMINISTRATOR · Assoc. | `{ title, description \| null, dueDate, type, remindDaysBefore }` | `201 Deadline` | `400 "The date must be in the future."` |
| Edit / delete | `PUT /deadlines/{id}` · `DELETE /deadlines/{id}` | ADMINISTRATOR · Assoc. | same | `200 Deadline` / `204` | `400` · `404` |
| Reminder | WebSocket notification `DEADLINE`, `remindDaysBefore` days before `dueDate` | – | – | – | – |

### UC19 – View arrears, calculate penalties and record cash payments

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Arrears at a date | `GET /arrears?date=` | ADMINISTRATOR · PRESIDENT · Assoc. | `date` optional, default today | `200 ArrearsRow[]` | – |
| Details of an apartment | `GET /arrears/{apartmentId}?date=` | ADMINISTRATOR · PRESIDENT · Assoc. | – | `200 BillItem[]` (unpaid, with penalties at that date) | `404` |
| Record a cash payment | `POST /payments/cash` | ADMINISTRATOR · Assoc. | `{ billItemId, paidAt }` (pays the whole amount) | `201 Payment` with `receiptNumber` | `409 "This bill is already paid."` · `403` for the president |

Penalties are always calculated by the server: `amountDue × penaltyRate / 100 × days late after the grace period`, at most the amount due.

### UC20 – View statistics

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Consumption by month | `GET /statistics/consumption?from=&to=&meterType=` | ADMINISTRATOR · PRESIDENT · Assoc. | months `"2026-01"` … | `200 { month, value, unit }[]` | `400 "The period is invalid."` |
| Expense structure | `GET /statistics/expenses?from=&to=` | same | months | `200 { category, amount, percent }[]` | `400` |
| Collections vs arrears | `GET /statistics/collections?from=&to=` | same | months | `200 { month, collected, outstanding }[]` | `400` |

The server returns aggregated data; the charts are drawn by the client.

### UC21 – Export lists

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Maintenance bill | `GET /exports/bill?month=` | ADMINISTRATOR · Assoc. | – | `200 text/csv` | `404 "There is no bill for this month."` |
| Payments | `GET /exports/payments?from=&to=` | ADMINISTRATOR · Assoc. | ISO dates | `200 text/csv` | `400` |
| Arrears | `GET /exports/arrears?date=` | ADMINISTRATOR · Assoc. | ISO date | `200 text/csv` | – |

CSV files use `;` as separator, UTF-8 with BOM (so Excel shows diacritics), amounts in RON with two decimals.

### UC22 – Approve expenses above the threshold and vote results

| Action | Method and path | Roles | Request | Success | Errors |
| --- | --- | --- | --- | --- | --- |
| Pending requests | `GET /approvals?status=` | PRESIDENT · ADMINISTRATOR (read) · Assoc. | `status` optional, default `PENDING` | `200 ApprovalRequest[]` | – |
| Decide | `PUT /approvals/{id}` | PRESIDENT | `{ decision: "APPROVED" \| "REJECTED", reason \| null }` | `200 ApprovalRequest`; the invoice becomes `VALID`/`REJECTED`, or the vote `APPROVED`/`REJECTED` and residents are notified; the administrator receives `APPROVAL_RESULT` | `400 "A reason is required when rejecting."` · `409 "This request is already decided."` |

The president's read-only views are the `GET` endpoints of UC12, UC16, UC17, UC19 and UC20.

### UC23–UC25 – Server console (system administrator)

These are commands typed in the server's terminal, not HTTP endpoints. Their only visible effect on the clients is listed in the last column.

| Use case | Command | Output | Effect on the clients |
| --- | --- | --- | --- |
| UC23 | `status` | state (`RUNNING`/`STOPPED`), uptime, connected clients, requests served | – |
| UC23 | `stop` · `start` | confirmation, or "already stopped/running" | while stopped: every request → `503 "The server is temporarily stopped."`, WebSockets closed with `1001` |
| UC24 | `clients` | count and table: client id, user, role, app (`WEB`/`MOBILE`), IP address, connected since | – |
| UC24 | `disconnect <clientId>` | confirmation or "unknown client" | WebSocket closed with `4001`; the next request → `401 "Your session was closed by the administrator."` |
| UC25 | `history <from> <to> [clientId]` (`yyyy-mm-dd hh:mm`) | events: time, client, type (`CONNECT`/`DISCONNECT`/`REQUEST`), method, path, **response status**, duration | – |
| UC25 | `export <from> <to> [clientId] <file.csv>` | "N records saved to …" or the write error | – |

---

## 4. Endpoint summary

| Area | Count |
| --- | --- |
| Authentication and profile (UC1, UC2, UC11) | 12 |
| Apartments, meters, occupancy, accounts (UC5, UC6, UC12, UC13) | 14 |
| Announcements and notifications (UC3, UC4, UC14) | 10 |
| Votes and approvals (UC9, UC15, UC22) | 7 |
| Bills, invoices and payments (UC7, UC8, UC16, UC19) | 17 |
| Issues and service companies (UC10, UC17) | 9 |
| Calendar, statistics, exports (UC18, UC20, UC21) | 10 |
| **Total** | **79 HTTP endpoints**, 1 WebSocket, 6 console commands |
