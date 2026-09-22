# Test Report — Online Examination System

This file records what was actually executed on the finished application and what the
runs produced. Nothing here is an expectation; every line is an outcome.

## Environment

| Item | Value |
|---|---|
| JDK | 21 |
| MySQL | 8.0, database `online_exam_db` |
| Build | Apache Maven 3.9, `online-examination-system.jar` |
| Screens driven under | a virtual X display, 1400x900 |

## Automated runs

| Test program | What it exercises | Checks | Passed | Failed |
|---|---|---|---|---|
| `FunctionalTest` | connection, login, registration, subjects, exams, questions, `ExamSession`, marking, the submit transaction, the rollback, the delete guards, validation, formatting | 166 | 166 | 0 |
| `DemoFlowTest` | the real Swing screens, driven by clicking buttons and typing into boxes | 95 | 95 | 0 |
| **Total** | | **261** | **261** | **0** |

`FunctionalTest` is grouped into 18 sections. `DemoFlowTest` follows one complete
story in 21 steps: the administrator logs in, adds a subject, is refused when creating
an examination with a bad duration, inspects questions and students, logs out; a new
student registers, logs in, writes a full examination, submits it, reads the answer
sheet, changes the profile; the administrator logs in again and finds that result and
is refused when trying to delete that student.

## Test cases

48 individual test cases (TC-01 to TC-48) are listed with their inputs, expected
results and actual results in Chapter 4 of
`docs/BCA_Project_Report_Online_Examination_System.docx`. All 48 were executed on the
finished application and all 48 behaved as expected.

## Defects found during testing and corrected

| # | Defect | Cause | Correction |
|---|---|---|---|
| 1 | The numbered buttons of the question palette were stretched over the whole height of the panel | the `GridLayout` holding them sat directly in the centre of a `BorderLayout`, so it was given all the free height | the grid was placed at the top of a holder panel |
| 2 | On Manage Exams the table columns were unreadably narrow and the form was cut off at the window edge | the form was to the right of the table, and no preferred column widths were set | the form was moved below the table and every table in the application was given preferred column widths |
| 3 | On the result window the summary floated in the middle and the "Correct Answer" column was cut short | the `GridBagLayout` had nothing to absorb the spare width, and the table columns had no preferred widths | an empty weighted third column was added, and the five answer-sheet columns were given widths |

All three were corrected and the full suite was run again afterwards.

Two further problems were found in the **test programs themselves**, not in the
application:

- one check compared the row counts of two question papers to prove that changing the
  examination reloaded the table, but both sample papers have eight questions, so the
  check could never fail — it now compares the text of the first question;
- one step deadlocked because it clicked a button that opens a modal window and then
  waited for the click to return, which cannot happen while the modal window is open —
  those buttons are now clicked without waiting.

## What was not tested

- The application was tested on the machine it was developed on. It was not run on
  several different computers or operating systems.
- Only one student wrote an examination at a time; simultaneous submissions were not
  tested.
- The MySQL server was not stopped in the middle of a submission, so that failure path
  was not exercised directly (the rollback itself was exercised by TC-24, which forces
  an insert to fail).
- The largest paper tested had eight questions; nothing can be said about papers with
  a hundred questions.
- Passwords are stored as plain text, so no password-security testing was possible.

## Honest conclusion

The application does everything described in the README and in Chapters 1 and 5 of the
report, and it does so on every one of the 261 automated checks and 48 test cases that
were run. That is not the same as saying it is free of every possible error, and this
report does not claim that.
