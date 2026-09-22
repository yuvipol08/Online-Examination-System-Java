package com.onlineexam.model;

/**
 * Holds one row of the answers table : which option the student
 * selected for one question and whether it was correct.
 */
public class AnswerRecord {

    private int answerId;
    private int resultId;
    private int questionId;
    private String questionText;
    private String selectedOption;
    private String correctOption;
    private String isCorrect;

    public AnswerRecord() {
    }

    public AnswerRecord(int questionId, String selectedOption, boolean correct) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.isCorrect = correct ? "Yes" : "No";
    }

    public int getAnswerId() { return answerId; }
    public void setAnswerId(int answerId) { this.answerId = answerId; }

    public int getResultId() { return resultId; }
    public void setResultId(int resultId) { this.resultId = resultId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getSelectedOption() { return selectedOption; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public String getIsCorrect() { return isCorrect; }
    public void setIsCorrect(String isCorrect) { this.isCorrect = isCorrect; }

    /** Shows a dash on the answer sheet when the question was left unanswered. */
    public String getSelectedOptionForDisplay() {
        return selectedOption == null || selectedOption.isBlank() ? "-" : selectedOption;
    }
}
