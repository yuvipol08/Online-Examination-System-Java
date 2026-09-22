# Student Guide — Online Examination System

This guide explains the project the way you would explain it to a friend: what each
file does, why it was written that way, and what happens step by step when the
application runs. Read it once from top to bottom and you will be able to explain any
part of the project.

---

## Part 1 — Getting it running

### 1.1 What you need

- **JDK 21** — check with `java -version`
- **MySQL 8.0** — the server must be running
- **Maven 3.9** — check with `mvn -version`

### 1.2 The three commands

```bash
# 1. create the database and load the sample data
mysql -u root -p < database/online_exam_db.sql

# 2. build the application
mvn clean package

# 3. run it
java -jar target/online-examination-system.jar
```

### 1.3 If it does not start

| What you see | What it means | What to do |
|---|---|---|
| "Could not connect to the MySQL database" | MySQL is not running, or the user name and password are different | Start MySQL. Then check `DBConnection.java` |
| "Unknown database 'online_exam_db'" | The script was never run | Run step 1 again |
| `mvn` is not recognised | Maven is not installed or not on the PATH | Install Maven and add it to the PATH |
| The window opens but every table is empty | The script ran but the sample data did not | Run the script again; it drops and recreates the database |

### 1.4 The accounts

| Role | Email | Password |
|---|---|---|
| Administrator | admin@exam.com | admin123 |
| Student | student@exam.com | stud123 |
| Student | ankita@exam.com | ankita123 |

---

## Part 2 — The shape of the project

### 2.1 Three layers

```
        the screens  (com.onlineexam.ui)
                 |  calls methods
                 v
        the DAO classes  (com.onlineexam.dao)
                 |  runs SQL through JDBC
                 v
        MySQL  (online_exam_db)
```

There is one rule, and everything else follows from it:

> **A screen never writes SQL.** It reads what the user typed, checks it, and calls a
> DAO method.

If someone asks you at the viva why the project is arranged this way, the honest
answer is: so that a change to a query touches one file, so that the same query can be
reused by several screens, and so that the database code can be tested without opening
a window.

### 2.2 Every file and what it does

**`MainApp.java`** — the starting point. It sets the look and feel of the operating
system, checks that MySQL can be reached, and opens the login window. If the database
is not reachable it shows a message that says exactly what to check, instead of
throwing a stack trace at the user.

**`db/DBConnection.java`** — the only class in the whole project that knows the
database URL, the user name and the password. That is deliberate: moving the database
to another computer means changing one file.

**The model classes (`model/`)** — each one carries one row of one table.

| Class | Table | Worth knowing |
|---|---|---|
| `User` | `users` | `isAdmin()` reads the `role` column |
| `Subject` | `subjects` | `toString()` returns the name, so combo boxes look right |
| `Exam` | `exams` | `getTotalMarks()` = questions × marks per question |
| `Question` | `questions` | `getOption("B")` returns option B; `isCorrect()` compares |
| `Result` | `results` | `getNotAttempted()` and `getWrongAnswers()` are worked out |
| `AnswerRecord` | `answers` | `getSelectedOptionForDisplay()` returns "-" for unanswered |
| `ExamSession` | **none** | the paper being written, held in memory |

**The DAO classes (`dao/`)** — all the SQL of the project lives here.

| Class | Looks after | The method to know |
|---|---|---|
| `UserDAO` | `users` | `login()`, `register()` |
| `SubjectDAO` | `subjects` | `countExams()` guards the delete |
| `ExamDAO` | `exams` | `getAvailableExams()`, `deleteExam()` |
| `QuestionDAO` | `questions` | `getQuestionsByExam()` |
| `ResultDAO` | `results` + `answers` | **`submitExam()`** — read this one first |

**The screens (`ui/`)** — fourteen of them.

| Class | What it is |
|---|---|
| `LoginFrame` | first window; the role decides which dashboard opens |
| `RegisterFrame` | a modal dialog for creating a student account |
| `AdminDashboard` | menu on the left, one panel at a time on the right |
| `StudentDashboard` | the same idea with three menu entries |
| `SubjectPanel` | table plus form for subjects |
| `ExamPanel` | table plus form for examinations |
| `QuestionPanel` | choose an examination, edit its questions |
| `StudentListPanel` | the registered students |
| `AdminResultPanel` | every attempt of every student |
| `AvailableExamPanel` | the examinations a student may write |
| `ExamWindow` | **the examination itself** — timer, palette, options |
| `ResultDialog` | marks and the full answer sheet |
| `MyResultsPanel` | the student's own attempts |
| `ProfilePanel` | the student's own details |

**The helpers (`util/`)** — `Validator` holds the input checks that more than one
screen needs. `UITheme` holds the colours, the fonts and three small helpers:
`createButton()`, `percent()` for formatting a percentage, `formatTime()` for the
mm:ss of the timer and `setSizeWithinScreen()` so a window is never taller than the
screen.

---

## Part 3 — The database

### 3.1 The six tables

```
subjects ──1:N──> exams ──1:N──> questions
                    |                 |
                   1:N               1:N
                    v                 v
users ──1:N──>   results ──1:N──>  answers
```

### 3.2 Why the results table stores numbers it could recalculate

`results` keeps `total_questions`, `attempted`, `correct_answers`, `marks_obtained`,
`total_marks` and `percentage`. All of them could be worked out again from `answers`.
They are stored on purpose.

A result is a record of what happened on a particular day. If the administrator later
changes the marks per question, or fixes a wrong answer key, the old results must not
change. Keeping the calculated values in the row makes the old result permanent. This
is a good thing to say in a viva, because it shows you thought about it rather than
just copying columns.

### 3.3 Why selected_option can be NULL

It is the only nullable column in the database. `NULL` means the student never answered
that question. An empty string would be a value, and would not be the same thing. This
is what lets the answer sheet print **Not Attempted** instead of **Wrong**.

---

## Part 4 — What happens, step by step

### 4.1 Logging in

1. You type an email and a password into `LoginFrame` and click Login.
2. `doLogin()` checks that neither box is empty.
3. It calls `userDAO.login(email, password)`.
4. `UserDAO` runs
   `SELECT * FROM users WHERE email = ? AND password = ?` through a `PreparedStatement`.
5. No row → `null` comes back → the screen shows one message for both a wrong email
   and a wrong password, so nobody can find out which accounts exist.
6. A row → a `User` object comes back → `user.isAdmin()` decides which dashboard opens.

### 4.2 Writing an examination

1. `AvailableExamPanel` lists the examinations from `ExamDAO.getAvailableExams()`.
   That query has two conditions: the status must be `Active` **and** a subquery must
   count at least one question. So an empty paper never appears.
2. You select one and click Start Exam. A confirmation appears, because the timer
   starts as soon as the window opens.
3. `QuestionDAO.getQuestionsByExam()` loads the paper.
4. A `new ExamSession(exam, questions)` is created — this is the paper in memory.
5. `ExamWindow` opens as a **modal** dialog, so the dashboard behind it cannot be used.
6. `startCountdown()` starts a `javax.swing.Timer` that fires once a second.
7. `showQuestion()` puts the current question and its four options on the screen, and
   re-selects the option you chose earlier if you are coming back to this question.
8. Choosing an option calls `session.selectOption(letter)`, which puts the letter into
   the map under that question's id. The numbered button turns green.
9. Submit Exam (or the timer reaching zero) calls `submitExam()`.

### 4.3 Submitting — the important part

```java
// 1. count and calculate  (still in memory, nothing touched yet)
int correctAnswers = session.getCorrectCount();
int marksObtained  = correctAnswers * exam.getMarksPerQuestion();
double percentage  = marksObtained * 100.0 / totalMarks;
String status      = percentage >= PASSING_PERCENTAGE ? "Pass" : "Fail";

// 2. start the transaction
con.setAutoCommit(false);

// 3. the results row, and read back the id MySQL gave it
ps = con.prepareStatement(INSERT_RESULT, Statement.RETURN_GENERATED_KEYS);
...
resultId = keys.getInt(1);

// 4. one answers row per question, all sent in one batch
for (Question question : questions) {
    String selected = session.getSelectedOption(question.getQuestionId());
    if (selected == null) ps.setNull(3, Types.CHAR);   // never answered
    else                  ps.setString(3, selected);
    ps.setString(4, question.isCorrect(selected) ? "Yes" : "No");
    ps.addBatch();
}
ps.executeBatch();

// 5. save everything together, or undo everything
con.commit();
// catch (SQLException e) { con.rollback(); throw e; }
```

Four things in that code are worth being able to explain:

1. **`setAutoCommit(false)`** — without it, every INSERT would be saved on its own and
   a failure half way would leave a broken attempt in the database.
2. **`RETURN_GENERATED_KEYS`** — gives the `result_id` of the row just inserted. Using
   `SELECT MAX(result_id)` instead would be wrong, because another student could
   submit between the two statements.
3. **`addBatch()` / `executeBatch()`** — one round trip to MySQL instead of one per
   question.
4. **`setNull(3, Types.CHAR)`** — stores "never answered", which is not the same as
   "answered wrongly".

---

## Part 5 — Things you should be ready to defend

**"Why is the password not encrypted?"**
It is not, and the report says so openly in the scope and in the future improvements.
Plain text keeps a first database project simple. If you are asked how you would fix
it: store a BCrypt hash instead of the password, and compare the typed password
against the hash at login instead of using it in the WHERE clause.

**"Why did you not use JavaFX / Hibernate / Spring?"**
Because the point of the project is to show JDBC and SQL directly. A framework would
hide exactly the part that is being assessed.

**"Is this a real transaction or did you just write the word?"**
It is real, and it was tested on purpose. Test case TC-24 forces the insert to fail by
making `marks_obtained` too large for its column, then counts the rows of both tables
before and after. Both counts are identical, so nothing was left behind.

**"What would you do differently next time?"**
Honest answers that hold up: hash the passwords; put the question order in a separate
column so a paper can be shuffled per student; add an index on `results.user_id`
because My Results always filters on it.

---

## Part 6 — Before the submission

- [ ] MySQL is running and `online_exam_db` exists
- [ ] `mvn clean package` finishes without an error
- [ ] `java -jar target/online-examination-system.jar` opens the login window
- [ ] You can log in as the administrator and as a student
- [ ] You can write a full examination and see the result
- [ ] The report is printed and the blank lines on the cover, the certificate and the
      declaration are filled in
- [ ] You have read `VIVA_QUICK_REFERENCE.md` at least twice
