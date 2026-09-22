package com.onlineexam.dao;

import com.onlineexam.db.DBConnection;
import com.onlineexam.model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Add, view, update and delete operations of the subjects table. */
public class SubjectDAO {

    public List<Subject> getAllSubjects() throws SQLException {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY subject_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Subject(rs.getInt("subject_id"),
                                     rs.getString("subject_name"),
                                     rs.getString("description")));
            }
        }
        return list;
    }

    public boolean addSubject(Subject subject) throws SQLException {
        String sql = "INSERT INTO subjects (subject_name, description) VALUES (?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getDescription());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateSubject(Subject subject) throws SQLException {
        String sql = "UPDATE subjects SET subject_name = ?, description = ? WHERE subject_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, subject.getSubjectName());
            ps.setString(2, subject.getDescription());
            ps.setInt(3, subject.getSubjectId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteSubject(int subjectId) throws SQLException {
        String sql = "DELETE FROM subjects WHERE subject_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subjectId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * A subject that still has examinations must not be deleted, otherwise
     * the foreign key in exams would break. The screen calls this first and
     * shows a message instead of attempting the delete.
     */
    public int countExams(int subjectId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM exams WHERE subject_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, subjectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }
}
