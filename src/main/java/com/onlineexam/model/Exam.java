package com.onlineexam.model;

/**
 * Holds one row of the exams table.
 * subjectName and questionCount are not columns of the table. They are
 * filled by a join and a count in ExamDAO so that the screens can show
 * them without running another query.
 */
public class Exam {

    private int examId;
    private String examTitle;
    private int subjectId;
    private String subjectName;
    private int durationMinutes;
    private int marksPerQuestion;
    private String status;
    private int questionCount;

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public int getMarksPerQuestion() { return marksPerQuestion; }
    public void setMarksPerQuestion(int marksPerQuestion) { this.marksPerQuestion = marksPerQuestion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }

    /** Total marks of the examination = number of questions x marks of one question. */
    public int getTotalMarks() {
        return questionCount * marksPerQuestion;
    }

    @Override
    public String toString() {
        return examTitle;
    }
}
