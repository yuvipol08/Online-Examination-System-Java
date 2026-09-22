package com.onlineexam.ui;

import com.onlineexam.dao.ResultDAO;
import com.onlineexam.model.Result;
import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Student screen that lists the examinations this student has written,
 * newest first. Selecting a row and clicking View Answer Sheet opens
 * the full sheet of that attempt.
 */
public class MyResultsPanel extends JPanel {

    private final ResultDAO resultDAO = new ResultDAO();
    private final User student;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Result ID", "Exam", "Date", "Attempted",
                         "Correct", "Marks", "Percentage", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JLabel summaryLabel = new JLabel(" ");

    public MyResultsPanel(User student) {
        this.student = student;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(UITheme.createTitleLabel("MY RESULTS"), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadResults();
    }

    private JScrollPane buildTable() {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {70, 190, 160, 90, 70, 80, 100, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        return scroll;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        summaryLabel.setFont(UITheme.LABEL_FONT);

        JButton refreshButton = UITheme.createButton("Refresh", UITheme.BUTTON);
        JButton viewButton = UITheme.createButton("View Answer Sheet", UITheme.SUCCESS);

        refreshButton.addActionListener(e -> loadResults());
        viewButton.addActionListener(e -> doViewAnswerSheet());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UITheme.BACKGROUND);
        right.add(refreshButton);
        right.add(viewButton);

        panel.add(summaryLabel, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    /** Reads the results of this student into the table. */
    public void loadResults() {
        try {
            tableModel.setRowCount(0);
            List<Result> results = resultDAO.getResultsByStudent(student.getUserId());

            int passed = 0;
            for (Result result : results) {
                if ("Pass".equalsIgnoreCase(result.getStatus())) {
                    passed++;
                }
                tableModel.addRow(new Object[]{
                        result.getResultId(),
                        result.getExamTitle(),
                        result.getExamDate(),
                        result.getAttempted() + " / " + result.getTotalQuestions(),
                        result.getCorrectAnswers(),
                        result.getMarksObtained() + " / " + result.getTotalMarks(),
                        UITheme.percent(result.getPercentage()) + " %",
                        result.getStatus()});
            }

            if (results.isEmpty()) {
                summaryLabel.setText("You have not written any examination yet.");
            } else {
                summaryLabel.setText("Exams written : " + results.size()
                        + "        Passed : " + passed
                        + "        Failed : " + (results.size() - passed));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read your results :\n" + e.getMessage(),
                    "My Results", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doViewAnswerSheet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select the result whose answer sheet you want to see.",
                    "My Results", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resultId = (int) tableModel.getValueAt(row, 0);

        try {
            Result result = resultDAO.getResultById(resultId);
            if (result == null) {
                JOptionPane.showMessageDialog(this, "This result no longer exists.",
                        "My Results", JOptionPane.WARNING_MESSAGE);
                return;
            }
            result.setAnswers(resultDAO.getAnswerSheet(resultId));

            Window parent = SwingUtilities.getWindowAncestor(this);
            new ResultDialog(parent, result, false).setVisible(true);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read the answer sheet :\n" + e.getMessage(),
                    "My Results", JOptionPane.ERROR_MESSAGE);
        }
    }
}
