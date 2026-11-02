-- Test accounts for development, one per role. All use the password: Test1234!
-- (stored as a BCrypt hash; generate new hashes with BCryptPasswordEncoder, never store plain text).

INSERT INTO utilizator (nume, prenume, email, parola_hash) VALUES
    ('Popescu', 'Ana',   'locatar@qvorum.test',  '$2a$10$PWik5VoO4QDcmO7oEvSUQe9/JsKgXt0H4REOFTrRu2vCbr95DT7PG'),
    ('Ionescu', 'Mihai', 'admin@qvorum.test',    '$2a$10$PWik5VoO4QDcmO7oEvSUQe9/JsKgXt0H4REOFTrRu2vCbr95DT7PG'),
    ('Rusu',    'Elena', 'presedinte@qvorum.test', '$2a$10$PWik5VoO4QDcmO7oEvSUQe9/JsKgXt0H4REOFTrRu2vCbr95DT7PG');

INSERT INTO utilizator_rol (utilizator_id, rol)
SELECT id, 'LOCATAR' FROM utilizator WHERE email = 'locatar@qvorum.test'
UNION ALL
SELECT id, 'ADMINISTRATOR' FROM utilizator WHERE email = 'admin@qvorum.test'
UNION ALL
-- The president also lives in the building, so the account has both roles.
SELECT id, 'PRESEDINTE' FROM utilizator WHERE email = 'presedinte@qvorum.test'
UNION ALL
SELECT id, 'LOCATAR' FROM utilizator WHERE email = 'presedinte@qvorum.test';
