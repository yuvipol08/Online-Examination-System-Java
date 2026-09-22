package com.onlineexam.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of one examination while the student is writing it.
 * It holds the questions, the option selected for each of them and
 * which question is currently on the screen. It exists only in
 * memory, and it is written to the database when the paper is
 * submitted.
 */
public class ExamSession {

    private final Exam exam;
    private final List<Question> questions;

    /** question id -> the option letter the student selected */
    private final Map<Integer, String> selectedOptions = new LinkedHashMap<>();

    private int currentIndex = 0;

    public ExamSession(Exam exam, List<Question> questions) {
        this.exam = exam;
        this.questions = questions;
    }

    public Exam getExam() { return exam; }

    public List<Question> getQuestions() { return questions; }

    public int getTotalQuestions() { return questions.size(); }

    public int getCurrentIndex() { return currentIndex; }

    public Question getCurrentQuestion() { return questions.get(currentIndex); }

    public boolean hasNext() { return currentIndex < questions.size() - 1; }

    public boolean hasPrevious() { return currentIndex > 0; }

    public void next() {
        if (hasNext()) {
            currentIndex++;
        }
    }

    public void previous() {
        if (hasPrevious()) {
            currentIndex--;
        }
    }

    public void goTo(int index) {
        if (index >= 0 && index < questions.size()) {
            currentIndex = index;
        }
    }

    /** Saves the option the student selected for the question on the screen. */
    public void selectOption(String optionLetter) {
        selectedOptions.put(getCurrentQuestion().getQuestionId(), optionLetter);
    }

    /** Removes the answer of the current question when the student clears it. */
    public void clearCurrentAnswer() {
        selectedOptions.remove(getCurrentQuestion().getQuestionId());
    }

    /** The option selected for a question, or null when it was not answered. */
    public String getSelectedOption(int questionId) {
        return selectedOptions.get(questionId);
    }

    public boolean isAnswered(int questionId) {
        return selectedOptions.containsKey(questionId);
    }

    public int getAnsweredCount() {
        return selectedOptions.size();
    }

    public int getNotAnsweredCount() {
        return questions.size() - selectedOptions.size();
    }

    /** Counts how many of the selected options are the correct ones. */
    public int getCorrectCount() {
        int correct = 0;
        for (Question question : questions) {
            String selected = selectedOptions.get(question.getQuestionId());
            if (question.isCorrect(selected)) {
                correct++;
            }
        }
        return correct;
    }
}
