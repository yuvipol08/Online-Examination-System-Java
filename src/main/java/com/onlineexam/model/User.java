package com.onlineexam.model;

/**
 * Holds the details of one row of the users table.
 * The same class is used for the administrator and for students,
 * the role field tells them apart.
 */
public class User {

    private int userId;
    private String fullName;
    private String email;
    private String password;
    private String rollNumber;
    private String course;
    private String role;

    public User() {
    }

    public User(String fullName, String email, String password,
                String rollNumber, String course, String role) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.rollNumber = rollNumber;
        this.course = course;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
