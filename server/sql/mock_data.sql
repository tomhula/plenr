INSERT INTO user (first_name, last_name, email, phone, password_hash, is_admin) VALUES
                                                                                    ('John', 'Doe', 'john.doe@example.com', '+1234567890', 0x5E884898DA28047151D0E56F8DC6292773603D0D6AABBDD62A11EF721D1542D8, 1),
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
                                                                                                 (1, 'Yoga Basics', 'Introduction to yoga.', 1, '2025-03-01 08:00:00.000000', 60),
                                                                                                 (2, 'Advanced Pilates', 'High-intensity pilates.', 2, '2025-03-02 10:00:00.000000', 75),
                                                                                                 (3, 'Strength Training', 'Full-body strength workout.', 3, '2025-03-03 09:00:00.000000', 90),
                                                                                                 (4, 'Cardio Blast', 'Intense cardio session.', 4, '2025-03-04 07:00:00.000000', 45),
                                                                                                 (5, 'HIIT Workout', 'High-intensity interval training.', 5, '2025-03-05 18:00:00.000000', 30),
                                                                                                 (6, 'CrossFit Challenge', 'Advanced crossfit workout.', 3, '2025-03-06 12:00:00.000000', 60),
                                                                                                 (7, 'Spin Class', 'High-energy spin session.', 4, '2025-03-07 17:00:00.000000', 45),
                                                                                                 (8, 'Functional Fitness', 'Practical strength and agility.', 2, '2025-03-08 14:00:00.000000', 60),
                                                                                                 (9, 'Zumba Dance', 'Dance your way to fitness.', 1, '2025-03-09 11:00:00.000000', 60),
                                                                                                 (10, 'Bootcamp', 'Military-style training.', 5, '2025-03-10 06:00:00.000000', 60),
                                                                                                 (1, 'Core Strength', 'Core and stability workout.', 3, '2025-03-11 09:30:00.000000', 45),
                                                                                                 (2, 'Stretch and Relax', 'Flexibility and relaxation.', 1, '2025-03-12 19:00:00.000000', 60),
                                                                                                 (3, 'Kickboxing Basics', 'Martial arts-inspired workout.', 4, '2025-03-13 18:00:00.000000', 60),
                                                                                                 (4, 'Aqua Aerobics', 'Low-impact water workout.', 2, '2025-03-14 10:00:00.000000', 45),
                                                                                                 (5, 'Trail Running', 'Outdoor endurance run.', 5, '2025-03-15 08:00:00.000000', 120),
                                                                                                 (6, 'Dance Cardio', 'Energetic dance workout.', 1, '2025-03-16 16:00:00.000000', 45),
                                                                                                 (7, 'Powerlifting', 'Heavy lifting session.', 3, '2025-03-17 14:00:00.000000', 90),
                                                                                                 (8, 'Mobility Training', 'Improve joint mobility.', 2, '2025-03-18 12:00:00.000000', 60),
                                                                                                 (9, 'Boxing Drills', 'Speed and power boxing.', 4, '2025-03-19 17:30:00.000000', 60),
                                                                                                 (10, 'Marathon Prep', 'Long-distance running prep.', 5, '2025-03-20 06:30:00.000000', 180);

INSERT INTO training_participant (training_id, participant_id) VALUES
                                                                   (1, 2), (1, 3), (2, 4), (2, 5), (3, 1), (3, 6), (4, 7), (4, 8), (5, 9), (5, 10),
                                                                   (6, 2), (6, 3), (7, 4), (7, 5), (8, 6), (8, 7), (9, 8), (9, 9), (10, 10), (10, 1),
                                                                   (11, 3), (11, 4), (12, 5), (12, 6), (13, 7), (13, 8), (14, 9), (14, 10), (15, 1), (15, 2),
                                                                   (16, 3), (16, 4), (17, 5), (17, 6), (18, 7), (18, 8), (19, 9), (19, 10), (20, 1), (20, 2);
