package com.onlineexam.dao;

import com.onlineexam.db.DBConnection;
import com.onlineexam.model.Question;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** All database work of the questions table. */
public class QuestionDAO {

    /**
     * The question paper of one examination, in the order the questions
     * were added. The exam screen and the admin screen both use this.
     */
    public List<Question> getQuestionsByExam(int examId) throws SQLException {
        List<Question> list = new ArrayList<>();
        String sql = "SELECT * FROM questions WHERE exam_id = ? ORDER BY question_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, examId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(buildQuestion(rs));
                }
            }
        }
        return list;
    }

    public boolean addQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, "
                   + "option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            fillQuestion(ps, question);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateQuestion(Question question) throws SQLException {
        String sql = "UPDATE questions SET exam_id = ?, question_text = ?, option_a = ?, "
                   + "option_b = ?, option_c = ?, option_d = ?, correct_option = ? "
                   + "WHERE question_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            fillQuestion(ps, question);
            ps.setInt(8, question.getQuestionId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteQuestion(int questionId) throws SQLException {
        String sql = "DELETE FROM questions WHERE question_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * A question that already appears on an answer sheet must not be
     * deleted, because the answers table has a foreign key pointing at it.
     */
    public int countAnswers(int questionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM answers WHERE question_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countQuestions(int examId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM questions WHERE exam_id = ?";

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

    /** The seven values that the insert and the update have in common. */
    private void fillQuestion(PreparedStatement ps, Question question) throws SQLException {
        ps.setInt(1, question.getExamId());
        ps.setString(2, question.getQuestionText());
        ps.setString(3, question.getOptionA());
        ps.setString(4, question.getOptionB());
        ps.setString(5, question.getOptionC());
        ps.setString(6, question.getOptionD());
        ps.setString(7, question.getCorrectOption().toUpperCase());
    }

    /** Copies one row of the result set into a Question object. */
    private Question buildQuestion(ResultSet rs) throws SQLException {
        Question question = new Question();
        question.setQuestionId(rs.getInt("question_id"));
        question.setExamId(rs.getInt("exam_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setOptionA(rs.getString("option_a"));
        question.setOptionB(rs.getString("option_b"));
        question.setOptionC(rs.getString("option_c"));
        question.setOptionD(rs.getString("option_d"));
        question.setCorrectOption(rs.getString("correct_option"));
        return question;
    }
}
