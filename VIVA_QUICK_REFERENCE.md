# Viva Quick Reference — Online Examination System

Short, honest answers to the questions that are most likely to be asked.
Every answer here matches what the code actually does.

---

## 1. Basics

**What is your project?**
A desktop application that lets a college conduct multiple choice examinations on a
computer. The administrator creates subjects, examinations and questions; a student
logs in, writes an examination against a countdown timer, and gets the marks and the
full answer sheet as soon as the paper is submitted.

**Which technologies did you use?**
Java 21 for the code, Java Swing for the screens, JDBC for the database connection,
MySQL 8.0 for the database and Apache Maven to build the executable JAR.

**Why a desktop application and not a website?**
The syllabus part I wanted to demonstrate is JDBC and SQL, especially transactions.
A Swing application lets me show that without adding a web server and a browser on
top of it.

**How many tables does your database have?**
Six: `users`, `subjects`, `exams`, `questions`, `results` and `answers`.
Six primary keys, six foreign keys and two unique keys.

**How many classes?**
Thirty Java classes in six packages: one main class, one connection class, seven model
classes, five DAO classes, fourteen screens and two helper classes.

---

## 2. Architecture

**Explain the architecture of your project.**
Three layers. The presentation layer is the fourteen Swing screens. The business layer
is the five DAO classes plus the model classes. The data access layer is `DBConnection`
and the JDBC classes. A screen never writes SQL — it checks what the user typed and
calls a DAO method.

**What is the DAO pattern?**
Data Access Object. All the database code for one table is put in one class, and the
rest of the application calls its methods. `UserDAO` holds every statement about the
`users` table, `ExamDAO` every statement about `exams`, and so on.

**Why is that better than writing SQL in the screens?**
Three reasons. If a query changes, only one method changes. The same method is reused
by several screens — `ResultDAO.getAnswerSheet()` is used both by the student result
window and by the administrator result screen. And the DAO classes can be tested from
a small program without opening any window, which is how the 166 database checks were
run.

**What are the model classes for?**
They carry one row of a table between the layers. `User` carries a row of `users`,
`Question` carries a row of `questions`. They have fields, getters and setters, and a
few small methods like `Exam.getTotalMarks()`.

---

## 3. ExamSession — the class they will ask about

**Which table does ExamSession map to?**
None. It is the only model class with no table behind it. It exists only in memory
while a student is writing a paper.

**What does it hold?**
The examination, the list of questions, the index of the question on the screen, and a
`LinkedHashMap<Integer, String>` that maps a question id to the option letter the
student selected.

**Why a map and not a list?**
Because a student can answer the questions in any order, can come back and change an
answer, and can clear an answer. A map keyed by question id handles all three:
selecting again replaces the value instead of adding a second one, and clearing is a
single `remove()`.

**When does it reach the database?**
Only when the paper is submitted. `ResultDAO.submitExam()` reads the map and turns it
into rows of the `answers` table.

---

## 4. The transaction — the most important question

**Which part of your project uses a transaction?**
`ResultDAO.submitExam()`. One finished attempt writes one row in `results` and one row
in `answers` for every question of the paper.

**Why does it need a transaction?**
Those rows belong to one attempt. A result row with only half of its answer rows would
be a wrong record. So either all of them are written or none of them is.

**Show me the three lines that make it a transaction.**
```java
con.setAutoCommit(false);   // nothing is saved on its own from here
   ... the INSERT statements ...
con.commit();               // everything is saved together
   ... or, in the catch block ...
con.rollback();             // everything is undone
```

**How do you get the result_id for the answer rows?**
The `results` row is inserted with `Statement.RETURN_GENERATED_KEYS`, and then
`ps.getGeneratedKeys()` gives back the `result_id` that MySQL generated. That id is
used as the foreign key of every `answers` row.

**Why not run a SELECT MAX(result_id) instead?**
Because between the INSERT and the SELECT another student could submit a paper, and
the answers would be attached to the wrong result. The generated key belongs to my own
statement, so it is always correct.

**What is addBatch and executeBatch?**
They collect several INSERT statements and send them to MySQL in one go. For an eight
question paper that is one round trip to the server instead of eight.

**Did you actually test the rollback?**
Yes. Test case TC-24. An attempt is submitted for an examination whose marks per
question is set so high that `marks_obtained` cannot fit in its column. The rows of
`results` and `answers` are counted before and after. The insert fails, the exception
is thrown, and both counts are exactly the same afterwards — nothing was left behind.

---

## 5. The examination screen

**How does the timer work?**
It is a `javax.swing.Timer` that fires once a second. A Swing timer runs its action on
the event dispatch thread, which is the only thread allowed to change the screen, so
the label can be updated straight from inside it. At one minute left the label changes
colour, and at zero the application submits the paper itself.

**Why not a normal Thread?**
A normal thread is not allowed to touch Swing components. I would have to wrap every
update in `SwingUtilities.invokeLater()`. The Swing timer does that for me.

**What are the numbered buttons on the right?**
The question palette. Green means the question has been answered, grey means it has
not, and the question on the screen has a blue outline. Clicking a number jumps to
that question.

**What happens if the student closes the window in the middle?**
The window asks whether to submit the paper as it is. It is never thrown away
silently — the close operation is set to `DO_NOTHING_ON_CLOSE` and a window listener
asks the question.

**How do you tell "wrong answer" apart from "not answered"?**
An unanswered question is stored as `NULL` in `answers.selected_option`, using
`ps.setNull(3, Types.CHAR)`. A wrong answer stores the letter that was chosen. So the
answer sheet can show "Not Attempted" for one and "Wrong" for the other.

---

## 6. Marking

**How are the marks calculated?**
`ExamSession.getCorrectCount()` walks through the questions and asks each one whether
the selected option is the right one. Marks obtained = correct answers × marks per
question. Percentage = marks obtained ÷ total marks × 100. Pass if the percentage is
40 or more.

**Where is the pass mark written?**
In one constant, `ResultDAO.PASSING_PERCENTAGE = 40.0`, so the rule appears in only one
place.

**Why does the results table store the marks if they could be recalculated?**
Because a result is a permanent record of what happened on that day. If the
administrator later changes the marks per question, or corrects a wrong answer key,
the old result must not change. Storing the calculated values keeps it safe.

---

## 7. Security and validation

**How do you stop SQL injection?**
Every statement in the project is a `PreparedStatement`. The SQL and the values are
sent separately, so a value can never change the meaning of the statement. Typing
`admin@exam.com' OR '1'='1` into the email box compares that whole string against the
email column as one value, and no row matches. That is test case TC-07.

**Can a student register as an administrator?**
No. The role is written into the SQL text as `'STUDENT'` in `UserDAO.register()`. It
is not taken from the screen at all.

**Are the passwords encrypted?**
No — and I would rather say so than pretend. They are stored as plain text. Hashing
them with something like BCrypt is the first item in my future improvements.

**What do you validate before saving?**
Email format, password length and matching, duration between 1 and 180 minutes, marks
per question between 1 and 10, question text not empty and at most 500 characters, all
four options filled, and the correct option being A, B, C or D.

**What happens if the user tries to delete a subject that has examinations?**
The screen calls `SubjectDAO.countExams()` first and shows a clear message with the
number. MySQL would also refuse because of the foreign key, but the user would see a
technical error instead of a sentence they can understand. The same guard exists for
examinations, questions and students.

---

## 8. Testing

**How did you test it?**
By hand first, then with two test programs. One calls the DAO classes directly — 166
checks. The other opens the real windows and clicks the real buttons — 95 steps. All
261 pass on the finished application.

**Did the testing find anything?**
Yes, three defects in the screens. The question palette buttons were being stretched
over the whole height by the layout manager. The Manage Exams table was too narrow and
its form was cut off at the window edge. And on the result window the summary floated
in the middle and one column was cut short. All three were corrected and the tests
were run again.

**What did you not test?**
It was tested on the computer it was developed on, not on several machines. Only one
student wrote an examination at a time. The largest paper tested had eight questions.
I cannot claim anything beyond that.

---

## 9. Future improvements

- Store the passwords as hashes instead of plain text
- Show the questions of a paper in a random order for each student
- Support descriptive questions as well as multiple choice
- Let the administrator export the result list to Excel or PDF
- Add a graph of the marks of a class
- Move the database to a server so that several computers can use one database

---

## 10. If you are asked something you do not know

Say so, and say what you would do to find out. It is a far better answer than a guess.
For example: *"I have not measured that. I would add a timestamp before and after the
call and print the difference to find out."*
