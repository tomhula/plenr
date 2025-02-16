INSERT INTO user (first_name, last_name, email, phone, password_hash, is_admin) VALUES
                                                                                    ('Tomáš', 'Hůla', 'tomashula@example.com', '+420 123 456 789', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 1),
                                                                                    ('John', 'Doe', 'john.doe@example.com', '+1234567890', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Jane', 'Smith', 'jane.smith@example.com', '+1234567891', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Alice', 'Johnson', 'alice.johnson@example.com', '+1234567892', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Bob', 'Brown', 'bob.brown@example.com', '+1234567893', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Charlie', 'Davis', 'charlie.davis@example.com', '+1234567894', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('David', 'Wilson', 'david.wilson@example.com', '+1234567895', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Eva', 'Martinez', 'eva.martinez@example.com', '+1234567896', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Frank', 'Taylor', 'frank.taylor@example.com', '+1234567897', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Grace', 'Anderson', 'grace.anderson@example.com', '+1234567898', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0),
                                                                                    ('Henry', 'Thomas', 'henry.thomas@example.com', '+1234567899', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 0);

INSERT INTO training (arranger_id, name, description, type, start_date_time, length_minutes) VALUES
                                                                                                 (1, 'Yoga Basics', 'Introduction to yoga.', 1, '2025-02-14 08:00:00.000000', 60),
                                                                                                 (1, 'Advanced Pilates', 'High-intensity pilates.', 0, '2025-02-15 10:00:00.000000', 75),
                                                                                                 (1, 'Strength Training', 'Full-body strength workout.', 1, '2025-02-16 09:00:00.000000', 90),
                                                                                                 (1, 'Cardio Blast', 'Intense cardio session.', 0, '2025-02-17 07:00:00.000000', 45),
                                                                                                 (1, 'HIIT Workout', 'High-intensity interval training.', 1, '2025-02-18 18:00:00.000000', 30),
                                                                                                 (1, 'CrossFit Challenge', 'Advanced crossfit workout.', 0, '2025-02-19 12:00:00.000000', 60),
                                                                                                 (1, 'Spin Class', 'High-energy spin session.', 1, '2025-02-20 17:00:00.000000', 45),
                                                                                                 (1, 'Functional Fitness', 'Practical strength and agility.', 0, '2025-02-21 14:00:00.000000', 60),
                                                                                                 (1, 'Zumba Dance', 'Dance your way to fitness.', 1, '2025-02-22 11:00:00.000000', 60),
                                                                                                 (1, 'Bootcamp', 'Military-style training.', 0, '2025-02-23 06:00:00.000000', 60),
                                                                                                 (1, 'Core Strength', 'Core and stability workout.', 1, '2025-02-24 09:30:00.000000', 45),
                                                                                                 (1, 'Stretch and Relax', 'Flexibility and relaxation.', 0, '2025-02-25 19:00:00.000000', 60),
                                                                                                 (1, 'Kickboxing Basics', 'Martial arts-inspired workout.', 1, '2025-02-26 18:00:00.000000', 60),
                                                                                                 (1, 'Aqua Aerobics', 'Low-impact water workout.', 0, '2025-02-27 10:00:00.000000', 45),
                                                                                                 (1, 'Trail Running', 'Outdoor endurance run.', 1, '2025-02-28 08:00:00.000000', 120),
                                                                                                 (1, 'Dance Cardio', 'Energetic dance workout.', 0, '2025-03-01 16:00:00.000000', 45);


INSERT INTO training_participant (training_id, participant_id) VALUES
                                                                   (1, 2), (1, 3), (2, 4), (2, 5), (3, 1), (3, 6), (4, 7), (4, 8), (5, 9), (5, 10),
                                                                   (6, 2), (6, 3), (7, 4), (7, 5), (8, 6), (8, 7), (9, 8), (9, 9), (10, 10), (10, 1),
                                                                   (11, 3), (11, 4), (12, 5), (12, 6), (13, 7), (13, 8), (14, 9), (14, 10), (15, 1), (15, 2),
                                                                   (16, 3), (16, 4);
