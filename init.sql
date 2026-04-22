-- init.sql — runs automatically on first container start
-- this contains test data and test tables for now. 

CREATE TABLE IF NOT EXISTS test_items (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO test_items (name) VALUES
    ('The'),
    ('Database'),
    ('is'),
    ('working!'),
    ('Nice :)');


CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('student', 'advisor', 'admin')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE courses (
    course_id SERIAL PRIMARY KEY,
    root_prerequisite_node_id INT NULL,
    course_code VARCHAR(30) NOT NULL UNIQUE,
    crn VARCHAR(20),
    title VARCHAR(255) NOT NULL,
    credits INT NOT NULL CHECK (credits >= 0),
    attributes TEXT[] DEFAULT ARRAY[]::TEXT[]
);

CREATE TABLE students (
    student_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    school_student_id VARCHAR(50) UNIQUE,
    major VARCHAR(100),

    CONSTRAINT fk_students_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON DELETE CASCADE
);

CREATE TABLE prerequisite_nodes (
    node_id SERIAL PRIMARY KEY,
    course_id INT NOT NULL,
    node_type VARCHAR(10) NOT NULL,
    required_course_id INT NULL,

    CONSTRAINT fk_prereq_nodes_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_prereq_nodes_required_course
        FOREIGN KEY (required_course_id)
        REFERENCES courses(course_id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_prereq_node_type
        CHECK (node_type IN ('AND', 'OR', 'COURSE')),

    CONSTRAINT chk_prereq_node_structure
        CHECK (
            (node_type = 'COURSE' AND required_course_id IS NOT NULL)
            OR
            (node_type IN ('AND', 'OR') AND required_course_id IS NULL)
        )
);

-- Add root prerequisite FK after both tables exist
ALTER TABLE courses
ADD CONSTRAINT fk_courses_root_prereq_node
FOREIGN KEY (root_prerequisite_node_id)
REFERENCES prerequisite_nodes(node_id)
ON DELETE SET NULL;


CREATE TABLE prerequisite_node_edges (
    parent_node_id INT NOT NULL,
    child_node_id INT NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,

    CONSTRAINT pk_prerequisite_node_edges
        PRIMARY KEY (parent_node_id, child_node_id),

    CONSTRAINT fk_prereq_edges_parent
        FOREIGN KEY (parent_node_id)
        REFERENCES prerequisite_nodes(node_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_prereq_edges_child
        FOREIGN KEY (child_node_id)
        REFERENCES prerequisite_nodes(node_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_no_self_edge
        CHECK (parent_node_id <> child_node_id)
);


CREATE TABLE student_course_records (
    record_id SERIAL PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    grade VARCHAR(5),
    semester_taken VARCHAR(20),
    year_taken INT,

    CONSTRAINT fk_student_records_student
        FOREIGN KEY (student_id)
        REFERENCES students(student_id)
        ON DELETE CASCADE,

    CONSTRAINT fk_student_records_course
        FOREIGN KEY (course_id)
        REFERENCES courses(course_id)
        ON DELETE CASCADE,

    CONSTRAINT uq_student_course
        UNIQUE (student_id, course_id),

    CONSTRAINT chk_student_course_status
        CHECK (status IN ('completed', 'in_progress', 'planned')),

    CONSTRAINT chk_year_taken
        CHECK (year_taken IS NULL OR year_taken >= 1900)
);

-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX idx_courses_crn ON courses(crn);
CREATE INDEX idx_prerequisite_nodes_course_id ON prerequisite_nodes(course_id);
CREATE INDEX idx_prerequisite_nodes_required_course_id ON prerequisite_nodes(required_course_id);
CREATE INDEX idx_student_course_records_student_id ON student_course_records(student_id);
CREATE INDEX idx_student_course_records_course_id ON student_course_records(course_id);
CREATE INDEX idx_prerequisite_node_edges_child_node_id ON prerequisite_node_edges(child_node_id);

-- =========================================================
-- SAMPLE DATA
-- =========================================================


INSERT INTO users (user_id, name, email, password_hash, role)
VALUES
    (1, 'Cole Nam', 'cole@example.com', 'hashed_pw_student', 'student'),
    (2, 'Dr. Advisor', 'advisor@example.com', 'hashed_pw_advisor', 'advisor'),
    (3, 'Tech Admin', 'admin@example.com', 'hashed_pw_admin', 'admin');


INSERT INTO students (student_id, user_id, school_student_id, major)
VALUES
    (1, 1, 'NP100001', 'Computer Science');

-- -------------------------
-- Courses
-- IDs chosen explicitly so prereq inserts are readable
-- -------------------------
INSERT INTO courses (course_id, root_prerequisite_node_id, course_code, crn, title, credits, attributes)
VALUES
    (1,  NULL, 'MAT101', '10001', 'College Algebra', 3, ARRAY['Math Foundation']),
    (2,  NULL, 'CPS101', '10002', 'Intro to Programming', 3, ARRAY['Core']),
    (3,  NULL, 'ENG101', '10003', 'College Writing', 3, ARRAY['GenEd']),
    (4,  NULL, 'MAT120', '10004', 'Precalculus', 4, ARRAY['Math Foundation']),
    (5,  NULL, 'CPS110', '10005', 'Computer Science I', 4, ARRAY['Core']),
    (6,  NULL, 'CPS210', '10006', 'Data Structures', 4, ARRAY['Core']),
    (7,  NULL, 'CPS220', '10007', 'Discrete Structures', 3, ARRAY['Core']),
    (8,  NULL, 'CPS310', '10008', 'Algorithms', 3, ARRAY['Advanced Core']),
    (9,  NULL, 'CPS320', '10009', 'Database Systems', 3, ARRAY['Advanced Core']),
    (10, NULL, 'CPS330', '10010', 'Operating Systems', 3, ARRAY['Advanced Core']),
    (11, NULL, 'CPS340', '10011', 'Software Engineering', 3, ARRAY['Project']),
    (12, NULL, 'CPS410', '10012', 'Advanced Topics in CS', 3, ARRAY['Capstone Track']);

-- =========================================================
-- PREREQUISITE TREES
-- =========================================================


-- Prereq trees:
-- MAT101: none
-- CPS101: none
-- ENG101: none
-- MAT120: none
-- CPS110: none
-- CPS210: CPS110
-- CPS220: none
-- CPS310: CPS210 AND CPS220
-- CPS320: CPS210 OR MAT120
-- CPS330: CPS210
-- CPS340: CPS210 AND ENG101
-- CPS410: (CPS310 AND CPS320) OR CPS330



-- ---------------------------------------------------------
-- Course 6: CPS210 Data Structures
-- Simple prerequisite:
-- CPS110
-- ---------------------------------------------------------
-- root node 1 = COURSE(CPS110)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id)
VALUES
    (1, 6, 'COURSE', 5);

UPDATE courses
SET root_prerequisite_node_id = 1
WHERE course_id = 6;

-- ---------------------------------------------------------
-- Course 8: CPS310 Algorithms
-- AND prerequisite:
-- CPS210 AND CPS220
-- ---------------------------------------------------------
-- node 2 = AND root
-- node 3 = COURSE(CPS210)
-- node 4 = COURSE(CPS220)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id)
VALUES
    (2, 8, 'AND', NULL),
    (3, 8, 'COURSE', 6),
    (4, 8, 'COURSE', 7);

INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order)
VALUES
    (2, 3, 1),
    (2, 4, 2);

UPDATE courses
SET root_prerequisite_node_id = 2
WHERE course_id = 8;

-- ---------------------------------------------------------
-- Course 9: CPS320 Database Systems
-- OR prerequisite:
-- CPS210 OR MAT120
-- ---------------------------------------------------------
-- node 5 = OR root
-- node 6 = COURSE(CPS210)
-- node 7 = COURSE(MAT120)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id)
VALUES
    (5, 9, 'OR', NULL),
    (6, 9, 'COURSE', 6),
    (7, 9, 'COURSE', 4);

INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order)
VALUES
    (5, 6, 1),
    (5, 7, 2);

UPDATE courses
SET root_prerequisite_node_id = 5
WHERE course_id = 9;

-- ---------------------------------------------------------
-- Course 10: CPS330 Operating Systems
-- Single normal prerequisite:
-- CPS210
-- ---------------------------------------------------------
-- node 8 = COURSE(CPS210)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id)
VALUES
    (8, 10, 'COURSE', 6);

UPDATE courses
SET root_prerequisite_node_id = 8
WHERE course_id = 10;

-- ---------------------------------------------------------
-- Course 11: CPS340 Software Engineering
-- AND prerequisite:
-- CPS210 AND ENG101
-- ---------------------------------------------------------
-- node 9  = AND root
-- node 10 = COURSE(CPS210)
-- node 11 = COURSE(ENG101)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id)
VALUES
    (9, 11, 'AND', NULL),
    (10, 11, 'COURSE', 6),
    (11, 11, 'COURSE', 3);

INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order)
VALUES
    (9, 10, 1),
    (9, 11, 2);

UPDATE courses
SET root_prerequisite_node_id = 9
WHERE course_id = 11;

-- ---------------------------------------------------------
-- Course 12: CPS410 Advanced Topics in CS
-- Nested prerequisite:
-- (CPS310 AND CPS320) OR CPS330
-- ---------------------------------------------------------
-- node 12 = OR root
-- node 13 = AND
-- node 14 = COURSE(CPS310)
-- node 15 = COURSE(CPS320)
-- node 16 = COURSE(CPS330)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id)
VALUES
    (12, 12, 'OR', NULL),
    (13, 12, 'AND', NULL),
    (14, 12, 'COURSE', 8),
    (15, 12, 'COURSE', 9),
    (16, 12, 'COURSE', 10);

INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order)
VALUES
    (12, 13, 1),
    (12, 16, 2),
    (13, 14, 1),
    (13, 15, 2);

UPDATE courses
SET root_prerequisite_node_id = 12
WHERE course_id = 12;

-- =========================================================
-- SAMPLE STUDENT COURSE RECORDS
-- Cole has:
-- completed: MAT101, CPS101, ENG101, MAT120, CPS110, CPS220
-- in progress: CPS210
-- planned: CPS320
-- =========================================================
INSERT INTO student_course_records
    (record_id, student_id, course_id, status, grade, semester_taken, year_taken)
VALUES
    (1, 1, 1, 'completed',   'A',  'Fall',   2024), -- MAT101
    (2, 1, 2, 'completed',   'A-', 'Fall',   2024), -- CPS101
    (3, 1, 3, 'completed',   'B+', 'Spring', 2025), -- ENG101
    (4, 1, 4, 'completed',   'B',  'Spring', 2025), -- MAT120
    (5, 1, 5, 'completed',   'A',  'Spring', 2025), -- CPS110
    (6, 1, 7, 'completed',   'A-', 'Fall',   2025), -- CPS220
    (7, 1, 6, 'in_progress', NULL, 'Spring', 2026), -- CPS210
    (8, 1, 9, 'planned',     NULL, 'Fall',   2026); -- CPS320

-- =========================================================
-- Reset sequences so future inserts continue correctly
-- =========================================================
SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1), true);
SELECT setval('students_student_id_seq', COALESCE((SELECT MAX(student_id) FROM students), 1), true);
SELECT setval('courses_course_id_seq', COALESCE((SELECT MAX(course_id) FROM courses), 1), true);
SELECT setval('prerequisite_nodes_node_id_seq', COALESCE((SELECT MAX(node_id) FROM prerequisite_nodes), 1), true);
SELECT setval('student_course_records_record_id_seq', COALESCE((SELECT MAX(record_id) FROM student_course_records), 1), true);

