-- OptiMatch Large Test Data
-- 120 students, 15 projects, challenging constraints
-- Run this after schema.sql to stress-test the algorithm

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
-- PROJECTS (15 projects with varying requirements)
-- Total capacity: min 95, max 145 students (for 120 students)
-- Some projects are highly competitive (low capacity, popular)
-- ============================================================

INSERT INTO projects (code, name, description, min_capacity, max_capacity, required_gpa) VALUES
-- High-demand, limited capacity (competitive)
('AI-100', 'Advanced Machine Learning', 'Deep learning, transformers, and cutting-edge AI research.', 4, 8, 3.50),
('SEC-300', 'Cybersecurity & Ethical Hacking', 'Penetration testing, network security, CTF challenges.', 4, 7, 3.30),
('QUANT-400', 'Quantitative Finance', 'Algorithmic trading, financial modeling, risk analysis.', 3, 6, 3.60),

-- Medium-demand projects
('WEB-200', 'Full-Stack Web Development', 'React, Node.js, cloud deployment, microservices.', 6, 10, 2.80),
('MOB-150', 'Mobile App Development', 'iOS/Android native and cross-platform with Flutter.', 6, 10, 2.50),
('DATA-250', 'Big Data & Analytics', 'Spark, Hadoop, data pipelines, visualization.', 5, 9, 3.00),
('CLOUD-275', 'Cloud Architecture', 'AWS, Azure, Kubernetes, DevOps practices.', 5, 9, 2.90),
('BLOCK-350', 'Blockchain Development', 'Smart contracts, DeFi, Web3 applications.', 4, 8, 3.10),

-- Lower barrier projects (more accessible)
('GAME-175', 'Game Development with Unity', '2D/3D games, physics, multiplayer systems.', 6, 12, 2.00),
('IOT-225', 'IoT & Embedded Systems', 'Arduino, Raspberry Pi, sensor networks.', 5, 10, 2.30),
('UX-180', 'UX/UI Design & Research', 'User research, prototyping, design systems.', 6, 10, 0.00),
('QA-160', 'Software Testing & QA', 'Automation, CI/CD, test frameworks.', 6, 12, 2.00),

-- Catch-all projects (high capacity, low requirements)
('OPEN-100', 'Open Source Contribution', 'Contribute to real open source projects.', 8, 15, 0.00),
('STARTUP-200', 'Startup Incubator', 'Build your own startup idea with mentorship.', 6, 12, 2.20),
('RESEARCH-300', 'Research Assistant Program', 'Assist faculty with ongoing research.', 5, 10, 2.50);

-- ============================================================
-- STUDENTS (120 students with varying GPAs)
-- 10 partner pairs (20 students), 100 individuals
-- Distribution designed to create conflicts
-- ============================================================

-- High GPA students (3.5+) - 20 students
INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES
('S001', 'Alexander Chen', 'alex.chen@uni.edu', 3.98, NULL),
('S002', 'Beatrice Wang', 'bea.wang@uni.edu', 3.95, NULL),
('S003', 'Charles Kim', 'charles.kim@uni.edu', 3.92, NULL),
('S004', 'Diana Patel', 'diana.patel@uni.edu', 3.89, NULL),
('S005', 'Ethan Brown', 'ethan.brown@uni.edu', 3.85, NULL),
('S006', 'Fiona Davis', 'fiona.davis@uni.edu', 3.82, NULL),
('S007', 'George Miller', 'george.miller@uni.edu', 3.78, NULL),
('S008', 'Hannah Wilson', 'hannah.wilson@uni.edu', 3.75, NULL),
('S009', 'Ivan Rodriguez', 'ivan.rod@uni.edu', 3.72, NULL),
('S010', 'Julia Martinez', 'julia.mart@uni.edu', 3.68, NULL),
('S011', 'Kevin Lee', 'kevin.lee@uni.edu', 3.65, NULL),
('S012', 'Laura Johnson', 'laura.j@uni.edu', 3.62, NULL),
('S013', 'Michael Taylor', 'michael.t@uni.edu', 3.58, NULL),
('S014', 'Nina Anderson', 'nina.a@uni.edu', 3.55, NULL),
('S015', 'Oscar Thomas', 'oscar.t@uni.edu', 3.52, NULL),
('S016', 'Patricia White', 'patricia.w@uni.edu', 3.50, NULL),
('S017', 'Quincy Harris', 'quincy.h@uni.edu', 3.50, NULL),
('S018', 'Rachel Clark', 'rachel.c@uni.edu', 3.50, NULL),
('S019', 'Steven Lewis', 'steven.l@uni.edu', 3.50, NULL),
('S020', 'Tiffany Walker', 'tiffany.w@uni.edu', 3.50, NULL);

-- Medium-high GPA students (3.0-3.49) - 35 students
INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES
('S021', 'Uma Robinson', 'uma.r@uni.edu', 3.48, NULL),
('S022', 'Victor Young', 'victor.y@uni.edu', 3.45, NULL),
('S023', 'Wendy King', 'wendy.k@uni.edu', 3.42, NULL),
('S024', 'Xavier Wright', 'xavier.w@uni.edu', 3.40, NULL),
('S025', 'Yolanda Scott', 'yolanda.s@uni.edu', 3.38, NULL),
('S026', 'Zachary Green', 'zach.g@uni.edu', 3.35, NULL),
('S027', 'Amber Hall', 'amber.h@uni.edu', 3.32, NULL),
('S028', 'Brandon Adams', 'brandon.a@uni.edu', 3.30, NULL),
('S029', 'Cynthia Baker', 'cynthia.b@uni.edu', 3.28, NULL),
('S030', 'Derek Nelson', 'derek.n@uni.edu', 3.25, NULL),
('S031', 'Elena Carter', 'elena.c@uni.edu', 3.22, NULL),
('S032', 'Felix Mitchell', 'felix.m@uni.edu', 3.20, NULL),
('S033', 'Gloria Perez', 'gloria.p@uni.edu', 3.18, NULL),
('S034', 'Henry Roberts', 'henry.r@uni.edu', 3.15, NULL),
('S035', 'Iris Turner', 'iris.t@uni.edu', 3.12, NULL),
('S036', 'James Phillips', 'james.p@uni.edu', 3.10, NULL),
('S037', 'Karen Campbell', 'karen.c@uni.edu', 3.08, NULL),
('S038', 'Leo Parker', 'leo.p@uni.edu', 3.06, NULL),
('S039', 'Monica Evans', 'monica.e@uni.edu', 3.05, NULL),
('S040', 'Nathan Edwards', 'nathan.e@uni.edu', 3.04, NULL),
('S041', 'Olivia Collins', 'olivia.c@uni.edu', 3.03, NULL),
('S042', 'Paul Stewart', 'paul.s@uni.edu', 3.02, NULL),
('S043', 'Quinn Sanchez', 'quinn.s@uni.edu', 3.01, NULL),
('S044', 'Rosa Morris', 'rosa.m@uni.edu', 3.00, NULL),
('S045', 'Samuel Rogers', 'samuel.r@uni.edu', 3.00, NULL),
('S046', 'Teresa Reed', 'teresa.r@uni.edu', 3.00, NULL),
('S047', 'Ulysses Cook', 'ulysses.c@uni.edu', 3.00, NULL),
('S048', 'Valerie Morgan', 'valerie.m@uni.edu', 3.00, NULL),
('S049', 'William Bell', 'william.b@uni.edu', 3.00, NULL),
('S050', 'Xena Murphy', 'xena.m@uni.edu', 3.00, NULL),
('S051', 'Yusuf Bailey', 'yusuf.b@uni.edu', 3.00, NULL),
('S052', 'Zara Rivera', 'zara.r@uni.edu', 3.00, NULL),
('S053', 'Adam Cooper', 'adam.c@uni.edu', 3.00, NULL),
('S054', 'Bella Richardson', 'bella.r@uni.edu', 3.00, NULL),
('S055', 'Caleb Cox', 'caleb.c@uni.edu', 3.00, NULL);

-- Medium GPA students (2.5-2.99) - 40 students
INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES
('S056', 'Daisy Howard', 'daisy.h@uni.edu', 2.98, NULL),
('S057', 'Edwin Ward', 'edwin.w@uni.edu', 2.95, NULL),
('S058', 'Faith Torres', 'faith.t@uni.edu', 2.92, NULL),
('S059', 'Gary Peterson', 'gary.p@uni.edu', 2.90, NULL),
('S060', 'Holly Gray', 'holly.g@uni.edu', 2.88, NULL),
('S061', 'Isaac Ramirez', 'isaac.r@uni.edu', 2.85, NULL),
('S062', 'Jade James', 'jade.j@uni.edu', 2.82, NULL),
('S063', 'Kyle Watson', 'kyle.w@uni.edu', 2.80, NULL),
('S064', 'Lily Brooks', 'lily.b@uni.edu', 2.78, NULL),
('S065', 'Mason Kelly', 'mason.k@uni.edu', 2.75, NULL),
('S066', 'Nora Sanders', 'nora.s@uni.edu', 2.72, NULL),
('S067', 'Owen Price', 'owen.p@uni.edu', 2.70, NULL),
('S068', 'Penny Bennett', 'penny.b@uni.edu', 2.68, NULL),
('S069', 'Reed Wood', 'reed.w@uni.edu', 2.65, NULL),
('S070', 'Stella Barnes', 'stella.b@uni.edu', 2.62, NULL),
('S071', 'Troy Ross', 'troy.r@uni.edu', 2.60, NULL),
('S072', 'Unity Henderson', 'unity.h@uni.edu', 2.58, NULL),
('S073', 'Vince Coleman', 'vince.c@uni.edu', 2.55, NULL),
('S074', 'Willow Jenkins', 'willow.j@uni.edu', 2.52, NULL),
('S075', 'Xander Perry', 'xander.p@uni.edu', 2.50, NULL),
('S076', 'Yara Powell', 'yara.p@uni.edu', 2.50, NULL),
('S077', 'Zeke Long', 'zeke.l@uni.edu', 2.50, NULL),
('S078', 'Aria Patterson', 'aria.p@uni.edu', 2.50, NULL),
('S079', 'Blake Hughes', 'blake.h@uni.edu', 2.50, NULL),
('S080', 'Clara Flores', 'clara.f@uni.edu', 2.50, NULL),
('S081', 'Dante Washington', 'dante.w@uni.edu', 2.50, NULL),
('S082', 'Elise Butler', 'elise.b@uni.edu', 2.50, NULL),
('S083', 'Finn Simmons', 'finn.s@uni.edu', 2.50, NULL),
('S084', 'Grace Foster', 'grace.f@uni.edu', 2.50, NULL),
('S085', 'Hugo Gonzales', 'hugo.g@uni.edu', 2.50, NULL),
('S086', 'Ivy Bryant', 'ivy.b@uni.edu', 2.50, NULL),
('S087', 'Jack Alexander', 'jack.a@uni.edu', 2.50, NULL),
('S088', 'Kira Russell', 'kira.r@uni.edu', 2.50, NULL),
('S089', 'Lance Griffin', 'lance.g@uni.edu', 2.50, NULL),
('S090', 'Maya Diaz', 'maya.d@uni.edu', 2.50, NULL),
('S091', 'Nico Hayes', 'nico.h@uni.edu', 2.50, NULL),
('S092', 'Opal Myers', 'opal.m@uni.edu', 2.50, NULL),
('S093', 'Pete Ford', 'pete.f@uni.edu', 2.50, NULL),
('S094', 'Quinn Hamilton', 'quinn.ham@uni.edu', 2.50, NULL),
('S095', 'Ruby Graham', 'ruby.g@uni.edu', 2.50, NULL);

-- Lower GPA students (below 2.5) - 25 students
INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES
('S096', 'Sean Sullivan', 'sean.s@uni.edu', 2.45, NULL),
('S097', 'Tara Wallace', 'tara.w@uni.edu', 2.42, NULL),
('S098', 'Uri West', 'uri.w@uni.edu', 2.38, NULL),
('S099', 'Vera Cole', 'vera.c@uni.edu', 2.35, NULL),
('S100', 'Wade Hunt', 'wade.h@uni.edu', 2.30, NULL),
('S101', 'Xyla Stephens', 'xyla.s@uni.edu', 2.28, NULL),
('S102', 'York Ellis', 'york.e@uni.edu', 2.25, NULL),
('S103', 'Zola Harper', 'zola.h@uni.edu', 2.22, NULL),
('S104', 'Axel Warren', 'axel.w@uni.edu', 2.20, NULL),
('S105', 'Bree Mason', 'bree.m@uni.edu', 2.18, NULL),
('S106', 'Cruz Stone', 'cruz.s@uni.edu', 2.15, NULL),
('S107', 'Dawn Webb', 'dawn.w@uni.edu', 2.12, NULL),
('S108', 'Ezra Black', 'ezra.b@uni.edu', 2.10, NULL),
('S109', 'Flora Lawrence', 'flora.l@uni.edu', 2.08, NULL),
('S110', 'Gage Knight', 'gage.k@uni.edu', 2.05, NULL);

-- Partner pairs (10 pairs = 20 students with varying GPAs)
INSERT INTO students (student_id, name, email, gpa, partner_id) VALUES
-- Pair 1: High GPA partners wanting competitive projects
('S111', 'Harper Reid', 'harper.r@uni.edu', 3.75, NULL),
('S112', 'Ian Foster', 'ian.f@uni.edu', 3.70, NULL),
-- Pair 2: High GPA partners
('S113', 'Jenna Cross', 'jenna.c@uni.edu', 3.55, NULL),
('S114', 'Keith Dunn', 'keith.d@uni.edu', 3.48, NULL),
-- Pair 3: Medium-high GPA partners
('S115', 'Luna Shaw', 'luna.s@uni.edu', 3.25, NULL),
('S116', 'Miles Webb', 'miles.w@uni.edu', 3.18, NULL),
-- Pair 4: Medium GPA partners
('S117', 'Nadia Hunt', 'nadia.h@uni.edu', 2.95, NULL),
('S118', 'Omar Fields', 'omar.f@uni.edu', 2.88, NULL),
-- Pair 5: Medium GPA partners wanting same project
('S119', 'Piper Lane', 'piper.l@uni.edu', 2.75, NULL),
('S120', 'Quinn Nash', 'quinn.n@uni.edu', 2.70, NULL);

-- Set up partner relationships
UPDATE students SET partner_id = 112 WHERE id = 111;
UPDATE students SET partner_id = 111 WHERE id = 112;
UPDATE students SET partner_id = 114 WHERE id = 113;
UPDATE students SET partner_id = 113 WHERE id = 114;
UPDATE students SET partner_id = 116 WHERE id = 115;
UPDATE students SET partner_id = 115 WHERE id = 116;
UPDATE students SET partner_id = 118 WHERE id = 117;
UPDATE students SET partner_id = 117 WHERE id = 118;
UPDATE students SET partner_id = 120 WHERE id = 119;
UPDATE students SET partner_id = 119 WHERE id = 120;

-- ============================================================
-- PREFERENCES
-- Project IDs: 1=AI, 2=SEC, 3=QUANT, 4=WEB, 5=MOB, 6=DATA,
--              7=CLOUD, 8=BLOCK, 9=GAME, 10=IOT, 11=UX, 12=QA,
--              13=OPEN, 14=STARTUP, 15=RESEARCH
--
-- Design creates conflicts:
-- - Many high GPA students want AI(1), SEC(2), QUANT(3)
-- - Medium students cluster around WEB(4), DATA(6), BLOCK(8)
-- - Creates competition for limited spots
-- ============================================================

-- High GPA students (1-20): Most want AI, SEC, QUANT (competitive)
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(1, 1, 1), (1, 3, 2), (1, 2, 3), (1, 6, 4), (1, 8, 5),
(2, 1, 1), (2, 2, 2), (2, 3, 3), (2, 6, 4), (2, 7, 5),
(3, 3, 1), (3, 1, 2), (3, 2, 3), (3, 8, 4), (3, 6, 5),
(4, 1, 1), (4, 3, 2), (4, 6, 3), (4, 2, 4), (4, 7, 5),
(5, 2, 1), (5, 1, 2), (5, 3, 3), (5, 8, 4), (5, 6, 5),
(6, 1, 1), (6, 2, 2), (6, 6, 3), (6, 3, 4), (6, 7, 5),
(7, 3, 1), (7, 2, 2), (7, 1, 3), (7, 8, 4), (7, 6, 5),
(8, 1, 1), (8, 6, 2), (8, 3, 3), (8, 2, 4), (8, 7, 5),
(9, 2, 1), (9, 3, 2), (9, 1, 3), (9, 6, 4), (9, 8, 5),
(10, 1, 1), (10, 2, 2), (10, 8, 3), (10, 3, 4), (10, 6, 5),
(11, 3, 1), (11, 1, 2), (11, 2, 3), (11, 6, 4), (11, 7, 5),
(12, 1, 1), (12, 6, 2), (12, 2, 3), (12, 3, 4), (12, 8, 5),
(13, 2, 1), (13, 1, 2), (13, 6, 3), (13, 3, 4), (13, 7, 5),
(14, 1, 1), (14, 3, 2), (14, 2, 3), (14, 8, 4), (14, 6, 5),
(15, 6, 1), (15, 1, 2), (15, 3, 3), (15, 2, 4), (15, 7, 5),
(16, 1, 1), (16, 2, 2), (16, 6, 3), (16, 8, 4), (16, 3, 5),
(17, 2, 1), (17, 3, 2), (17, 1, 3), (17, 6, 4), (17, 7, 5),
(18, 3, 1), (18, 1, 2), (18, 8, 3), (18, 2, 4), (18, 6, 5),
(19, 1, 1), (19, 6, 2), (19, 2, 3), (19, 3, 4), (19, 8, 5),
(20, 2, 1), (20, 1, 2), (20, 3, 3), (20, 6, 4), (20, 7, 5);

-- Medium-high GPA students (21-55): Mix of competitive and mid-tier
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(21, 1, 1), (21, 6, 2), (21, 4, 3), (21, 7, 4), (21, 8, 5),
(22, 2, 1), (22, 8, 2), (22, 6, 3), (22, 4, 4), (22, 7, 5),
(23, 6, 1), (23, 1, 2), (23, 4, 3), (23, 7, 4), (23, 5, 5),
(24, 4, 1), (24, 6, 2), (24, 8, 3), (24, 7, 4), (24, 5, 5),
(25, 8, 1), (25, 6, 2), (25, 4, 3), (25, 2, 4), (25, 7, 5),
(26, 6, 1), (26, 4, 2), (26, 8, 3), (26, 7, 4), (26, 5, 5),
(27, 4, 1), (27, 5, 2), (27, 6, 3), (27, 8, 4), (27, 7, 5),
(28, 2, 1), (28, 6, 2), (28, 4, 3), (28, 8, 4), (28, 7, 5),
(29, 8, 1), (29, 4, 2), (29, 6, 3), (29, 7, 4), (29, 5, 5),
(30, 6, 1), (30, 8, 2), (30, 4, 3), (30, 7, 4), (30, 5, 5),
(31, 4, 1), (31, 6, 2), (31, 5, 3), (31, 8, 4), (31, 7, 5),
(32, 8, 1), (32, 6, 2), (32, 4, 3), (32, 7, 4), (32, 9, 5),
(33, 6, 1), (33, 4, 2), (33, 8, 3), (33, 5, 4), (33, 7, 5),
(34, 4, 1), (34, 8, 2), (34, 6, 3), (34, 7, 4), (34, 9, 5),
(35, 8, 1), (35, 4, 2), (35, 6, 3), (35, 5, 4), (35, 7, 5),
(36, 6, 1), (36, 8, 2), (36, 4, 3), (36, 7, 4), (36, 5, 5),
(37, 4, 1), (37, 6, 2), (37, 5, 3), (37, 9, 4), (37, 7, 5),
(38, 6, 1), (38, 4, 2), (38, 8, 3), (38, 7, 4), (38, 5, 5),
(39, 4, 1), (39, 5, 2), (39, 6, 3), (39, 9, 4), (39, 7, 5),
(40, 6, 1), (40, 4, 2), (40, 7, 3), (40, 8, 4), (40, 5, 5),
(41, 5, 1), (41, 4, 2), (41, 6, 3), (41, 9, 4), (41, 7, 5),
(42, 4, 1), (42, 6, 2), (42, 5, 3), (42, 7, 4), (42, 8, 5),
(43, 6, 1), (43, 5, 2), (43, 4, 3), (43, 9, 4), (43, 7, 5),
(44, 4, 1), (44, 6, 2), (44, 7, 3), (44, 5, 4), (44, 9, 5),
(45, 5, 1), (45, 6, 2), (45, 4, 3), (45, 9, 4), (45, 7, 5),
(46, 6, 1), (46, 4, 2), (46, 5, 3), (46, 7, 4), (46, 9, 5),
(47, 4, 1), (47, 5, 2), (47, 6, 3), (47, 9, 4), (47, 10, 5),
(48, 5, 1), (48, 4, 2), (48, 9, 3), (48, 6, 4), (48, 7, 5),
(49, 6, 1), (49, 4, 2), (49, 5, 3), (49, 7, 4), (49, 10, 5),
(50, 4, 1), (50, 9, 2), (50, 5, 3), (50, 6, 4), (50, 7, 5),
(51, 5, 1), (51, 6, 2), (51, 4, 3), (51, 9, 4), (51, 10, 5),
(52, 4, 1), (52, 5, 2), (52, 9, 3), (52, 6, 4), (52, 7, 5),
(53, 9, 1), (53, 4, 2), (53, 5, 3), (53, 6, 4), (53, 10, 5),
(54, 5, 1), (54, 4, 2), (54, 6, 3), (54, 9, 4), (54, 11, 5),
(55, 4, 1), (55, 9, 2), (55, 5, 3), (55, 6, 4), (55, 10, 5);

-- Medium GPA students (56-95): More diverse preferences
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(56, 4, 1), (56, 5, 2), (56, 9, 3), (56, 6, 4), (56, 10, 5),
(57, 5, 1), (57, 9, 2), (57, 4, 3), (57, 10, 4), (57, 11, 5),
(58, 9, 1), (58, 4, 2), (58, 5, 3), (58, 11, 4), (58, 6, 5),
(59, 4, 1), (59, 6, 2), (59, 7, 3), (59, 9, 4), (59, 5, 5),
(60, 5, 1), (60, 4, 2), (60, 9, 3), (60, 10, 4), (60, 11, 5),
(61, 9, 1), (61, 5, 2), (61, 4, 3), (61, 11, 4), (61, 10, 5),
(62, 4, 1), (62, 9, 2), (62, 5, 3), (62, 7, 4), (62, 10, 5),
(63, 5, 1), (63, 4, 2), (63, 10, 3), (63, 9, 4), (63, 7, 5),
(64, 9, 1), (64, 11, 2), (64, 4, 3), (64, 5, 4), (64, 10, 5),
(65, 4, 1), (65, 5, 2), (65, 9, 3), (65, 11, 4), (65, 10, 5),
(66, 11, 1), (66, 9, 2), (66, 4, 3), (66, 5, 4), (66, 10, 5),
(67, 9, 1), (67, 4, 2), (67, 11, 3), (67, 5, 4), (67, 10, 5),
(68, 4, 1), (68, 11, 2), (68, 9, 3), (68, 5, 4), (68, 12, 5),
(69, 5, 1), (69, 9, 2), (69, 4, 3), (69, 10, 4), (69, 11, 5),
(70, 9, 1), (70, 11, 2), (70, 4, 3), (70, 12, 4), (70, 5, 5),
(71, 11, 1), (71, 4, 2), (71, 9, 3), (71, 5, 4), (71, 12, 5),
(72, 4, 1), (72, 9, 2), (72, 11, 3), (72, 10, 4), (72, 5, 5),
(73, 9, 1), (73, 5, 2), (73, 11, 3), (73, 4, 4), (73, 12, 5),
(74, 5, 1), (74, 11, 2), (74, 9, 3), (74, 4, 4), (74, 10, 5),
(75, 11, 1), (75, 9, 2), (75, 5, 3), (75, 4, 4), (75, 12, 5),
(76, 9, 1), (76, 4, 2), (76, 11, 3), (76, 12, 4), (76, 5, 5),
(77, 4, 1), (77, 9, 2), (77, 5, 3), (77, 11, 4), (77, 13, 5),
(78, 11, 1), (78, 5, 2), (78, 9, 3), (78, 4, 4), (78, 12, 5),
(79, 9, 1), (79, 11, 2), (79, 4, 3), (79, 5, 4), (79, 13, 5),
(80, 5, 1), (80, 4, 2), (80, 9, 3), (80, 11, 4), (80, 12, 5),
(81, 4, 1), (81, 11, 2), (81, 9, 3), (81, 5, 4), (81, 13, 5),
(82, 9, 1), (82, 5, 2), (82, 4, 3), (82, 12, 4), (82, 11, 5),
(83, 11, 1), (83, 9, 2), (83, 4, 3), (83, 5, 4), (83, 13, 5),
(84, 5, 1), (84, 11, 2), (84, 9, 3), (84, 12, 4), (84, 4, 5),
(85, 9, 1), (85, 4, 2), (85, 5, 3), (85, 11, 4), (85, 13, 5),
(86, 4, 1), (86, 9, 2), (86, 11, 3), (86, 5, 4), (86, 12, 5),
(87, 11, 1), (87, 5, 2), (87, 4, 3), (87, 9, 4), (87, 13, 5),
(88, 9, 1), (88, 11, 2), (88, 5, 3), (88, 4, 4), (88, 12, 5),
(89, 5, 1), (89, 4, 2), (89, 9, 3), (89, 11, 4), (89, 13, 5),
(90, 4, 1), (90, 9, 2), (90, 5, 3), (90, 12, 4), (90, 11, 5),
(91, 9, 1), (91, 5, 2), (91, 11, 3), (91, 4, 4), (91, 13, 5),
(92, 11, 1), (92, 4, 2), (92, 9, 3), (92, 5, 4), (92, 12, 5),
(93, 5, 1), (93, 9, 2), (93, 4, 3), (93, 11, 4), (93, 13, 5),
(94, 4, 1), (94, 11, 2), (94, 5, 3), (94, 9, 4), (94, 12, 5),
(95, 9, 1), (95, 5, 2), (95, 4, 3), (95, 11, 4), (95, 13, 5);

-- Lower GPA students (96-110): Limited options due to GPA requirements
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(96, 9, 1), (96, 11, 2), (96, 13, 3), (96, 12, 4), (96, 14, 5),
(97, 11, 1), (97, 9, 2), (97, 13, 3), (97, 14, 4), (97, 12, 5),
(98, 9, 1), (98, 13, 2), (98, 11, 3), (98, 12, 4), (98, 14, 5),
(99, 13, 1), (99, 9, 2), (99, 11, 3), (99, 14, 4), (99, 12, 5),
(100, 11, 1), (100, 13, 2), (100, 9, 3), (100, 12, 4), (100, 14, 5),
(101, 9, 1), (101, 11, 2), (101, 14, 3), (101, 13, 4), (101, 12, 5),
(102, 13, 1), (102, 11, 2), (102, 9, 3), (102, 14, 4), (102, 12, 5),
(103, 11, 1), (103, 9, 2), (103, 13, 3), (103, 12, 4), (103, 14, 5),
(104, 9, 1), (104, 13, 2), (104, 11, 3), (104, 14, 4), (104, 12, 5),
(105, 13, 1), (105, 9, 2), (105, 14, 3), (105, 11, 4), (105, 12, 5),
(106, 11, 1), (106, 13, 2), (106, 9, 3), (106, 12, 4), (106, 14, 5),
(107, 9, 1), (107, 14, 2), (107, 13, 3), (107, 11, 4), (107, 12, 5),
(108, 13, 1), (108, 11, 2), (108, 14, 3), (108, 9, 4), (108, 12, 5),
(109, 14, 1), (109, 9, 2), (109, 13, 3), (109, 11, 4), (109, 12, 5),
(110, 9, 1), (110, 13, 2), (110, 14, 3), (110, 11, 4), (110, 12, 5);

-- Partner pairs (111-120): Partners have similar preferences
-- Pair 1 (111-112): Both want SEC and AI (both qualify)
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(111, 2, 1), (111, 1, 2), (111, 6, 3), (111, 8, 4), (111, 7, 5),
(112, 2, 1), (112, 1, 2), (112, 8, 3), (112, 6, 4), (112, 7, 5);

-- Pair 2 (113-114): Both want AI and QUANT (one barely qualifies)
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(113, 1, 1), (113, 3, 2), (113, 6, 3), (113, 8, 4), (113, 7, 5),
(114, 1, 1), (114, 6, 2), (114, 8, 3), (114, 4, 4), (114, 7, 5);

-- Pair 3 (115-116): Web and Data focus
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(115, 4, 1), (115, 6, 2), (115, 8, 3), (115, 7, 4), (115, 5, 5),
(116, 4, 1), (116, 6, 2), (116, 5, 3), (116, 8, 4), (116, 7, 5);

-- Pair 4 (117-118): Mobile and Games
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(117, 5, 1), (117, 9, 2), (117, 4, 3), (117, 10, 4), (117, 11, 5),
(118, 5, 1), (118, 9, 2), (118, 10, 3), (118, 4, 4), (118, 11, 5);

-- Pair 5 (119-120): Games and UX
INSERT INTO preferences (student_id, project_id, `rank`) VALUES
(119, 9, 1), (119, 11, 2), (119, 5, 3), (119, 4, 4), (119, 13, 5),
(120, 9, 1), (120, 11, 2), (120, 4, 3), (120, 5, 4), (120, 13, 5);

-- ============================================================
-- DATA SUMMARY
-- ============================================================
-- Students: 120 (20 high GPA, 35 medium-high, 40 medium, 15 low, 10 partners)
-- Projects: 15 (total capacity: 95-145 for 120 students)
-- Preferences: 600 (5 per student)
-- Partner pairs: 5 pairs (10 students)
--
-- Key conflicts:
--   - AI (8 spots, 3.5 GPA): ~15 students want it as 1st choice
--   - SEC (7 spots, 3.3 GPA): ~10 students want it as 1st choice
--   - QUANT (6 spots, 3.6 GPA): ~5 students want it as 1st choice
--   - Many medium GPA students cluster around WEB, DATA, GAME
--   - Low GPA students can only access GAME, UX, OPEN, QA
--
-- Expected challenges:
--   - Capacity constraints will be tight
--   - GPA requirements will force reassignments
--   - Partner pairs must stay together (some have conflicting eligibility)
-- ============================================================

-- Verify data was inserted correctly
SELECT 'Students:' AS '', COUNT(*) AS count FROM students
UNION ALL
SELECT 'Projects:', COUNT(*) FROM projects
UNION ALL
SELECT 'Preferences:', COUNT(*) FROM preferences
UNION ALL
SELECT 'Partner pairs:', COUNT(*)/2 FROM students WHERE partner_id IS NOT NULL;
