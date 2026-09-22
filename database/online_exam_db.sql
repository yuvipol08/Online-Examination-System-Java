-- ============================================================
--  Online Examination System
--  Database script for MySQL
--
--  How to run:
--      mysql -u root -p < online_exam_db.sql
--  or open this file in MySQL Workbench and execute it.
-- ============================================================

DROP DATABASE IF EXISTS online_exam_db;
CREATE DATABASE online_exam_db;
USE online_exam_db;

-- ------------------------------------------------------------
-- Table 1 : users
-- Stores the login details of the administrator and of every
-- student. The role column decides which dashboard opens after
-- a successful login.
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id     INT AUTO_INCREMENT,
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(50)  NOT NULL,
    roll_number VARCHAR(20),
    course      VARCHAR(50),
    role        VARCHAR(10)  NOT NULL DEFAULT 'STUDENT',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_users_email (email)
);

-- ------------------------------------------------------------
-- Table 2 : subjects
-- Stores the subjects under which examinations are created,
-- for example Java Programming or Database Management.
-- ------------------------------------------------------------
CREATE TABLE subjects (
    subject_id   INT AUTO_INCREMENT,
    subject_name VARCHAR(60) NOT NULL,
    description  VARCHAR(255),
    PRIMARY KEY (subject_id),
    UNIQUE KEY uk_subjects_name (subject_name)
);

-- ------------------------------------------------------------
-- Table 3 : exams
-- One row for every examination created by the administrator.
-- Every examination belongs to one subject.
-- ------------------------------------------------------------
CREATE TABLE exams (
    exam_id            INT AUTO_INCREMENT,
    exam_title         VARCHAR(100) NOT NULL,
    subject_id         INT          NOT NULL,
    duration_minutes   INT          NOT NULL,
    marks_per_question INT          NOT NULL DEFAULT 1,
    status             VARCHAR(10)  NOT NULL DEFAULT 'Active',
    PRIMARY KEY (exam_id),
    CONSTRAINT fk_exam_subject FOREIGN KEY (subject_id)
        REFERENCES subjects (subject_id)
);

-- ------------------------------------------------------------
-- Table 4 : questions
-- Stores the multiple choice questions of an examination with
-- its four options and the letter of the correct option.
-- ------------------------------------------------------------
CREATE TABLE questions (
    question_id    INT AUTO_INCREMENT,
    exam_id        INT          NOT NULL,
    question_text  VARCHAR(500) NOT NULL,
    option_a       VARCHAR(200) NOT NULL,
    option_b       VARCHAR(200) NOT NULL,
    option_c       VARCHAR(200) NOT NULL,
    option_d       VARCHAR(200) NOT NULL,
    correct_option CHAR(1)      NOT NULL,
    PRIMARY KEY (question_id),
    CONSTRAINT fk_question_exam FOREIGN KEY (exam_id)
        REFERENCES exams (exam_id)
);

-- ------------------------------------------------------------
-- Table 5 : results
-- One row for every examination attempted by a student. It
-- keeps the marks, the percentage and whether the student
-- passed or failed.
-- ------------------------------------------------------------
CREATE TABLE results (
    result_id       INT AUTO_INCREMENT,
    exam_id         INT          NOT NULL,
    user_id         INT          NOT NULL,
    exam_date       DATETIME     NOT NULL,
    total_questions INT          NOT NULL,
    attempted       INT          NOT NULL,
    correct_answers INT          NOT NULL,
    marks_obtained  INT          NOT NULL,
    total_marks     INT          NOT NULL,
    percentage      DECIMAL(5,2) NOT NULL,
    status          VARCHAR(10)  NOT NULL,
    PRIMARY KEY (result_id),
    CONSTRAINT fk_result_exam FOREIGN KEY (exam_id)
        REFERENCES exams (exam_id),
    CONSTRAINT fk_result_user FOREIGN KEY (user_id)
        REFERENCES users (user_id)
);

-- ------------------------------------------------------------
-- Table 6 : answers
-- Stores the answer the student selected for every question of
-- an attempt. One result has as many rows here as there were
-- questions in the examination.
-- ------------------------------------------------------------
CREATE TABLE answers (
    answer_id       INT AUTO_INCREMENT,
    result_id       INT        NOT NULL,
    question_id     INT        NOT NULL,
    selected_option CHAR(1),
    is_correct      VARCHAR(3) NOT NULL,
    PRIMARY KEY (answer_id),
    CONSTRAINT fk_answer_result FOREIGN KEY (result_id)
        REFERENCES results (result_id),
    CONSTRAINT fk_answer_question FOREIGN KEY (question_id)
        REFERENCES questions (question_id)
);

-- ============================================================
--  Sample data
-- ============================================================

-- Default administrator account
INSERT INTO users (full_name, email, password, roll_number, course, role) VALUES
('System Administrator', 'admin@exam.com', 'admin123', NULL, NULL, 'ADMIN');

-- Two demo students so the examination and result screens can be checked
INSERT INTO users (full_name, email, password, roll_number, course, role) VALUES
('Sneha Kulkarni', 'student@exam.com', 'stud123', 'TY001', 'T.Y. BCA', 'STUDENT'),
('Ankita Deshmukh', 'ankita@exam.com', 'ankita123', 'TY002', 'T.Y. BCA', 'STUDENT');

INSERT INTO subjects (subject_name, description) VALUES
('Java Programming',    'Core Java, classes, objects and exception handling'),
('Database Management', 'SQL, keys, joins and normalisation'),
('Computer Networks',   'Network layers, protocols and addressing');

INSERT INTO exams (exam_title, subject_id, duration_minutes, marks_per_question, status) VALUES
('Java Basics Test',      1, 10, 1, 'Active'),
('SQL Fundamentals Test', 2, 10, 1, 'Active'),
('Networking Basics Test',3, 10, 1, 'Inactive');

-- Questions of the Java Basics Test
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
(1, 'Which keyword is used to create an object in Java?', 'new', 'create', 'object', 'make', 'A'),
(1, 'Which method is the starting point of a Java program?', 'start()', 'main()', 'run()', 'init()', 'B'),
(1, 'Which of these is NOT a primitive data type in Java?', 'int', 'double', 'String', 'boolean', 'C'),
(1, 'Which keyword is used for inheritance in Java?', 'implement', 'inherits', 'super', 'extends', 'D'),
(1, 'Which block is always executed whether an exception occurs or not?', 'finally', 'catch', 'throw', 'try', 'A'),
(1, 'What is the size of an int variable in Java?', '2 bytes', '4 bytes', '8 bytes', '1 byte', 'B'),
(1, 'Which collection class allows duplicate values and keeps the order?', 'HashSet', 'TreeSet', 'ArrayList', 'HashMap', 'C'),
(1, 'Which keyword makes a variable belong to the class and not to an object?', 'final', 'private', 'void', 'static', 'D');

-- Questions of the SQL Fundamentals Test
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
(2, 'Which SQL command is used to add a new row to a table?', 'INSERT', 'UPDATE', 'SELECT', 'ALTER', 'A'),
(2, 'Which key uniquely identifies every row of a table?', 'Foreign Key', 'Primary Key', 'Unique Key', 'Index', 'B'),
(2, 'Which clause is used to filter the rows of a SELECT query?', 'ORDER BY', 'GROUP BY', 'WHERE', 'HAVING', 'C'),
(2, 'Which command removes all rows but keeps the table structure?', 'DROP', 'DELETE', 'REMOVE', 'TRUNCATE', 'D'),
(2, 'Which key points at the primary key of another table?', 'Foreign Key', 'Candidate Key', 'Super Key', 'Composite Key', 'A'),
(2, 'Which JOIN returns only the rows matching in both tables?', 'LEFT JOIN', 'INNER JOIN', 'RIGHT JOIN', 'FULL JOIN', 'B'),
(2, 'Which SQL function counts the number of rows?', 'SUM()', 'TOTAL()', 'COUNT()', 'NUMBER()', 'C'),
(2, 'Which command is used to change data that already exists in a table?', 'INSERT', 'SELECT', 'ALTER', 'UPDATE', 'D');

-- Questions of the Networking Basics Test
INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES
(3, 'How many layers are there in the OSI model?', '7', '5', '4', '6', 'A'),
(3, 'Which protocol is used to send email?', 'FTP', 'SMTP', 'HTTP', 'SNMP', 'B'),
(3, 'What is the default port number of HTTP?', '21', '25', '80', '443', 'C'),
(3, 'Which device connects two different networks together?', 'Hub', 'Switch', 'Repeater', 'Router', 'D');
