-- Development data: one association run by admin@qvorum.test, with two staircases, four
-- apartments and their meters. The resident and the president from V2 live in apartments 1 and 2.

INSERT INTO associations (administrator_id, name, address, fiscal_code, iban, due_day,
                          reading_start_day, reading_end_day, penalty_rate, grace_days,
                          approval_threshold)
SELECT id, 'Asociatia de Proprietari Bloc M3', 'Str. Observatorului 12, Cluj-Napoca', 'RO12345678',
       'RO49AAAA1B31007593840000', 20, 1, 5, 0.200, 30, 500000
FROM users WHERE email = 'admin@qvorum.test';

INSERT INTO staircases (association_id, name)
SELECT a.id, s.name
FROM associations a, (VALUES ('A'), ('B')) AS s(name)
WHERE a.fiscal_code = 'RO12345678';

INSERT INTO apartments (association_id, staircase_id, number, floor, area, share)
SELECT a.id, s.id, ap.number, ap.floor, ap.area, ap.share
FROM associations a
JOIN staircases s ON s.association_id = a.id
JOIN (VALUES ('1', 'A', 0, 54.20, 0.2600),
             ('2', 'A', 1, 54.20, 0.2600),
             ('3', 'B', 0, 41.50, 0.2400),
             ('4', 'B', 1, 41.50, 0.2400)) AS ap(number, staircase, floor, area, share)
  ON ap.staircase = s.name
WHERE a.fiscal_code = 'RO12345678';

INSERT INTO meters (apartment_id, type, serial_number)
SELECT ap.id, m.type, 'M3-' || ap.number || '-' || m.suffix
FROM apartments ap,
     (VALUES ('COLD_WATER', 'CW'), ('HOT_WATER', 'HW'), ('GAS', 'GAS')) AS m(type, suffix);

UPDATE users u
SET association_id = a.id,
    apartment_id = ap.id
FROM associations a
JOIN apartments ap ON ap.association_id = a.id
WHERE a.fiscal_code = 'RO12345678'
  AND ((u.email = 'resident@qvorum.test' AND ap.number = '1')
    OR (u.email = 'president@qvorum.test' AND ap.number = '2'));
