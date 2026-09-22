# Online Examination System

A desktop application that lets a college conduct multiple choice examinations on a
computer instead of on paper. It is written in **Java (Swing)** and stores everything
in a **MySQL** database through **JDBC**.

Built as a T.Y. BCA project.

---

## What the application does

**The administrator can**

- log in with the same screen the students use
- add, update and delete **subjects**
- create **examinations** under a subject, with a duration, marks per question and a
  status of Active or Inactive
- add, update and delete the **questions** of an examination, each with four options
  and the letter of the correct answer
- see the list of **registered students**
- see the **result of every attempt** of every student, and open the full answer sheet

**A student can**

- register an account from the login screen
- see the examinations that are **open** (Active and containing at least one question)
- **write an examination**: one question at a time, four options, a countdown timer,
  a numbered palette showing which questions are answered, and free movement between
  the questions
- **submit** the paper, or let the timer submit it automatically when the time is over
- see the **marks, percentage and pass/fail status** immediately, together with the
  full answer sheet
- look at any **older result** again
- change the own name, roll number, course and password

---

## Requirements

| Software | Version used |
|---|---|
| JDK | 21 |
| MySQL Community Server | 8.0 |
| Apache Maven | 3.9 |
| MySQL Connector/J | 8.4.0 (downloaded by Maven) |

---

## How to run it

**1. Create the database**

```bash
mysql -u root -p < database/online_exam_db.sql
```

or open `database/online_exam_db.sql` in MySQL Workbench and execute it. The script
creates the database `online_exam_db`, its six tables and the sample data.

**2. Check the database settings**

The user name and the password are in one file only:
`src/main/java/com/onlineexam/db/DBConnection.java`. Change them there if your MySQL
uses different ones.

**3. Build**

```bash
mvn clean package
```

**4. Run**

```bash
java -jar target/online-examination-system.jar
```

If MySQL is not running, the application says so clearly instead of crashing.

---

## Sample accounts

| Role | Email | Password |
|---|---|---|
| Administrator | admin@exam.com | admin123 |
| Student | student@exam.com | stud123 |
| Student | ankita@exam.com | ankita123 |

The sample data contains three subjects, three examinations (two Active, one Inactive)
and twenty questions.

---

## How the code is arranged

```
src/main/java/com/onlineexam/
├── MainApp.java              checks the database and opens the login window
├── db/
│   └── DBConnection.java     the only class that knows the database URL
├── model/                    one class per table, plus ExamSession
│   ├── User.java  Subject.java  Exam.java  Question.java
│   ├── Result.java  AnswerRecord.java
│   └── ExamSession.java      the paper being written, held in memory
├── dao/                      every SQL statement of the project
│   ├── UserDAO.java  SubjectDAO.java  ExamDAO.java
│   ├── QuestionDAO.java
│   └── ResultDAO.java        contains submitExam(), the main transaction
├── ui/                       the fourteen Swing screens
└── util/
    ├── Validator.java        the input checks
    └── UITheme.java          colours, fonts and formatting helpers
```

The application is built in three layers. A screen never writes SQL: it reads what the
user typed, checks it, and calls a DAO. The DAO runs the statement through JDBC and
returns model objects.

---

## The part worth reading first

`ResultDAO.submitExam()` is the heart of the project. One finished attempt produces
one row in `results` and one row in `answers` for every question of the paper. Those
rows belong together, so they are written inside a single transaction:

```java
con.setAutoCommit(false);
// INSERT INTO results ... RETURN_GENERATED_KEYS -> result_id
// INSERT INTO answers ... addBatch() per question -> executeBatch()
con.commit();          // or con.rollback() if anything above failed
```

If any statement fails, the whole attempt is rolled back and the database is left
exactly as it was before the student pressed Submit.

---

## Database

Six tables:

| Table | What it holds |
|---|---|
| `users` | the administrator and the students, with a `role` column |
| `subjects` | the subjects under which examinations are created |
| `exams` | one row per examination, with duration, marks and status |
| `questions` | the questions of an examination with four options and the correct one |
| `results` | one row per finished attempt, with the marks and the percentage |
| `answers` | one row per question of an attempt, with the option the student chose |

Six primary keys, six foreign keys, two unique keys. The ER diagram is in
`docs/diagrams/er_diagram.png`.

---

## Testing

The application was tested by hand and with two test programs:

| Test program | Checks | Passed |
|---|---|---|
| Database layer (DAO classes called directly) | 166 | 166 |
| User interface (the real Swing screens, driven by clicks) | 95 | 95 |
| **Total** | **261** | **261** |

Chapter 4 of the project report lists 48 individual test cases with their results, the
three defects that were found during testing and corrected, and an honest list of what
was **not** tested.

---

## Documentation

| File | Contents |
|---|---|
| `docs/BCA_Project_Report_Online_Examination_System.docx` | the full project report (43 pages) |
| `docs/diagrams/` | architecture, use case, class, ER, sequence and activity diagrams |
| `docs/screenshots/` | screenshots of every screen, taken from the running application |
| `STUDENT_GUIDE.md` | how the project works, explained file by file |
| `VIVA_QUICK_REFERENCE.md` | short answers to the questions most likely to be asked |
