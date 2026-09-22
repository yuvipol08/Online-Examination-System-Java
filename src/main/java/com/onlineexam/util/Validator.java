package com.onlineexam.util;

/**
 * Small helper methods used by the forms to check what the user typed
 * before the value is sent to the database.
 */
public class Validator {

    /** Longest examination allowed, in minutes. */
    public static final int MAX_DURATION = 180;

    /** Largest number of marks that one question can carry. */
    public static final int MAX_MARKS = 10;

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /** Accepts a normal email such as name@example.com */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * The duration must be a whole number of minutes from 1 to MAX_DURATION.
     */
    public static boolean isValidDuration(String minutes) {
        try {
            int value = Integer.parseInt(minutes.trim());
            return value > 0 && value <= MAX_DURATION;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * The marks of one question must be a whole number from 1 to MAX_MARKS.
     */
    public static boolean isValidMarks(String marks) {
        try {
            int value = Integer.parseInt(marks.trim());
            return value > 0 && value <= MAX_MARKS;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /** The correct option must be one of the four letters A, B, C or D. */
    public static boolean isValidOption(String option) {
        return option != null && option.matches("[ABCDabcd]");
    }
}
