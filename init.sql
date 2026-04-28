-- init.sql — runs automatically on first container start
-- this contains course and user data

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
    student_id INT PRIMARY KEY,
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
-- SYSTEM USERS (100 TOTAL: 81 Students, 11 Advisors, 8 Admins)
-- =========================================================

INSERT INTO users (user_id, name, email, password_hash, role) VALUES
    (1, 'Cole Nam', 'cole@newpaltz.edu', 'hashed_pw_student', 'student'),
    (2, 'Florian Leinfellner', 'florian@newpaltz.edu', 'hashed_pw_student', 'student'),
    (3, 'David Dankwah', 'david@newpaltz.edu', 'hashed_pw_student', 'student'),
    (4, 'Denzel Discua', 'denzel@newpaltz.edu', 'hashed_pw_student', 'student'),
    (5, 'Michael Brown', 'michael.brown@newpaltz.edu', 'hashed_pw_student', 'student'),
    (6, 'Emma Wilson', 'emma.wilson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (7, 'James Taylor', 'james.taylor@newpaltz.edu', 'hashed_pw_student', 'student'),
    (8, 'Olivia Moore', 'olivia.moore@newpaltz.edu', 'hashed_pw_student', 'student'),
    (9, 'Robert Anderson', 'robert.anderson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (10, 'Sophia Thomas', 'sophia.thomas@newpaltz.edu', 'hashed_pw_student', 'student'),
    (11, 'William Jackson', 'william.jackson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (12, 'Isabella White', 'isabella.white@newpaltz.edu', 'hashed_pw_student', 'student'),
    (13, 'Richard Harris', 'richard.harris@newpaltz.edu', 'hashed_pw_student', 'student'),
    (14, 'Mia Martin', 'mia.martin@newpaltz.edu', 'hashed_pw_student', 'student'),
    (15, 'Joseph Thompson', 'joseph.thompson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (16, 'Charlotte Garcia', 'charlotte.garcia@newpaltz.edu', 'hashed_pw_student', 'student'),
    (17, 'Charles Martinez', 'charles.martinez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (18, 'Amelia Robinson', 'amelia.robinson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (19, 'Thomas Clark', 'thomas.clark@newpaltz.edu', 'hashed_pw_student', 'student'),
    (20, 'Harper Rodriguez', 'harper.rodriguez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (21, 'Christopher Lewis', 'christopher.lewis@newpaltz.edu', 'hashed_pw_student', 'student'),
    (22, 'Evelyn Lee', 'evelyn.lee@newpaltz.edu', 'hashed_pw_student', 'student'),
    (23, 'Daniel Walker', 'daniel.walker@newpaltz.edu', 'hashed_pw_student', 'student'),
    (24, 'Abigail Hall', 'abigail.hall@newpaltz.edu', 'hashed_pw_student', 'student'),
    (25, 'Paul Allen', 'paul.allen@newpaltz.edu', 'hashed_pw_student', 'student'),
    (26, 'Emily Young', 'emily.young@newpaltz.edu', 'hashed_pw_student', 'student'),
    (27, 'Mark Hernandez', 'mark.hernandez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (28, 'Elizabeth King', 'elizabeth.king@newpaltz.edu', 'hashed_pw_student', 'student'),
    (29, 'Donald Wright', 'donald.wright@newpaltz.edu', 'hashed_pw_student', 'student'),
    (30, 'Avery Lopez', 'avery.lopez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (31, 'George Hill', 'george.hill@newpaltz.edu', 'hashed_pw_student', 'student'),
    (32, 'Ella Scott', 'ella.scott@newpaltz.edu', 'hashed_pw_student', 'student'),
    (33, 'Kenneth Green', 'kenneth.green@newpaltz.edu', 'hashed_pw_student', 'student'),
    (34, 'Madison Adams', 'madison.adams@newpaltz.edu', 'hashed_pw_student', 'student'),
    (35, 'Steven Baker', 'steven.baker@newpaltz.edu', 'hashed_pw_student', 'student'),
    (36, 'Scarlett Gonzalez', 'scarlett.gonzalez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (37, 'Edward Nelson', 'edward.nelson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (38, 'Grace Carter', 'grace.carter@newpaltz.edu', 'hashed_pw_student', 'student'),
    (39, 'Brian Mitchell', 'brian.mitchell@newpaltz.edu', 'hashed_pw_student', 'student'),
    (40, 'Chloe Perez', 'chloe.perez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (41, 'Ronald Roberts', 'ronald.roberts@newpaltz.edu', 'hashed_pw_student', 'student'),
    (42, 'Victoria Turner', 'victoria.turner@newpaltz.edu', 'hashed_pw_student', 'student'),
    (43, 'Anthony Phillips', 'anthony.phillips@newpaltz.edu', 'hashed_pw_student', 'student'),
    (44, 'Riley Campbell', 'riley.campbell@newpaltz.edu', 'hashed_pw_student', 'student'),
    (45, 'Kevin Parker', 'kevin.parker@newpaltz.edu', 'hashed_pw_student', 'student'),
    (46, 'Aria Evans', 'aria.evans@newpaltz.edu', 'hashed_pw_student', 'student'),
    (47, 'Jason Edwards', 'jason.edwards@newpaltz.edu', 'hashed_pw_student', 'student'),
    (48, 'Lily Collins', 'lily.collins@newpaltz.edu', 'hashed_pw_student', 'student'),
    (49, 'Matthew Stewart', 'matthew.stewart@newpaltz.edu', 'hashed_pw_student', 'student'),
    (50, 'Aubrey Sanchez', 'aubrey.sanchez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (51, 'Gary Morris', 'gary.morris@newpaltz.edu', 'hashed_pw_student', 'student'),
    (52, 'Zoey Rogers', 'zoey.rogers@newpaltz.edu', 'hashed_pw_student', 'student'),
    (53, 'Timothy Reed', 'timothy.reed@newpaltz.edu', 'hashed_pw_student', 'student'),
    (54, 'Penelope Cook', 'penelope.cook@newpaltz.edu', 'hashed_pw_student', 'student'),
    (55, 'Jose Morgan', 'jose.morgan@newpaltz.edu', 'hashed_pw_student', 'student'),
    (56, 'Lillian Bell', 'lillian.bell@newpaltz.edu', 'hashed_pw_student', 'student'),
    (57, 'Larry Murphy', 'larry.murphy@newpaltz.edu', 'hashed_pw_student', 'student'),
    (58, 'Addison Bailey', 'addison.bailey@newpaltz.edu', 'hashed_pw_student', 'student'),
    (59, 'Jeffrey Cooper', 'jeffrey.cooper@newpaltz.edu', 'hashed_pw_student', 'student'),
    (60, 'Layla Richardson', 'layla.richardson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (61, 'Frank Cox', 'frank.cox@newpaltz.edu', 'hashed_pw_student', 'student'),
    (62, 'Brooklyn Howard', 'brooklyn.howard@newpaltz.edu', 'hashed_pw_student', 'student'),
    (63, 'Scott Ward', 'scott.ward@newpaltz.edu', 'hashed_pw_student', 'student'),
    (64, 'Zoe Torres', 'zoe.torres@newpaltz.edu', 'hashed_pw_student', 'student'),
    (65, 'Eric Peterson', 'eric.peterson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (66, 'Nora Gray', 'nora.gray@newpaltz.edu', 'hashed_pw_student', 'student'),
    (67, 'Stephen Ramirez', 'stephen.ramirez@newpaltz.edu', 'hashed_pw_student', 'student'),
    (68, 'Hannah James', 'hannah.james@newpaltz.edu', 'hashed_pw_student', 'student'),
    (69, 'Andrew Watson', 'andrew.watson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (70, 'Mila Brooks', 'mila.brooks@newpaltz.edu', 'hashed_pw_student', 'student'),
    (71, 'Raymond Kelly', 'raymond.kelly@newpaltz.edu', 'hashed_pw_student', 'student'),
    (72, 'Leah Sanders', 'leah.sanders@newpaltz.edu', 'hashed_pw_student', 'student'),
    (73, 'Gregory Price', 'gregory.price@newpaltz.edu', 'hashed_pw_student', 'student'),
    (74, 'Audrey Bennett', 'audrey.bennett@newpaltz.edu', 'hashed_pw_student', 'student'),
    (75, 'Joshua Wood', 'joshua.wood@newpaltz.edu', 'hashed_pw_student', 'student'),
    (76, 'Eleanor Barnes', 'eleanor.barnes@newpaltz.edu', 'hashed_pw_student', 'student'),
    (77, 'Jerry Ross', 'jerry.ross@newpaltz.edu', 'hashed_pw_student', 'student'),
    (78, 'Samantha Henderson', 'samantha.henderson@newpaltz.edu', 'hashed_pw_student', 'student'),
    (79, 'Dennis Coleman', 'dennis.coleman@newpaltz.edu', 'hashed_pw_student', 'student'),
    (80, 'Stella Jenkins', 'stella.jenkins@newpaltz.edu', 'hashed_pw_student', 'student'),
    (81, 'Walter Perry', 'walter.perry@newpaltz.edu', 'hashed_pw_student', 'student'),
    -- 11 Advisors
    (82, 'Dr. Maya Powell', 'maya.powell@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (83, 'Prof. Patrick Long', 'patrick.long@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (84, 'Dr. Savannah Patterson', 'savannah.patterson@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (85, 'Prof. Peter Hughes', 'peter.hughes@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (86, 'Dr. Lucy Washington', 'lucy.washington@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (87, 'Prof. Harold Butler', 'harold.butler@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (88, 'Dr. Anna Simmons', 'anna.simmons@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (89, 'Prof. Douglas Foster', 'douglas.foster@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (90, 'Dr. Caroline Gonzales', 'caroline.gonzales@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (91, 'Prof. Henry Bryant', 'henry.bryant@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    (92, 'Dr. Genesis Alexander', 'genesis.alexander@newpaltz.edu', 'hashed_pw_advisor', 'advisor'),
    -- 8 Admins
    (93, 'System Root', 'root@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (94, 'Carl Russell', 'carl.russell@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (95, 'Aaliyah Griffin', 'aaliyah.griffin@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (96, 'Arthur Diaz', 'arthur.diaz@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (97, 'Kennedy Hayes', 'kennedy.hayes@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (98, 'Ryan Myers', 'ryan.myers@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (99, 'Allison Ford', 'allison.ford@newpaltz.edu', 'hashed_pw_admin', 'admin'),
    (100, 'Roger Hamilton', 'roger.hamilton@newpaltz.edu', 'hashed_pw_admin', 'admin');


INSERT INTO students (student_id, user_id, school_student_id, major) VALUES
    (1, 1, '1', 'Computer Science'),
    (2, 2, '2', 'Computer Engineering'),
    (3, 3, '3', 'Computer Science'),
    (4, 4, '4', 'Computer Science'),
    (5, 5, '5', 'Computer Science'),
    (6, 6, '6', 'Computer Engineering'),
    (7, 7, '7', 'Electrical Engineering'),
    (8, 8, '8', 'Mechanical Engineering'),
    (9, 9, '9', 'Computer Science'),
    (10, 10, '10', 'Computer Engineering'),
    (11, 11, '11', 'Electrical Engineering'),
    (12, 12, '12', 'Computer Science'),
    (13, 13, '13', 'Computer Science'),
    (14, 14, '14', 'Computer Engineering'),
    (15, 15, '15', 'Electrical Engineering'),
    (16, 16, '16', 'Mechanical Engineering'),
    (17, 17, '17', 'Computer Science'),
    (18, 18, '18', 'Computer Engineering'),
    (19, 19, '19', 'Electrical Engineering'),
    (20, 20, '20', 'Computer Science'),
    (21, 21, '21', 'Computer Science'),
    (22, 22, '22', 'Computer Engineering'),
    (23, 23, '23', 'Electrical Engineering'),
    (24, 24, '24', 'Computer Science'),
    (25, 25, '25', 'Computer Science'),
    (26, 26, '26', 'Computer Engineering'),
    (27, 27, '27', 'Electrical Engineering'),
    (28, 28, '28', 'Mechanical Engineering'),
    (29, 29, '29', 'Computer Science'),
    (30, 30, '30', 'Computer Engineering'),
    (31, 31, '31', 'Computer Science'),
    (32, 32, '32', 'Computer Science'),
    (33, 33, '33', 'Computer Science'),
    (34, 34, '34', 'Computer Engineering'),
    (35, 35, '35', 'Electrical Engineering'),
    (36, 36, '36', 'Mechanical Engineering'),
    (37, 37, '37', 'Computer Science'),
    (38, 38, '38', 'Computer Engineering'),
    (39, 39, '39', 'Electrical Engineering'),
    (40, 40, '40', 'Mechanical Engineering'),
    (41, 41, '41', 'Computer Science'),
    (42, 42, '42', 'Computer Engineering'),
    (43, 43, '43', 'Electrical Engineering'),
    (44, 44, '44', 'Mechanical Engineering'),
    (45, 45, '45', 'Computer Science'),
    (46, 46, '46', 'Computer Engineering'),
    (47, 47, '47', 'Electrical Engineering'),
    (48, 48, '48', 'Mechanical Engineering'),
    (49, 49, '49', 'Computer Science'),
    (50, 50, '50', 'Computer Engineering'),
    (51, 51, '51', 'Electrical Engineering'),
    (52, 52, '52', 'Mechanical Engineering'),
    (53, 53, '53', 'Computer Science'),
    (54, 54, '54', 'Computer Engineering'),
    (55, 55, '55', 'Electrical Engineering'),
    (56, 56, '56', 'Mechanical Engineering'),
    (57, 57, '57', 'Computer Science'),
    (58, 58, '58', 'Computer Engineering'),
    (59, 59, '59', 'Computer Science'),
    (60, 60, '60', 'Mechanical Engineering'),
    (61, 61, '61', 'Computer Science'),
    (62, 62, '62', 'Computer Engineering'),
    (63, 63, '63', 'Electrical Engineering'),
    (64, 64, '64', 'Mechanical Engineering'),
    (65, 65, '65', 'Computer Science'),
    (66, 66, '66', 'Computer Engineering'),
    (67, 67, '67', 'Electrical Engineering'),
    (68, 68, '68', 'Computer Science'),
    (69, 69, '69', 'Computer Science'),
    (70, 70, '70', 'Computer Engineering'),
    (71, 71, '71', 'Electrical Engineering'),
    (72, 72, '72', 'Computer Science'),
    (73, 73, '73', 'Computer Science'),
    (74, 74, '74', 'Computer Engineering'),
    (75, 75, '75', 'Electrical Engineering'),
    (76, 76, '76', 'Mechanical Engineering'),
    (77, 77, '77', 'Computer Science'),
    (78, 78, '78', 'Computer Engineering'),
    (79, 79, '79', 'Electrical Engineering'),
    (80, 80, '80', 'Mechanical Engineering'),
    (81, 81, '81', 'Computer Science');


-- =========================================================
-- SUNY NEW PALTZ COMPUTER SCIENCE (CPS) & MATH (MAT) COURSES
-- =========================================================
INSERT INTO courses (course_id, root_prerequisite_node_id, course_code, crn, title, credits, attributes) VALUES
    -- CPS Courses (IDs 1-17)
    (1, NULL, 'CPS100', '10001', 'Computers and Applications', 3, ARRAY['Liberal Arts']),
    (2, NULL, 'CPS104', '10002', 'Visual Programming', 3, ARRAY['Liberal Arts']),
    (3, NULL, 'CPS110', '10003', 'Web Page Design', 3, ARRAY['Liberal Arts']),
    (4, NULL, 'CPS210', '10004', 'Computer Science I: Foundations', 4, ARRAY['Liberal Arts', 'Core', 'Critical Thinking']),
    (5, NULL, 'CPS310', '10005', 'Computer Science II: Data Structures', 4, ARRAY['Liberal Arts', 'Core']),
    (6, NULL, 'CPS315', '10006', 'Computer Science III', 4, ARRAY['Liberal Arts', 'Core']),
    (7, NULL, 'CPS330', '10007', 'Assembly Language and Computer Architecture', 4, ARRAY['Liberal Arts', 'Core']),
    (8, NULL, 'CPS340', '10008', 'Operating Systems', 4, ARRAY['Liberal Arts', 'Core']),
    (9, NULL, 'CPS352', '10009', 'Object Oriented Programming', 3, ARRAY['Liberal Arts', 'Core']),
    (10, NULL, 'CPS353', '10010', 'Software Engineering', 3, ARRAY['Liberal Arts', 'Core']),
    (11, NULL, 'CPS415', '10011', 'Discrete and Continuous Computer Algorithms', 3, ARRAY['Liberal Arts', 'Advanced']),
    (12, NULL, 'CPS425', '10012', 'Language Processing', 4, ARRAY['Liberal Arts', 'Advanced']),
    (13, NULL, 'CPS440', '10013', 'Database Principles', 3, ARRAY['Liberal Arts', 'Advanced']),
    (14, NULL, 'CPS470', '10014', 'Computer Communication Networks', 3, ARRAY['Liberal Arts', 'Advanced']),
    (15, NULL, 'CPS471', '10015', 'Computer Communication Networks II', 4, ARRAY['Liberal Arts', 'Advanced']),
    (16, NULL, 'CPS485', '10016', 'Projects', 4, ARRAY['Liberal Arts', 'Capstone']),
    (17, NULL, 'CPS493', '10017', 'Computer Science Selected Topic', 3, ARRAY['Liberal Arts', 'Elective']),
    
    -- MAT Courses (IDs 18-35)
    (18, NULL, 'MAT120', '20001', 'College Mathematics', 3, ARRAY['Liberal Arts', 'Math']),
    (19, NULL, 'MAT121', '20002', 'College Mathematics with Algebra Workshop', 3, ARRAY['Liberal Arts', 'Math']),
    (20, NULL, 'MAT140', '20003', 'Mathematics for Elementary Teachers I', 3, ARRAY['Liberal Arts', 'Math']),
    (21, NULL, 'MAT152', '20004', 'College Algebra', 3, ARRAY['Liberal Arts', 'Math']),
    (22, NULL, 'MAT181', '20005', 'Precalculus', 4, ARRAY['Liberal Arts', 'Math']),
    (23, NULL, 'MAT241', '20006', 'Introduction to Statistics', 3, ARRAY['Liberal Arts', 'Math']),
    (24, NULL, 'MAT251', '20007', 'Calculus I', 4, ARRAY['Liberal Arts', 'Math']),
    (25, NULL, 'MAT252', '20008', 'Calculus II', 4, ARRAY['Liberal Arts', 'Math']),
    (26, NULL, 'MAT260', '20009', 'Introduction to Proof', 3, ARRAY['Liberal Arts', 'Math']),
    (27, NULL, 'MAT304', '20010', 'Foundations of Analysis', 3, ARRAY['Liberal Arts', 'Math']),
    (28, NULL, 'MAT331', '20011', 'Axiomatic Geometry', 3, ARRAY['Liberal Arts', 'Math']),
    (29, NULL, 'MAT353', '20012', 'Calculus III', 4, ARRAY['Liberal Arts', 'Math']),
    (30, NULL, 'MAT359', '20013', 'Ordinary Differential Equations', 3, ARRAY['Liberal Arts', 'Math']),
    (31, NULL, 'MAT362', '20014', 'Linear Algebra', 3, ARRAY['Liberal Arts', 'Math']),
    (32, NULL, 'MAT364', '20015', 'Abstract Algebra I', 3, ARRAY['Liberal Arts', 'Math']),
    (33, NULL, 'MAT381', '20016', 'Probability and Statistics I', 3, ARRAY['Liberal Arts', 'Math']),
    (34, NULL, 'MAT431', '20017', 'Real Analysis I', 3, ARRAY['Liberal Arts', 'Math']),
    (35, NULL, 'MAT320', '20018', 'Discrete Mathematics for Computing', 3, ARRAY['Liberal Arts', 'Math']);

-- =========================================================
-- MAT PREREQUISITES
-- =========================================================

-- MAT 181 requires MAT 152
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (1, 22, 'COURSE', 21);
UPDATE courses SET root_prerequisite_node_id = 1 WHERE course_id = 22;

-- MAT 241 requires MAT 152 OR MAT 181
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES 
    (2, 23, 'OR', NULL),
    (3, 23, 'COURSE', 21),
    (4, 23, 'COURSE', 22);
INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order) VALUES 
    (2, 3, 1),
    (2, 4, 2);
UPDATE courses SET root_prerequisite_node_id = 2 WHERE course_id = 23;

-- MAT 251 requires MAT 152 OR MAT 181
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES 
    (5, 24, 'OR', NULL),
    (6, 24, 'COURSE', 21),
    (7, 24, 'COURSE', 22);
INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order) VALUES 
    (5, 6, 1),
    (5, 7, 2);
UPDATE courses SET root_prerequisite_node_id = 5 WHERE course_id = 24;

-- MAT 252 (Calc II) requires MAT 251
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (8, 25, 'COURSE', 24);
UPDATE courses SET root_prerequisite_node_id = 8 WHERE course_id = 25;

-- MAT 260 (Proof) requires MAT 251
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (9, 26, 'COURSE', 24);
UPDATE courses SET root_prerequisite_node_id = 9 WHERE course_id = 26;

-- MAT 353 (Calc III) requires MAT 252
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (10, 29, 'COURSE', 25);
UPDATE courses SET root_prerequisite_node_id = 10 WHERE course_id = 29;

-- MAT 359 (Differential Equations) requires MAT 353
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (11, 30, 'COURSE', 29);
UPDATE courses SET root_prerequisite_node_id = 11 WHERE course_id = 30;

-- MAT 362 (Linear Algebra) requires MAT 252
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (12, 31, 'COURSE', 25);
UPDATE courses SET root_prerequisite_node_id = 12 WHERE course_id = 31;

-- MAT 364 (Abstract Algebra) requires MAT 260 AND MAT 362
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES 
    (13, 32, 'AND', NULL),
    (14, 32, 'COURSE', 26),
    (15, 32, 'COURSE', 31);
INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order) VALUES 
    (13, 14, 1),
    (13, 15, 2);
UPDATE courses SET root_prerequisite_node_id = 13 WHERE course_id = 32;

-- MAT 381 (Probability/Statistics I) requires MAT 252
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (16, 33, 'COURSE', 25);
UPDATE courses SET root_prerequisite_node_id = 16 WHERE course_id = 33;

-- MAT 431 (Real Analysis) requires MAT 260 AND MAT 353
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES 
    (17, 34, 'AND', NULL),
    (18, 34, 'COURSE', 26),
    (19, 34, 'COURSE', 29);
INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order) VALUES 
    (17, 18, 1),
    (17, 19, 2);
UPDATE courses SET root_prerequisite_node_id = 17 WHERE course_id = 34;

-- MAT 320 requires MAT 181
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (46, 35, 'COURSE', 22);
UPDATE courses SET root_prerequisite_node_id = 46 WHERE course_id = 35;

-- =========================================================
-- CPS PREREQUISITES
-- =========================================================

-- CPS 210 requires MAT 152
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (20, 4, 'COURSE', 21);
UPDATE courses SET root_prerequisite_node_id = 20 WHERE course_id = 4;

-- CPS 310 requires CPS 210
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (23, 5, 'COURSE', 4);
UPDATE courses SET root_prerequisite_node_id = 23 WHERE course_id = 5;

-- CPS 315 requires CPS 310
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (24, 6, 'COURSE', 5);
UPDATE courses SET root_prerequisite_node_id = 24 WHERE course_id = 6;

-- CPS 330 requires CPS 310
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (25, 7, 'COURSE', 5);
UPDATE courses SET root_prerequisite_node_id = 25 WHERE course_id = 7;

-- CPS 340 requires CPS 330 (Syntax error fixed here)
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (26, 8, 'COURSE', 7);
UPDATE courses SET root_prerequisite_node_id = 26 WHERE course_id = 8;

-- CPS 352 requires CPS 310
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (29, 9, 'COURSE', 5);
UPDATE courses SET root_prerequisite_node_id = 29 WHERE course_id = 9;

-- CPS 353 requires CPS 310
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (30, 10, 'COURSE', 5);
UPDATE courses SET root_prerequisite_node_id = 30 WHERE course_id = 10;

-- CPS 415 requires MAT 320
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (31, 11, 'COURSE', 35);
UPDATE courses SET root_prerequisite_node_id = 31 WHERE course_id = 11;

-- CPS 425 requires CPS 310 AND CPS 330
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES 
    (34, 12, 'AND', NULL),
    (35, 12, 'COURSE', 5),
    (36, 12, 'COURSE', 7);
INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order) VALUES 
    (34, 35, 1),
    (34, 36, 2);
UPDATE courses SET root_prerequisite_node_id = 34 WHERE course_id = 12;

-- CPS 440 requires CPS 310
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (37, 13, 'COURSE', 5);
UPDATE courses SET root_prerequisite_node_id = 37 WHERE course_id = 13;

-- CPS 470 requires CPS 310
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (38, 14, 'COURSE', 5);
UPDATE courses SET root_prerequisite_node_id = 38 WHERE course_id = 14;

-- CPS 471 requires CPS 470
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES (39, 15, 'COURSE', 14);
UPDATE courses SET root_prerequisite_node_id = 39 WHERE course_id = 15;

-- CPS 485 requires CPS 440 OR CPS 470 OR CPS 493
INSERT INTO prerequisite_nodes (node_id, course_id, node_type, required_course_id) VALUES 
    (40, 16, 'OR', NULL),
    (41, 16, 'COURSE', 13),
    (42, 16, 'COURSE', 14),
    (43, 16, 'COURSE', 17);
INSERT INTO prerequisite_node_edges (parent_node_id, child_node_id, sort_order) VALUES 
    (40, 41, 1),
    (40, 42, 2),
    (40, 43, 3);
UPDATE courses SET root_prerequisite_node_id = 40 WHERE course_id = 16;


-- =========================================================
-- SAMPLE STUDENT COURSE RECORDS
-- =========================================================
INSERT INTO student_course_records
    (record_id, student_id, course_id, status, grade, semester_taken, year_taken)
VALUES
    (1, 1, 21, 'completed',   'A-',  'Spring',   2024), -- Completed MAT152 (College Algebra)
    (2, 1, 1, 'completed',   'A',  'Fall',   2024), -- Completed CPS100 (Computers and Algorithms)
    (3, 1, 2, 'completed',   'A-', 'Fall',   2024), -- Completed CPS104 (Visual Programming)
    (4, 1, 3, 'completed',   'B+', 'Spring', 2025), -- Completed CPS110 (Web Design)
    (5, 1, 22, 'completed',  'B',  'Spring', 2025), -- Completed MAT181 (Precalc)
    (6, 1, 4, 'completed',   'A',  'Spring', 2025), -- Completed CPS210 (Foundations)
    (7, 1, 5, 'in_progress', NULL, 'Fall',   2025), -- In progress CPS310 (Data Structures)
    (8, 1, 24, 'in_progress', NULL, 'Fall',  2025), -- In progress MAT251 (Calculus I)
    (9, 1, 11, 'planned',     NULL, 'Spring',2026); -- Planned CPS415 (Algorithms)

-- =========================================================
-- Reset sequences so future inserts continue correctly
-- =========================================================
SELECT setval('users_user_id_seq', COALESCE((SELECT MAX(user_id) FROM users), 1), true);
SELECT setval('courses_course_id_seq', COALESCE((SELECT MAX(course_id) FROM courses), 1), true);
SELECT setval('prerequisite_nodes_node_id_seq', COALESCE((SELECT MAX(node_id) FROM prerequisite_nodes), 1), true);
SELECT setval('student_course_records_record_id_seq', COALESCE((SELECT MAX(record_id) FROM student_course_records), 1), true);