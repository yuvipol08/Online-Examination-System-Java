package com.onlineexam.dao;

import com.onlineexam.db.DBConnection;
import com.onlineexam.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * All database work of the users table : login, student registration,
 * the student list used by the admin and the profile update.
 */
public class UserDAO {

    /**
     * Checks the email and password against the users table.
     * Returns the User object on success and null when the
     * credentials do not match any row.
     */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildUser(rs);
                }
            }
        }
        return null;
    }

    /** Used during registration to stop two accounts with the same email. */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT user_id FROM users WHERE email = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Inserts a new student. The role is always STUDENT here. */
    public boolean register(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, password, roll_number, course, role) "
                   + "VALUES (?, ?, ?, ?, ?, 'STUDENT')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRollNumber());
            ps.setString(5, user.getCourse());

            return ps.executeUpdate() > 0;
        }
    }

    /** The list of students shown to the admin on the Manage Students screen. */
    public List<User> getAllStudents() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'STUDENT' ORDER BY user_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(buildUser(rs));
            }
        }
        return list;
    }

    /** Saves the changes made by the student on the My Profile screen. */
    public boolean updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, roll_number = ?, course = ?, "
                   + "password = ? WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user.getFullName());
            ps.setString(2, user.getRollNumber());
            ps.setString(3, user.getCourse());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteStudent(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ? AND role = 'STUDENT'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * A student who has already written an examination must not be deleted,
     * because the results table has a foreign key pointing at the student.
     */
    public int countResults(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM results WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /** Copies one row of the result set into a User object. */
    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRollNumber(rs.getString("roll_number"));
        user.setCourse(rs.getString("course"));
        user.setRole(rs.getString("role"));
        return user;
    }
}
