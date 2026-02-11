-- OptiMatch Test Data
-- 30 students, 6 projects, full preferences
-- Run this after schema.sql to populate the database with test data

-- Clear existing data
DELETE FROM assignments;
DELETE FROM algorithm_runs;
DELETE FROM preferences;
DELETE FROM projects;
DELETE FROM students;

-- Reset auto-increment
ALTER TABLE students AUTO_INCREMENT = 1;
ALTER TABLE projects AUTO_INCREMENT = 1;
ALTER TABLE preferences AUTO_INCREMENT = 1;

-- ============================================================
-- PROJECTS (6 projects with varying requirements)
-- Total capacity: min 18, max 36 students
-- ============================================================

INSERT INTO projects (code, name, description, min_capacity, max_capacity, required_gpa) VALUES
('AI-100', 'Machine Learning Fundamentals', 'Introduction to ML algorithms, neural networks, and practical applications using Python.', 3, 6, 3.00),
('WEB-200', 'Full-Stack Web Development', 'Build modern web applications using React, Node.js, and databases.', 4, 7, 2.50),
('MOB-150', 'Mobile App Development', 'Create cross-platform mobile apps using Flutter and Firebase.', 3, 6, 2.50),
('SEC-300', 'Cybersecurity & Ethical Hacking', 'Learn penetration testing, network security, and security best practices.', 3, 5, 3.20),
('DATA-250', 'Big Data Analytics', 'Work with large datasets using Spark, Hadoop, and data visualization.', 3, 6, 2.80),
('GAME-175', 'Game Development with Unity', 'Design and develop 2D/3D games using Unity engine and C#.', 2, 6, 0.00);

-- ============================================================
-- STUDENTS (30 students with varying GPAs)
-- 4 partner pairs (8 students), 22 individuals
-- ============================================================

INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES
-- High GPA students (3.5+)
('STU001', 'Alice Chen', 'alice.chen@university.edu', 3.95, NULL),
('STU002', 'Bob Williams', 'bob.williams@university.edu', 3.82, NULL),
('STU003', 'Carol Davis', 'carol.davis@university.edu', 3.78, NULL),
('STU004', 'David Kim', 'david.kim@university.edu', 3.65, NULL),
('STU005', 'Emma Brown', 'emma.brown@university.edu', 3.55, NULL),
('STU006', 'Frank Miller', 'frank.miller@university.edu', 3.50, NULL),

-- Medium-high GPA students (3.0-3.49)
('STU007', 'Grace Lee', 'grace.lee@university.edu', 3.42, NULL),
('STU008', 'Henry Wang', 'henry.wang@university.edu', 3.35, NULL),
('STU009', 'Ivy Johnson', 'ivy.johnson@university.edu', 3.28, NULL),
('STU010', 'Jack Smith', 'jack.smith@university.edu', 3.22, NULL),
('STU011', 'Karen Taylor', 'karen.taylor@university.edu', 3.15, NULL),
('STU012', 'Leo Martinez', 'leo.martinez@university.edu', 3.08, NULL),
('STU013', 'Mia Garcia', 'mia.garcia@university.edu', 3.02, NULL),

-- Medium GPA students (2.5-2.99)
('STU014', 'Nathan Anderson', 'nathan.anderson@university.edu', 2.95, NULL),
('STU015', 'Olivia Thomas', 'olivia.thomas@university.edu', 2.88, NULL),
('STU016', 'Peter Jackson', 'peter.jackson@university.edu', 2.82, NULL),
('STU017', 'Quinn White', 'quinn.white@university.edu', 2.75, NULL),
('STU018', 'Rachel Harris', 'rachel.harris@university.edu', 2.68, NULL),
('STU019', 'Samuel Clark', 'samuel.clark@university.edu', 2.62, NULL),
('STU020', 'Tina Lewis', 'tina.lewis@university.edu', 2.55, NULL),
('STU021', 'Uma Robinson', 'uma.robinson@university.edu', 2.50, NULL),

-- Lower GPA students (below 2.5)
('STU022', 'Victor Young', 'victor.young@university.edu', 2.45, NULL),
('STU023', 'Wendy King', 'wendy.king@university.edu', 2.38, NULL),
('STU024', 'Xavier Wright', 'xavier.wright@university.edu', 2.32, NULL),
('STU025', 'Yolanda Scott', 'yolanda.scott@university.edu', 2.25, NULL),
('STU026', 'Zack Green', 'zack.green@university.edu', 2.18, NULL),

-- Partner pairs (will update partner_id after insert)
('STU027', 'Amy Parker', 'amy.parker@university.edu', 3.40, NULL),
('STU028', 'Ben Cooper', 'ben.cooper@university.edu', 3.25, NULL),
('STU029', 'Chloe Reed', 'chloe.reed@university.edu', 2.90, NULL),
('STU030', 'Dan Murphy', 'dan.murphy@university.edu', 2.70, NULL);

-- Set up partner relationships (pairs work together)
-- Amy (27) and Ben (28) are partners
UPDATE students SET partner_id = 28 WHERE id = 27;
UPDATE students SET partner_id = 27 WHERE id = 28;

-- Chloe (29) and Dan (30) are partners
UPDATE students SET partner_id = 30 WHERE id = 29;
UPDATE students SET partner_id = 29 WHERE id = 30;

-- ============================================================
-- PREFERENCES (5 choices per student)
-- Project IDs: 1=AI, 2=WEB, 3=MOB, 4=SEC, 5=DATA, 6=GAME
-- ============================================================

-- Alice Chen (3.95) - High achiever, interested in AI/Data
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(1, 1, 1), (1, 4, 2), (1, 5, 3), (1, 2, 4), (1, 3, 5);

-- Bob Williams (3.82) - Security focused
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(2, 4, 1), (2, 1, 2), (2, 5, 3), (2, 2, 4), (2, 3, 5);

-- Carol Davis (3.78) - Web development enthusiast
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(3, 2, 1), (3, 3, 2), (3, 1, 3), (3, 6, 4), (3, 5, 5);

-- David Kim (3.65) - AI and Data
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(4, 1, 1), (4, 5, 2), (4, 4, 3), (4, 2, 4), (4, 6, 5);

-- Emma Brown (3.55) - Mobile development
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(5, 3, 1), (5, 2, 2), (5, 6, 3), (5, 1, 4), (5, 5, 5);

-- Frank Miller (3.50) - Game development passion
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(6, 6, 1), (6, 3, 2), (6, 2, 3), (6, 1, 4), (6, 5, 5);

-- Grace Lee (3.42) - Security and AI
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(7, 4, 1), (7, 1, 2), (7, 5, 3), (7, 3, 4), (7, 2, 5);

-- Henry Wang (3.35) - Big Data focus
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(8, 5, 1), (8, 1, 2), (8, 2, 3), (8, 4, 4), (8, 3, 5);

-- Ivy Johnson (3.28) - Full-stack web
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(9, 2, 1), (9, 3, 2), (9, 5, 3), (9, 6, 4), (9, 1, 5);

-- Jack Smith (3.22) - Mobile and games
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(10, 3, 1), (10, 6, 2), (10, 2, 3), (10, 5, 4), (10, 1, 5);

-- Karen Taylor (3.15) - AI interested
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(11, 1, 1), (11, 5, 2), (11, 2, 3), (11, 4, 4), (11, 3, 5);

-- Leo Martinez (3.08) - Web and mobile
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(12, 2, 1), (12, 3, 2), (12, 6, 3), (12, 5, 4), (12, 1, 5);

-- Mia Garcia (3.02) - Data and AI
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(13, 5, 1), (13, 1, 2), (13, 2, 3), (13, 3, 4), (13, 6, 5);

-- Nathan Anderson (2.95) - Web focused
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(14, 2, 1), (14, 6, 2), (14, 3, 3), (14, 5, 4), (14, 1, 5);

-- Olivia Thomas (2.88) - Game and mobile
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(15, 6, 1), (15, 3, 2), (15, 2, 3), (15, 5, 4), (15, 1, 5);

-- Peter Jackson (2.82) - Data science
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(16, 5, 1), (16, 2, 2), (16, 1, 3), (16, 3, 4), (16, 6, 5);

-- Quinn White (2.75) - Mobile first
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(17, 3, 1), (17, 2, 2), (17, 6, 3), (17, 5, 4), (17, 1, 5);

-- Rachel Harris (2.68) - Web and games
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(18, 2, 1), (18, 6, 2), (18, 3, 3), (18, 5, 4), (18, 1, 5);

-- Samuel Clark (2.62) - Games passion
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(19, 6, 1), (19, 3, 2), (19, 2, 3), (19, 5, 4), (19, 1, 5);

-- Tina Lewis (2.55) - Mobile focused
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(20, 3, 1), (20, 6, 2), (20, 2, 3), (20, 5, 4), (20, 1, 5);

-- Uma Robinson (2.50) - Web development
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(21, 2, 1), (21, 3, 2), (21, 6, 3), (21, 5, 4), (21, 1, 5);

-- Victor Young (2.45) - Games only option (low GPA)
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(22, 6, 1), (22, 3, 2), (22, 2, 3), (22, 5, 4), (22, 1, 5);

-- Wendy King (2.38) - Mobile and games
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(23, 6, 1), (23, 3, 2), (23, 2, 3), (23, 5, 4), (23, 1, 5);

-- Xavier Wright (2.32) - Games interest
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(24, 6, 1), (24, 2, 2), (24, 3, 3), (24, 5, 4), (24, 1, 5);

-- Yolanda Scott (2.25) - Practical projects
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(25, 6, 1), (25, 2, 2), (25, 3, 3), (25, 5, 4), (25, 1, 5);

-- Zack Green (2.18) - Game focused
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(26, 6, 1), (26, 2, 2), (26, 3, 3), (26, 5, 4), (26, 1, 5);

-- Amy Parker (3.40) - Partner with Ben, Security and AI
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(27, 4, 1), (27, 1, 2), (27, 5, 3), (27, 2, 4), (27, 3, 5);

-- Ben Cooper (3.25) - Partner with Amy, Security and AI (similar preferences)
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(28, 4, 1), (28, 1, 2), (28, 2, 3), (28, 5, 4), (28, 3, 5);

-- Chloe Reed (2.90) - Partner with Dan, Web and Mobile
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(29, 2, 1), (29, 3, 2), (29, 6, 3), (29, 5, 4), (29, 1, 5);

-- Dan Murphy (2.70) - Partner with Chloe, Web and Mobile (similar preferences)
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(30, 2, 1), (30, 3, 2), (30, 6, 3), (30, 5, 4), (30, 1, 5);

-- ============================================================
-- DATA SUMMARY
-- ============================================================
-- Students: 30 (6 high GPA, 7 medium-high, 8 medium, 5 low, 4 partners)
-- Projects: 6 (total capacity: 18-36)
-- Preferences: 150 (5 per student)
-- Partner pairs: 2 (Amy+Ben, Chloe+Dan)
--
-- Project distribution of 1st choices:
--   AI-100 (requires 3.0): 3 students
--   WEB-200 (requires 2.5): 7 students
--   MOB-150 (requires 2.5): 4 students
--   SEC-300 (requires 3.2): 3 students
--   DATA-250 (requires 2.8): 3 students
--   GAME-175 (no requirement): 10 students
-- ============================================================

-- Verify data was inserted correctly
SELECT 'Students:' AS '', COUNT(*) AS count FROM students
UNION ALL
SELECT 'Projects:', COUNT(*) FROM projects
UNION ALL
SELECT 'Preferences:', COUNT(*) FROM preferences
UNION ALL
SELECT 'Partner pairs:', COUNT(*)/2 FROM students WHERE partner_id IS NOT NULL;
