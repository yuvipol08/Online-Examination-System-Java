package com.onlineexam.ui;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.dao.ResultDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.Question;
import com.onlineexam.model.Result;
import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Student screen that lists the examinations which are open, and starts
 * one of them. Only examinations that the admin marked Active and that
 * actually contain questions are shown here.
 */
public class AvailableExamPanel extends JPanel {

    private final ExamDAO examDAO = new ExamDAO();
    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ResultDAO resultDAO = new ResultDAO();

    private final StudentDashboard dashboard;
    private final User student;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Exam Title", "Subject", "Questions",
                         "Total Marks", "Duration (min)", "Attempts So Far"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    public AvailableExamPanel(StudentDashboard dashboard, User student) {
        this.dashboard = dashboard;
        this.student = student;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(UITheme.createTitleLabel("AVAILABLE EXAMS"), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadExams();
    }

    private JScrollPane buildTable() {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {40, 210, 190, 90, 100, 110, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(UITheme.BACKGROUND);

        JButton refreshButton = UITheme.createButton("Refresh", UITheme.BUTTON);
        JButton startButton = UITheme.createButton("Start Exam", UITheme.SUCCESS);

        refreshButton.addActionListener(e -> loadExams());
        startButton.addActionListener(e -> doStartExam());

        panel.add(refreshButton);
        panel.add(startButton);
        return panel;
    }

    /** Reads the open examinations into the table. */
    public void loadExams() {
        try {
            tableModel.setRowCount(0);
            List<Exam> exams = examDAO.getAvailableExams();

            for (Exam exam : exams) {
                int attempts = resultDAO.countAttempts(exam.getExamId(), student.getUserId());

                tableModel.addRow(new Object[]{exam.getExamId(),
                                               exam.getExamTitle(),
                                               exam.getSubjectName(),
                                               exam.getQuestionCount(),
                                               exam.getTotalMarks(),
                                               exam.getDurationMinutes(),
                                               attempts});
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read the examinations :\n" + e.getMessage(),
                    "Exams", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads the question paper, opens the examination window and, once it
     * closes, shows the result of the attempt.
     */
    private void doStartExam() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select the examination you want to write.",
                    "Exams", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int examId = (int) tableModel.getValueAt(row, 0);

        try {
            Exam exam = examDAO.getExamById(examId);
            if (exam == null || !exam.isActive()) {
                JOptionPane.showMessageDialog(this,
                        "This examination is no longer available.\n"
                        + "Please click Refresh to see the current list.",
                        "Exams", JOptionPane.WARNING_MESSAGE);
                loadExams();
                return;
            }

            List<Question> questions = questionDAO.getQuestionsByExam(examId);
            if (questions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "This examination has no questions yet.",
                        "Exams", JOptionPane.WARNING_MESSAGE);
                loadExams();
                return;
            }

            String message = "Exam : " + exam.getExamTitle()
                    + "\nQuestions : " + questions.size()
                    + "\nTotal Marks : " + exam.getTotalMarks()
                    + "\nDuration : " + exam.getDurationMinutes() + " minutes"
                    + "\n\nThe timer starts as soon as you click Yes.\n"
                    + "Do you want to start the examination?";

            int choice = JOptionPane.showConfirmDialog(this, message,
                    "Start Exam", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            ExamSession session = new ExamSession(exam, questions);
            Window parent = SwingUtilities.getWindowAncestor(this);

            ExamWindow window = new ExamWindow(parent, session, student.getUserId());
            window.setVisible(true);          // waits here until the paper is submitted

            Result result = window.getResult();
            if (result != null) {
                result.setAnswers(resultDAO.getAnswerSheet(result.getResultId()));
                result.setStudentName(student.getFullName());
                result.setRollNumber(student.getRollNumber());

                new ResultDialog(parent, result, false).setVisible(true);
                dashboard.showResultsAfterExam();
            }
            loadExams();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not start the examination :\n" + e.getMessage(),
                    "Exams", JOptionPane.ERROR_MESSAGE);
        }
    }
}
