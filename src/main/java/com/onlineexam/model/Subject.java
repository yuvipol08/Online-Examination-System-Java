package com.onlineexam.model;

/** Holds one row of the subjects table. */
public class Subject {

    private int subjectId;
    private String subjectName;
    private String description;

    public Subject() {
    }

    public Subject(int subjectId, String subjectName, String description) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.description = description;
    }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Combo boxes display whatever toString() returns, so returning the
     * subject name shows a readable list instead of the object address.
     */
    @Override
    public String toString() {
        return subjectName;
    }
}
