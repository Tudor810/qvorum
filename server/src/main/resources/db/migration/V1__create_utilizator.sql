-- Flyway migration: runs once, in version order, on server start-up.
-- Never edit a migration that has already run; add a new V<n>__description.sql instead.

CREATE TABLE utilizator (
    id          BIGSERIAL    PRIMARY KEY,
    nume        VARCHAR(100) NOT NULL,
    prenume     VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    parola_hash VARCHAR(100) NOT NULL
);

CREATE TABLE utilizator_rol (
    utilizator_id BIGINT      NOT NULL REFERENCES utilizator (id) ON DELETE CASCADE,
    rol           VARCHAR(20) NOT NULL,
    PRIMARY KEY (utilizator_id, rol)
);
