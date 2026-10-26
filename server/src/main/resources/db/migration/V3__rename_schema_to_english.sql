-- Renames the schema from V1/V2 to English. V1 and V2 are left untouched because they may
-- already have run; Flyway rejects edits to applied migrations.

ALTER TABLE utilizator RENAME TO users;
ALTER TABLE users RENAME COLUMN nume TO last_name;
ALTER TABLE users RENAME COLUMN prenume TO first_name;
ALTER TABLE users RENAME COLUMN parola_hash TO password_hash;
ALTER TABLE users RENAME CONSTRAINT utilizator_pkey TO users_pkey;
ALTER TABLE users RENAME CONSTRAINT utilizator_email_key TO users_email_key;
ALTER SEQUENCE utilizator_id_seq RENAME TO users_id_seq;

ALTER TABLE utilizator_rol RENAME TO user_roles;
ALTER TABLE user_roles RENAME COLUMN utilizator_id TO user_id;
ALTER TABLE user_roles RENAME COLUMN rol TO role;
ALTER TABLE user_roles RENAME CONSTRAINT utilizator_rol_pkey TO user_roles_pkey;
ALTER TABLE user_roles RENAME CONSTRAINT utilizator_rol_utilizator_id_fkey TO user_roles_user_id_fkey;

UPDATE user_roles SET role = 'RESIDENT' WHERE role = 'LOCATAR';
UPDATE user_roles SET role = 'PRESIDENT' WHERE role = 'PRESEDINTE';

-- Test accounts from V2 (same password, Test1234!).
UPDATE users SET email = 'resident@qvorum.test' WHERE email = 'locatar@qvorum.test';
UPDATE users SET email = 'president@qvorum.test' WHERE email = 'presedinte@qvorum.test';
