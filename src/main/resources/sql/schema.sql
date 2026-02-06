-- OptiMatch Database Schema
-- Student-to-Project Assignment System

-- Drop tables if they exist (in reverse order of dependencies)
DROP TABLE IF EXISTS assignments;
DROP TABLE IF EXISTS algorithm_runs;
DROP TABLE IF EXISTS preferences;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS students;

-- Students table
CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    gpa DECIMAL(3,2) NOT NULL,
    partner_id INT NULL,
    FOREIGN KEY (partner_id) REFERENCES students(id)
);

-- Projects table
CREATE TABLE projects (
    id INT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    min_capacity INT NOT NULL DEFAULT 1,
    max_capacity INT NOT NULL,
    required_gpa DECIMAL(3,2) DEFAULT 0.00
);

-- Preferences table (ranked choices)
CREATE TABLE preferences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    student_id INT NOT NULL,
    project_id INT NOT NULL,
    `rank` INT NOT NULL,  -- 1 = first choice, 2 = second, etc.
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE (student_id, `rank`),
    UNIQUE (student_id, project_id)
);

-- Algorithm runs history
CREATE TABLE algorithm_runs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    run_timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    population_size INT,
    generations INT,
    mutation_rate DECIMAL(4,3),
    crossover_rate DECIMAL(4,3),
    best_fitness DECIMAL(10,2),
    execution_time_ms BIGINT
);

-- Final assignments
CREATE TABLE assignments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    run_id INT NOT NULL,
    student_id INT NOT NULL,
    project_id INT NOT NULL,
    preference_rank INT,  -- NULL if not in student's preferences
    FOREIGN KEY (run_id) REFERENCES algorithm_runs(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    UNIQUE (run_id, student_id)  -- Each student assigned once per run
);

-- Generation statistics (per-generation data for each algorithm run)
CREATE TABLE generation_stats (
    id INT PRIMARY KEY AUTO_INCREMENT,
    run_id INT NOT NULL,
    generation INT NOT NULL,
    best_fitness DECIMAL(10,2),
    average_fitness DECIMAL(10,2),
    worst_fitness DECIMAL(10,2),
    standard_deviation DECIMAL(10,4),
    valid_count INT,
    best_ever_fitness DECIMAL(10,2),
    FOREIGN KEY (run_id) REFERENCES algorithm_runs(id) ON DELETE CASCADE,
    UNIQUE (run_id, generation)
);

-- Indexes for performance
CREATE INDEX idx_preferences_student ON preferences(student_id);
CREATE INDEX idx_preferences_project ON preferences(project_id);
CREATE INDEX idx_assignments_run ON assignments(run_id);
CREATE INDEX idx_assignments_student ON assignments(student_id);
CREATE INDEX idx_assignments_project ON assignments(project_id);
CREATE INDEX idx_generation_stats_run ON generation_stats(run_id);
