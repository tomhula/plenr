-- Disable foreign key checks to avoid issues while truncating tables
SET FOREIGN_KEY_CHECKS = 0;

-- Truncate all tables to clear data
TRUNCATE TABLE permanent_busy_time;
TRUNCATE TABLE temp_busy_time;
TRUNCATE TABLE training_participant;
TRUNCATE TABLE training;
TRUNCATE TABLE user_activation;
TRUNCATE TABLE user_preferences;
TRUNCATE TABLE user;

-- Enable foreign key checks back
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO user (first_name, last_name, email, phone, password_hash, is_admin)
VALUES ('Tomáš', 'Hůla', 'tomas.hula@example.com', '+420 123 456 789',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 1),
       ('John', 'Doe', 'john.doe@example.com', '+1234567890',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Jane', 'Smith', 'jane.smith@example.com', '+1234567891',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Alice', 'Johnson', 'alice.johnson@example.com', '+1234567892',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Bob', 'Brown', 'bob.brown@example.com', '+1234567893',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Charlie', 'Davis', 'charlie.davis@example.com', '+1234567894',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('David', 'Wilson', 'david.wilson@example.com', '+1234567895',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Eva', 'Martinez', 'eva.martinez@example.com', '+1234567896',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Frank', 'Taylor', 'frank.taylor@example.com', '+1234567897',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Grace', 'Anderson', 'grace.anderson@example.com', '+1234567898',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
       ('Henry', 'Thomas', 'henry.thomas@example.com', '+1234567899',
        0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0);

INSERT INTO training (arranger_id, name, description, type, start_date_time, length_minutes)
VALUES (1, 'Yoga Basics', 'Introduction to yoga.', 1,
        DATE_ADD(CURDATE(), INTERVAL 1 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Advanced Pilates', 'High-intensity pilates.', 0,
        DATE_ADD(CURDATE(), INTERVAL 2 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 75),
       (1, 'Strength Training', 'Full-body strength workout.', 1,
        DATE_ADD(CURDATE(), INTERVAL 3 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 90),
       (1, 'Cardio Blast', 'Intense cardio session.', 0,
        DATE_ADD(CURDATE(), INTERVAL 4 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 45),
       (1, 'HIIT Workout', 'High-intensity interval training.', 1,
        DATE_ADD(CURDATE(), INTERVAL 5 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 30),
       (1, 'CrossFit Challenge', 'Advanced crossfit workout.', 0,
        DATE_ADD(CURDATE(), INTERVAL 6 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Spin Class', 'High-energy spin session.', 1,
        DATE_ADD(CURDATE(), INTERVAL 7 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 45),
       (1, 'Functional Fitness', 'Practical strength and agility.', 0,
        DATE_ADD(CURDATE(), INTERVAL 8 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Zumba Dance', 'Dance your way to fitness.', 1,
        DATE_ADD(CURDATE(), INTERVAL 9 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Bootcamp', 'Military-style training.', 0,
        DATE_ADD(CURDATE(), INTERVAL 10 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Core Strength', 'Core and stability workout.', 1,
        DATE_ADD(CURDATE(), INTERVAL 11 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 45),
       (1, 'Stretch and Relax', 'Flexibility and relaxation.', 0,
        DATE_ADD(CURDATE(), INTERVAL 12 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Kickboxing Basics', 'Martial arts-inspired workout.', 1,
        DATE_ADD(CURDATE(), INTERVAL 13 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 60),
       (1, 'Aqua Aerobics', 'Low-impact water workout.', 0,
        DATE_ADD(CURDATE(), INTERVAL 14 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 45),
       (1, 'Trail Running', 'Outdoor endurance run.', 1,
        DATE_ADD(CURDATE(), INTERVAL 15 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 120),
       (1, 'Dance Cardio', 'Energetic dance workout.', 0,
        DATE_ADD(CURDATE(), INTERVAL 16 DAY) + INTERVAL FLOOR(RAND() * 24) HOUR, 45);


INSERT INTO training_participant (training_id, participant_id)
VALUES (1, 2),
       (1, 3),
       (2, 4),
       (2, 5),
       (3, 1),
       (3, 6),
       (4, 7),
       (4, 8),
       (5, 9),
       (5, 10),
       (6, 2),
       (6, 3),
       (7, 4),
       (7, 5),
       (8, 6),
       (8, 7),
       (9, 8),
       (9, 9),
       (10, 10),
       (10, 1),
       (11, 3),
       (11, 4),
       (12, 5),
       (12, 6),
       (13, 7),
       (13, 8),
       (14, 9),
       (14, 10),
       (15, 1),
       (15, 2),
       (16, 3),
       (16, 4);

INSERT INTO permanent_busy_time (user_id, day, start, end)
VALUES
    -- User 1
    (1, 0, '08:00:00', '16:00:00'),
    (1, 1, '09:00:00', '17:00:00'),
    (1, 2, '07:00:00', '15:00:00'),
    (1, 3, '10:00:00', '18:00:00'),
    (1, 4, '12:00:00', '20:00:00'),
    (1, 5, '11:00:00', '19:00:00'),
    (1, 6, '06:00:00', '14:00:00'),
    -- User 2
    (2, 0, '10:00:00', '14:00:00'),
    (2, 1, '13:00:00', '19:00:00'),
    (2, 2, '08:30:00', '15:30:00'),
    (2, 3, '09:15:00', '17:15:00'),
    (2, 4, '07:30:00', '13:30:00'),
    (2, 5, '14:00:00', '18:00:00'),
    (2, 6, '09:00:00', '12:00:00'),
    -- User 3
    (3, 0, '07:45:00', '16:45:00'),
    (3, 1, '08:15:00', '17:15:00'),
    (3, 2, '06:30:00', '12:30:00'),
    (3, 3, '13:00:00', '18:00:00'),
    (3, 4, '08:00:00', '16:00:00'),
    (3, 5, '11:00:00', '19:00:00'),
    (3, 6, '10:00:00', '14:00:00'),
    -- User 4
    (4, 0, '12:00:00', '20:00:00'),
    (4, 1, '07:30:00', '11:30:00'),
    (4, 2, '09:00:00', '17:00:00'),
    (4, 3, '10:00:00', '18:00:00'),
    (4, 4, '06:00:00', '14:00:00'),
    (4, 5, '13:00:00', '19:00:00'),
    (4, 6, '08:00:00', '12:00:00'),
    -- User 5
    (5, 0, '07:00:00', '13:00:00'),
    (5, 1, '09:00:00', '15:00:00'),
    (5, 2, '08:15:00', '14:15:00'),
    (5, 3, '10:30:00', '18:30:00'),
    (5, 4, '11:00:00', '19:00:00'),
    (5, 5, '12:00:00', '20:00:00'),
    (5, 6, '06:30:00', '12:30:00'),
    -- User 6
    (6, 0, '13:00:00', '18:00:00'),
    (6, 1, '08:00:00', '14:00:00'),
    (6, 2, '07:45:00', '15:45:00'),
    (6, 3, '09:30:00', '17:30:00'),
    (6, 4, '11:30:00', '19:30:00'),
    (6, 5, '06:00:00', '12:00:00'),
    (6, 6, '10:00:00', '14:00:00'),
    -- User 7
    (7, 0, '06:30:00', '14:30:00'),
    (7, 1, '09:00:00', '17:00:00'),
    (7, 2, '08:30:00', '16:30:00'),
    (7, 3, '10:00:00', '14:00:00'),
    (7, 4, '13:00:00', '18:00:00'),
    (7, 5, '12:00:00', '20:00:00'),
    (7, 6, '07:00:00', '15:00:00'),
    -- User 8
    (8, 0, '11:00:00', '16:00:00'),
    (8, 1, '07:30:00', '13:30:00'),
    (8, 2, '08:15:00', '14:15:00'),
    (8, 3, '09:45:00', '17:45:00'),
    (8, 4, '13:00:00', '19:00:00'),
    (8, 5, '07:00:00', '12:00:00'),
    (8, 6, '06:30:00', '14:30:00'),
    -- User 9
    (9, 0, '08:45:00', '15:45:00'),
    (9, 1, '10:00:00', '18:00:00'),
    (9, 2, '11:30:00', '19:30:00'),
    (9, 3, '07:15:00', '13:15:00'),
    (9, 4, '08:00:00', '16:00:00'),
    (9, 5, '09:00:00', '15:00:00'),
    (9, 6, '13:30:00', '18:30:00'),
    -- User 10
    (10, 0, '10:00:00', '14:00:00'),
    (10, 1, '07:45:00', '13:45:00'),
    (10, 2, '09:15:00', '15:15:00'),
    (10, 3, '08:30:00', '16:30:00'),
    (10, 4, '11:00:00', '19:00:00'),
    (10, 5, '06:30:00', '12:30:00'),
    (10, 6, '12:15:00', '18:15:00');
