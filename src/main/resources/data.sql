-- GYM CLASSES
INSERT INTO gym_class (name, instructor, description, day_of_week, start_time, duration_minutes, max_participants)
VALUES ('Morning Yoga', 'Anna Svensson', 'Calm yoga session to start your day', 'Monday', '08:00', 60, 12),

       ('HIIT Express', 'Johan Eriksson', 'High intensity interval training', 'Monday', '18:00', 30, 15),

       ('Strength Training', 'Erik Larsson', 'Full body strength workout', 'Tuesday', '17:30', 45, 10),

       ('Cycling Class', 'Sofia Nilsson', 'Indoor cycling with music', 'Wednesday', '18:30', 50, 14),

       ('Functional Fitness', 'Lina Karlsson', 'Mobility and functional strength', 'Thursday', '19:00', 45, 12),

       ('Pilates Core', 'Maria Andersson', 'Core strength and posture', 'Friday', '09:00', 50, 10),

       ('Zumba Dance', 'Sara Johansson', 'Fun dance workout', 'Saturday', '11:00', 60, 20),

       ('Boxing Basics', 'Daniel Berg', 'Intro to boxing techniques', 'Saturday', '13:00', 45, 12),

       ('Stretch & Mobility', 'Emma Lind', 'Relaxing stretching session', 'Sunday', '10:00', 40, 8),

       ('Evening Yoga Flow', 'Anna Svensson', 'Slow yoga flow for relaxation', 'Sunday', '18:00', 60, 12);

-- BOOKINGS
INSERT INTO booking (participant_name, email, booked_at, gym_class_id)
VALUES ('Emma Johansson', 'emma.j@example.com', CURRENT_TIMESTAMP, 1),
       ('Lucas Andersson', 'lucas.a@example.com', CURRENT_TIMESTAMP, 1),
       ('Maja Lindberg', 'maja.l@example.com', CURRENT_TIMESTAMP, 1),

       ('Oscar Berg', 'oscar.b@example.com', CURRENT_TIMESTAMP, 2),
       ('Ella Holm', 'ella.h@example.com', CURRENT_TIMESTAMP, 2),
       ('Noah Sjöberg', 'noah.s@example.com', CURRENT_TIMESTAMP, 2),

       ('Alice Karlsson', 'alice.k@example.com', CURRENT_TIMESTAMP, 3),
       ('Bob Eriksson', 'bob.e@example.com', CURRENT_TIMESTAMP, 3),

       ('Charlie Nilsson', 'charlie.n@example.com', CURRENT_TIMESTAMP, 4),
       ('David Svensson', 'david.s@example.com', CURRENT_TIMESTAMP, 4),
       ('Eva Andersson', 'eva.a@example.com', CURRENT_TIMESTAMP, 4),

       ('Filip Johansson', 'filip.j@example.com', CURRENT_TIMESTAMP, 5),
       ('Greta Lind', 'greta.l@example.com', CURRENT_TIMESTAMP, 5),

       ('Hugo Larsson', 'hugo.l@example.com', CURRENT_TIMESTAMP, 6),
       ('Isak Berg', 'isak.b@example.com', CURRENT_TIMESTAMP, 6),

       ('Julia Holm', 'julia.h@example.com', CURRENT_TIMESTAMP, 7),
       ('Kevin Sjöberg', 'kevin.s@example.com', CURRENT_TIMESTAMP, 7),
       ('Lina Eriksson', 'lina.e@example.com', CURRENT_TIMESTAMP, 7),

       ('Markus Svensson', 'markus.s@example.com', CURRENT_TIMESTAMP, 8),
       ('Nina Andersson', 'nina.a@example.com', CURRENT_TIMESTAMP, 8),

       ('Oskar Lindberg', 'oskar.l@example.com', CURRENT_TIMESTAMP, 9),
       ('Petra Karlsson', 'petra.k@example.com', CURRENT_TIMESTAMP, 9),

       ('Robin Johansson', 'robin.j@example.com', CURRENT_TIMESTAMP, 10),
       ('Sara Berg', 'sara.b@example.com', CURRENT_TIMESTAMP, 10);

-- ADMIN
INSERT INTO APP_USER (username, password, role)
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN');
