package com.onlineexam.dao;

import com.onlineexam.db.DBConnection;
import com.onlineexam.model.Exam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * All database work of the exams table.
 *
 * Every SELECT here joins the subjects table so that the screens can
 * show the subject name, and counts the rows of the questions table so
 * that the number of questions and the total marks can be displayed
 * without running a second query for every examination.
 */
public class ExamDAO {

    /** The common part of every SELECT of this class. */
    private static final String SELECT_EXAM =
            "SELECT e.*, s.subject_name, "
          + "(SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.exam_id) AS question_count "
          + "FROM exams e JOIN subjects s ON e.subject_id = s.subject_id ";

    /** Every examination, used by the admin on the Manage Exams screen. */
    public List<Exam> getAllExams() throws SQLException {
        List<Exam> list = new ArrayList<>();
        String sql = SELECT_EXAM + "ORDER BY e.exam_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(buildExam(rs));
            }
        }
        return list;
    }

    /**
     * The examinations a student is allowed to write. An examination is
     * shown only when the admin has marked it Active and it actually has
     * at least one question in it.
     */
    public List<Exam> getAvailableExams() throws SQLException {
        List<Exam> list = new ArrayList<>();
        String sql = SELECT_EXAM
                   + "WHERE e.status = 'Active' "
                   + "AND (SELECT COUNT(*) FROM questions q WHERE q.exam_id = e.exam_id) > 0 "
                   + "ORDER BY e.exam_id";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(buildExam(rs));
            }
        }
        return list;
    }

    public Exam getExamById(int examId) throws SQLException {
        String sql = SELECT_EXAM + "WHERE e.exam_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return buildExam(rs);
                }
            }
        }
        return null;
    }

    public boolean addExam(Exam exam) throws SQLException {
        String sql = "INSERT INTO exams (exam_title, subject_id, duration_minutes, "
                   + "marks_per_question, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, exam.getExamTitle());
            ps.setInt(2, exam.getSubjectId());
            ps.setInt(3, exam.getDurationMinutes());
            ps.setInt(4, exam.getMarksPerQuestion());
            ps.setString(5, exam.getStatus());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateExam(Exam exam) throws SQLException {
        String sql = "UPDATE exams SET exam_title = ?, subject_id = ?, duration_minutes = ?, "
                   + "marks_per_question = ?, status = ? WHERE exam_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, exam.getExamTitle());
            ps.setInt(2, exam.getSubjectId());
            ps.setInt(3, exam.getDurationMinutes());
            ps.setInt(4, exam.getMarksPerQuestion());
            ps.setString(5, exam.getStatus());
            ps.setInt(6, exam.getExamId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an examination together with its questions in one transaction.
     * The questions must go first because their foreign key points at the
     * examination. If the second delete fails the first one is rolled back,
     * so the examination never loses its questions and survives.
     */
    public boolean deleteExam(int examId) throws SQLException {
        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM questions WHERE exam_id = ?")) {
                ps.setInt(1, examId);
                ps.executeUpdate();
            }

            int deleted;
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM exams WHERE exam_id = ?")) {
                ps.setInt(1, examId);
                deleted = ps.executeUpdate();
            }

            con.commit();
            return deleted > 0;

        } catch (SQLException failure) {
            if (con != null) {
                con.rollback();
            }
            throw failure;

        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * An examination that students have already written must not be deleted,
     * because the results table has a foreign key pointing at it. The screen
     * calls this first and shows a message instead.
     */
    public int countResults(int examId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM results WHERE exam_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /** Copies one row of the result set into an Exam object. */
    private Exam buildExam(ResultSet rs) throws SQLException {
        Exam exam = new Exam();
        exam.setExamId(rs.getInt("exam_id"));
        exam.setExamTitle(rs.getString("exam_title"));
        exam.setSubjectId(rs.getInt("subject_id"));
        exam.setSubjectName(rs.getString("subject_name"));
        exam.setDurationMinutes(rs.getInt("duration_minutes"));
        exam.setMarksPerQuestion(rs.getInt("marks_per_question"));
        exam.setStatus(rs.getString("status"));
        exam.setQuestionCount(rs.getInt("question_count"));
        return exam;
    }
}
