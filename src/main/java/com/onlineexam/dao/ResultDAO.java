package com.onlineexam.dao;

import com.onlineexam.db.DBConnection;
import com.onlineexam.model.AnswerRecord;
import com.onlineexam.model.Exam;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.Question;
import com.onlineexam.model.Result;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * All database work of the results and answers tables.
 *
 * The important method of the whole project is submitExam. One attempt
 * produces one row in results and one row per question in answers, and
 * the two must be written together. They are therefore written inside a
 * single transaction : if anything fails half way, the whole attempt is
 * rolled back and the database is left exactly as it was before the
 * student pressed Submit.
 */
public class ResultDAO {

    /** A student needs at least this percentage to be marked Pass. */
    public static final double PASSING_PERCENTAGE = 40.0;

    private static final String INSERT_RESULT =
            "INSERT INTO results (exam_id, user_id, exam_date, total_questions, attempted, "
          + "correct_answers, marks_obtained, total_marks, percentage, status) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_ANSWER =
            "INSERT INTO answers (result_id, question_id, selected_option, is_correct) "
          + "VALUES (?, ?, ?, ?)";

    private static final String SELECT_RESULT =
            "SELECT r.*, e.exam_title, u.full_name, u.roll_number "
          + "FROM results r "
          + "JOIN exams e ON r.exam_id = e.exam_id "
          + "JOIN users u ON r.user_id = u.user_id ";

    /**
     * Writes one finished attempt to the database and returns the Result
     * object that the result screen shows.
     *
     * Steps :
     *   1. count the answers and work out the marks
     *   2. switch off auto commit, so nothing is saved on its own
     *   3. insert the results row and read back the generated result_id
     *   4. insert one answers row per question as a batch
     *   5. commit, or roll everything back when a step fails
     */
    public Result submitExam(ExamSession session, int userId) throws SQLException {

        Exam exam = session.getExam();
        List<Question> questions = session.getQuestions();

        if (questions.isEmpty()) {
            throw new SQLException("This examination has no questions, so it cannot be submitted.");
        }

        int totalQuestions = questions.size();
        int attempted = session.getAnsweredCount();
        int correctAnswers = session.getCorrectCount();
        int marksObtained = correctAnswers * exam.getMarksPerQuestion();
        int totalMarks = totalQuestions * exam.getMarksPerQuestion();
        double percentage = totalMarks == 0 ? 0.0 : (marksObtained * 100.0) / totalMarks;
        String status = percentage >= PASSING_PERCENTAGE ? "Pass" : "Fail";
        Timestamp examDate = new Timestamp(System.currentTimeMillis());

        Connection con = null;

        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            int resultId;

            // 1. the results row, with the generated key read back
            try (PreparedStatement ps = con.prepareStatement(INSERT_RESULT,
                    Statement.RETURN_GENERATED_KEYS)) {

                ps.setInt(1, exam.getExamId());
                ps.setInt(2, userId);
                ps.setTimestamp(3, examDate);
                ps.setInt(4, totalQuestions);
                ps.setInt(5, attempted);
                ps.setInt(6, correctAnswers);
                ps.setInt(7, marksObtained);
                ps.setInt(8, totalMarks);
                ps.setDouble(9, percentage);
                ps.setString(10, status);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("The result could not be saved.");
                    }
                    resultId = keys.getInt(1);
                }
            }

            // 2. one answers row per question, sent to MySQL as one batch
            try (PreparedStatement ps = con.prepareStatement(INSERT_ANSWER)) {

                for (Question question : questions) {
                    String selected = session.getSelectedOption(question.getQuestionId());

                    ps.setInt(1, resultId);
                    ps.setInt(2, question.getQuestionId());
                    if (selected == null) {
                        ps.setNull(3, java.sql.Types.CHAR);   // question left unanswered
                    } else {
                        ps.setString(3, selected);
                    }
                    ps.setString(4, question.isCorrect(selected) ? "Yes" : "No");
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();

            Result result = new Result();
            result.setResultId(resultId);
            result.setExamId(exam.getExamId());
            result.setExamTitle(exam.getExamTitle());
            result.setUserId(userId);
            result.setExamDate(examDate.toString().substring(0, 19));
            result.setTotalQuestions(totalQuestions);
            result.setAttempted(attempted);
            result.setCorrectAnswers(correctAnswers);
            result.setMarksObtained(marksObtained);
            result.setTotalMarks(totalMarks);
            result.setPercentage(percentage);
            result.setStatus(status);
            return result;

        } catch (SQLException failure) {
            if (con != null) {
                con.rollback();   // nothing of this attempt stays behind
            }
            throw failure;

        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /** The results of one student, newest first. Used by My Results. */
    public List<Result> getResultsByStudent(int userId) throws SQLException {
        String sql = SELECT_RESULT + "WHERE r.user_id = ? ORDER BY r.result_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            return readResults(ps);
        }
    }

    /** Every result of every student. Used by the admin on View Results. */
    public List<Result> getAllResults() throws SQLException {
        String sql = SELECT_RESULT + "ORDER BY r.result_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            return readResults(ps);
        }
    }

    public Result getResultById(int resultId) throws SQLException {
        String sql = SELECT_RESULT + "WHERE r.result_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, resultId);
            List<Result> list = readResults(ps);
            return list.isEmpty() ? null : list.get(0);
        }
    }

    /**
     * The answer sheet of one attempt : every question with the option the
     * student selected, the correct option and whether it was right.
     */
    public List<AnswerRecord> getAnswerSheet(int resultId) throws SQLException {
        List<AnswerRecord> list = new ArrayList<>();
        String sql = "SELECT a.*, q.question_text, q.correct_option AS right_option "
                   + "FROM answers a JOIN questions q ON a.question_id = q.question_id "
                   + "WHERE a.result_id = ? ORDER BY a.answer_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, resultId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AnswerRecord answer = new AnswerRecord();
                    answer.setAnswerId(rs.getInt("answer_id"));
                    answer.setResultId(rs.getInt("result_id"));
                    answer.setQuestionId(rs.getInt("question_id"));
                    answer.setQuestionText(rs.getString("question_text"));
                    answer.setSelectedOption(rs.getString("selected_option"));
                    answer.setCorrectOption(rs.getString("right_option"));
                    answer.setIsCorrect(rs.getString("is_correct"));
                    list.add(answer);
                }
            }
        }
        return list;
    }

    /** How many times this student has already written this examination. */
    public int countAttempts(int examId, int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM results WHERE exam_id = ? AND user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, examId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /** Runs the prepared statement and turns every row into a Result object. */
    private List<Result> readResults(PreparedStatement ps) throws SQLException {
        List<Result> list = new ArrayList<>();

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Result result = new Result();
                result.setResultId(rs.getInt("result_id"));
                result.setExamId(rs.getInt("exam_id"));
                result.setExamTitle(rs.getString("exam_title"));
                result.setUserId(rs.getInt("user_id"));
                result.setStudentName(rs.getString("full_name"));
                result.setRollNumber(rs.getString("roll_number"));

                Timestamp date = rs.getTimestamp("exam_date");
                result.setExamDate(date == null ? "" : date.toString().substring(0, 19));

                result.setTotalQuestions(rs.getInt("total_questions"));
                result.setAttempted(rs.getInt("attempted"));
                result.setCorrectAnswers(rs.getInt("correct_answers"));
                result.setMarksObtained(rs.getInt("marks_obtained"));
                result.setTotalMarks(rs.getInt("total_marks"));
                result.setPercentage(rs.getDouble("percentage"));
                result.setStatus(rs.getString("status"));
                list.add(result);
            }
        }
        return list;
    }
}
