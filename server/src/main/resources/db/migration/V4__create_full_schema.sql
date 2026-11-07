-- Creates every table of the ERD (docs/Qvorum_ERD.drawio) that does not exist yet, and adds the
-- missing columns to users. Money columns are BIGINT amounts in bani (1 RON = 100 bani).
-- Status and type columns are VARCHAR; the allowed values are Java enums in the model package.

-- ---------------------------------------------------------------------------------------------
-- Associations and building structure
-- ---------------------------------------------------------------------------------------------

CREATE TABLE associations (
    id                 BIGSERIAL     PRIMARY KEY,
    administrator_id   BIGINT        NOT NULL REFERENCES users (id),
    name               VARCHAR(150)  NOT NULL,
    address            VARCHAR(255)  NOT NULL,
    fiscal_code        VARCHAR(20)   NOT NULL UNIQUE,
    iban               VARCHAR(34)   NOT NULL,
    due_day            INT           NOT NULL CHECK (due_day BETWEEN 1 AND 28),
    reading_start_day  INT           NOT NULL CHECK (reading_start_day BETWEEN 1 AND 31),
    reading_end_day    INT           NOT NULL CHECK (reading_end_day BETWEEN 1 AND 31),
    penalty_rate       NUMERIC(5, 3) NOT NULL CHECK (penalty_rate >= 0),
    grace_days         INT           NOT NULL DEFAULT 0 CHECK (grace_days >= 0),
    approval_threshold BIGINT        NOT NULL CHECK (approval_threshold >= 0),
    created_at         TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE staircases (
    id             BIGSERIAL   PRIMARY KEY,
    association_id BIGINT      NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    name           VARCHAR(20) NOT NULL,
    UNIQUE (association_id, name)
);

CREATE TABLE apartments (
    id             BIGSERIAL     PRIMARY KEY,
    association_id BIGINT        NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    staircase_id   BIGINT        NOT NULL REFERENCES staircases (id),
    number         VARCHAR(10)   NOT NULL,
    floor          INT           NOT NULL,
    area           NUMERIC(7, 2) NOT NULL CHECK (area > 0),
    share          NUMERIC(7, 4) NOT NULL CHECK (share >= 0),
    UNIQUE (association_id, number)
);

CREATE TABLE meters (
    id            BIGSERIAL   PRIMARY KEY,
    apartment_id  BIGINT      NOT NULL REFERENCES apartments (id) ON DELETE CASCADE,
    type          VARCHAR(20) NOT NULL,
    serial_number VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE meter_readings (
    id           BIGSERIAL      PRIMARY KEY,
    meter_id     BIGINT         NOT NULL REFERENCES meters (id) ON DELETE CASCADE,
    month        DATE           NOT NULL,
    value        NUMERIC(12, 3) NOT NULL CHECK (value >= 0),
    consumption  NUMERIC(12, 3) NOT NULL CHECK (consumption >= 0),
    photo_path   VARCHAR(255),
    submitted_at TIMESTAMP      NOT NULL DEFAULT NOW(),
    UNIQUE (meter_id, month)
);

CREATE TABLE occupancy_declarations (
    id           BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT    NOT NULL REFERENCES apartments (id) ON DELETE CASCADE,
    occupants    INT       NOT NULL CHECK (occupants >= 0),
    valid_from   DATE      NOT NULL,
    declared_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------------------------
-- Users: link residents and the president to their association and apartment
-- ---------------------------------------------------------------------------------------------

-- Administrators have no association_id: they own associations through associations.administrator_id.
ALTER TABLE users
    ADD COLUMN association_id          BIGINT       REFERENCES associations (id) ON DELETE CASCADE,
    ADD COLUMN apartment_id            BIGINT       UNIQUE REFERENCES apartments (id) ON DELETE SET NULL,
    ADD COLUMN phone                   VARCHAR(20),
    ADD COLUMN must_change_password    BOOLEAN      NOT NULL DEFAULT TRUE,
    ADD COLUMN email_verified          BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN verification_token_hash VARCHAR(100),
    ADD COLUMN created_at              TIMESTAMP    NOT NULL DEFAULT NOW();

-- The test accounts from V2 already have a chosen password and a working email.
UPDATE users SET must_change_password = FALSE, email_verified = TRUE;

CREATE TABLE notifications (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type         VARCHAR(30)  NOT NULL,
    title        VARCHAR(150) NOT NULL,
    message      TEXT         NOT NULL,
    reference_id BIGINT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    read_at      TIMESTAMP
);
CREATE INDEX notifications_user_idx ON notifications (user_id, created_at DESC);

-- ---------------------------------------------------------------------------------------------
-- Announcements and deadlines
-- ---------------------------------------------------------------------------------------------

CREATE TABLE announcements (
    id             BIGSERIAL    PRIMARY KEY,
    association_id BIGINT       NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    title          VARCHAR(150) NOT NULL,
    content        TEXT         NOT NULL,
    type           VARCHAR(20)  NOT NULL,
    event_date     DATE,
    published_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE announcement_reads (
    announcement_id BIGINT    NOT NULL REFERENCES announcements (id) ON DELETE CASCADE,
    user_id         BIGINT    NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    read_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (announcement_id, user_id)
);

CREATE TABLE deadlines (
    id                 BIGSERIAL    PRIMARY KEY,
    association_id     BIGINT       NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    title              VARCHAR(150) NOT NULL,
    description        TEXT,
    due_date           DATE         NOT NULL,
    type               VARCHAR(20)  NOT NULL,
    remind_days_before INT          NOT NULL DEFAULT 3 CHECK (remind_days_before >= 0),
    reminded_at        TIMESTAMP
);

-- ---------------------------------------------------------------------------------------------
-- Issue reports
-- ---------------------------------------------------------------------------------------------

CREATE TABLE service_companies (
    id             BIGSERIAL    PRIMARY KEY,
    association_id BIGINT       NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    name           VARCHAR(150) NOT NULL,
    domain         VARCHAR(20)  NOT NULL,
    email          VARCHAR(255) NOT NULL,
    phone          VARCHAR(20)
);

CREATE TABLE issue_reports (
    id                 BIGSERIAL    PRIMARY KEY,
    association_id     BIGINT       NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    apartment_id       BIGINT       NOT NULL REFERENCES apartments (id) ON DELETE CASCADE,
    service_company_id BIGINT       REFERENCES service_companies (id) ON DELETE SET NULL,
    category           VARCHAR(20)  NOT NULL,
    description        TEXT         NOT NULL,
    photo_path         VARCHAR(255),
    status             VARCHAR(20)  NOT NULL DEFAULT 'NEW',
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE issue_status_changes (
    id         BIGSERIAL   PRIMARY KEY,
    issue_id   BIGINT      NOT NULL REFERENCES issue_reports (id) ON DELETE CASCADE,
    status     VARCHAR(20) NOT NULL,
    comment    TEXT,
    changed_at TIMESTAMP   NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------------------------------------
-- Invoices, maintenance bills and payments
-- ---------------------------------------------------------------------------------------------

CREATE TABLE invoices (
    id             BIGSERIAL    PRIMARY KEY,
    association_id BIGINT       NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    supplier       VARCHAR(150) NOT NULL,
    number         VARCHAR(50)  NOT NULL,
    issue_date     DATE         NOT NULL,
    month          DATE         NOT NULL,
    category       VARCHAR(30)  NOT NULL,
    amount         BIGINT       NOT NULL CHECK (amount > 0),
    split_method   VARCHAR(20)  NOT NULL,
    status         VARCHAR(20)  NOT NULL
);

CREATE TABLE maintenance_bills (
    id             BIGSERIAL   PRIMARY KEY,
    association_id BIGINT      NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    month          DATE        NOT NULL,
    status         VARCHAR(20) NOT NULL,
    due_date       DATE        NOT NULL,
    published_at   TIMESTAMP,
    UNIQUE (association_id, month)
);

CREATE TABLE bill_items (
    id           BIGSERIAL   PRIMARY KEY,
    bill_id      BIGINT      NOT NULL REFERENCES maintenance_bills (id) ON DELETE CASCADE,
    apartment_id BIGINT      NOT NULL REFERENCES apartments (id),
    arrears      BIGINT      NOT NULL DEFAULT 0,
    penalties    BIGINT      NOT NULL DEFAULT 0,
    total        BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL,
    UNIQUE (bill_id, apartment_id)
);

CREATE TABLE bill_lines (
    id           BIGSERIAL PRIMARY KEY,
    bill_item_id BIGINT    NOT NULL REFERENCES bill_items (id) ON DELETE CASCADE,
    invoice_id   BIGINT    NOT NULL REFERENCES invoices (id),
    amount       BIGINT    NOT NULL
);

CREATE TABLE payments (
    id                BIGSERIAL    PRIMARY KEY,
    bill_item_id      BIGINT       NOT NULL REFERENCES bill_items (id),
    amount            BIGINT       NOT NULL CHECK (amount > 0),
    method            VARCHAR(10)  NOT NULL,
    status            VARCHAR(20)  NOT NULL,
    stripe_payment_id VARCHAR(100),
    receipt_number    VARCHAR(30)  UNIQUE,
    paid_at           TIMESTAMP
);

-- ---------------------------------------------------------------------------------------------
-- Votes and approvals
-- ---------------------------------------------------------------------------------------------

CREATE TABLE votes (
    id                   BIGSERIAL    PRIMARY KEY,
    association_id       BIGINT       NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    title                VARCHAR(150) NOT NULL,
    description          TEXT,
    deadline             TIMESTAMP    NOT NULL,
    validation_threshold INT          NOT NULL CHECK (validation_threshold BETWEEN 1 AND 100),
    status               VARCHAR(20)  NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE vote_options (
    id      BIGSERIAL    PRIMARY KEY,
    vote_id BIGINT       NOT NULL REFERENCES votes (id) ON DELETE CASCADE,
    text    VARCHAR(150) NOT NULL
);

-- One vote per apartment.
CREATE TABLE ballots (
    id           BIGSERIAL PRIMARY KEY,
    vote_id      BIGINT    NOT NULL REFERENCES votes (id) ON DELETE CASCADE,
    option_id    BIGINT    NOT NULL REFERENCES vote_options (id) ON DELETE CASCADE,
    apartment_id BIGINT    NOT NULL REFERENCES apartments (id) ON DELETE CASCADE,
    cast_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (vote_id, apartment_id)
);

-- An approval request is about either an invoice (expense above the threshold) or a vote result.
CREATE TABLE approval_requests (
    id             BIGSERIAL   PRIMARY KEY,
    association_id BIGINT      NOT NULL REFERENCES associations (id) ON DELETE CASCADE,
    type           VARCHAR(20) NOT NULL,
    invoice_id     BIGINT      REFERENCES invoices (id) ON DELETE CASCADE,
    vote_id        BIGINT      REFERENCES votes (id) ON DELETE CASCADE,
    status         VARCHAR(20) NOT NULL,
    reason         TEXT,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    decided_at     TIMESTAMP,
    CHECK ((invoice_id IS NULL) <> (vote_id IS NULL))
);

-- ---------------------------------------------------------------------------------------------
-- Monitoring (UC24, UC25). No foreign keys, so the history survives when an account is deleted.
-- ---------------------------------------------------------------------------------------------

CREATE TABLE request_logs (
    id          BIGSERIAL    PRIMARY KEY,
    client_id   VARCHAR(50),
    user_id     BIGINT,
    event_type  VARCHAR(20)  NOT NULL,
    method      VARCHAR(10),
    path        VARCHAR(255),
    status      INT,
    duration_ms INT,
    occurred_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX request_logs_occurred_at_idx ON request_logs (occurred_at);
