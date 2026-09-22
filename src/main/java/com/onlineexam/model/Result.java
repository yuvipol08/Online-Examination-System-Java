package com.onlineexam.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds one row of the results table together with the answers of
 * that attempt. studentName and examTitle are filled by a join in
 * ResultDAO so the screens can show them.
 */
public class Result {

    private int resultId;
    private int examId;
    private String examTitle;
    private int userId;
    private String studentName;
    private String rollNumber;
    private String examDate;
    private int totalQuestions;
    private int attempted;
    private int correctAnswers;
    private int marksObtained;
    private int totalMarks;
    private double percentage;
    private String status;

    private List<AnswerRecord> answers = new ArrayList<>();

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getExamId() { return examId; }
    public void setExamId(int examId) { this.examId = examId; }

    public String getExamTitle() { return examTitle; }
    public void setExamTitle(String examTitle) { this.examTitle = examTitle; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public int getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }

    public int getAttempted() { return attempted; }
    public void setAttempted(int attempted) { this.attempted = attempted; }

    public int getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public int getMarksObtained() { return marksObtained; }
    public void setMarksObtained(int marksObtained) { this.marksObtained = marksObtained; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public double getPercentage() { return percentage; }
    public void setPercentage(double percentage) { this.percentage = percentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<AnswerRecord> getAnswers() { return answers; }
    public void setAnswers(List<AnswerRecord> answers) { this.answers = answers; }

    /** Number of questions the student did not answer at all. */
    public int getNotAttempted() {
        return totalQuestions - attempted;
    }

    /** Number of answered questions that were wrong. */
    public int getWrongAnswers() {
        return attempted - correctAnswers;
    }
}
