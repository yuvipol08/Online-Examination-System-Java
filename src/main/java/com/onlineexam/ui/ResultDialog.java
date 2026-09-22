package com.onlineexam.ui;

import com.onlineexam.model.AnswerRecord;
import com.onlineexam.model.Result;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Shows the result of one attempt : the marks at the top and the full
 * answer sheet below it, with every question, the option the student
 * chose, the correct option and whether the answer was right.
 *
 * The same dialog is used by the student after submitting a paper and
 * by the admin from the View Results screen.
 */
public class ResultDialog extends JDialog {

    private final Result result;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"No", "Question", "Your Answer", "Correct Answer", "Result"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    /**
     * @param forAdmin true when the admin opens the sheet, which adds the
     *                 name of the student to the heading.
     */
    public ResultDialog(Window parent, Result result, boolean forAdmin) {
        super(parent, "Result", ModalityType.APPLICATION_MODAL);
        this.result = result;

        UITheme.setSizeWithinScreen(this, 920, 660);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(UITheme.createTitleLabel(forAdmin ? "STUDENT ANSWER SHEET" : "YOUR RESULT"),
                BorderLayout.NORTH);
        add(buildBody(forAdmin), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        fillAnswerSheet();
    }

    private JPanel buildBody(boolean forAdmin) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));

        panel.add(buildSummary(forAdmin), BorderLayout.NORTH);
        panel.add(buildTable(), BorderLayout.CENTER);
        return panel;
    }

    /** The box at the top with the marks and the pass or fail status. */
    private JPanel buildSummary(boolean forAdmin) {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("Summary"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 12, 4, 12);
        gbc.anchor = GridBagConstraints.WEST;

        String[][] rows = {
                {"Exam :", result.getExamTitle()},
                {"Date :", result.getExamDate()},
                {"Total Questions :", String.valueOf(result.getTotalQuestions())},
                {"Attempted :", String.valueOf(result.getAttempted())},
                {"Not Attempted :", String.valueOf(result.getNotAttempted())},
                {"Correct :", String.valueOf(result.getCorrectAnswers())},
                {"Wrong :", String.valueOf(result.getWrongAnswers())},
                {"Marks Obtained :", result.getMarksObtained() + " out of " + result.getTotalMarks()},
                {"Percentage :", UITheme.percent(result.getPercentage()) + " %"}
        };

        int row = 0;

        if (forAdmin) {
            addSummaryRow(box, gbc, row++, "Student :",
                    result.getStudentName() + "  (" + result.getRollNumber() + ")");
        }

        for (String[] pair : rows) {
            addSummaryRow(box, gbc, row++, pair[0], pair[1]);
        }

        // the pass or fail line, in green or red
        JLabel statusTitle = UITheme.createFormLabel("Status :");
        JLabel statusValue = new JLabel(result.getStatus().toUpperCase());
        statusValue.setFont(new Font("SansSerif", Font.BOLD, 18));
        statusValue.setForeground("Pass".equalsIgnoreCase(result.getStatus())
                ? UITheme.SUCCESS : UITheme.DANGER);

        gbc.gridx = 0; gbc.gridy = row;
        box.add(statusTitle, gbc);
        gbc.gridx = 1;
        box.add(statusValue, gbc);

        // An empty third column that takes the spare width, so the two
        // columns above stay on the left instead of floating in the middle.
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        box.add(Box.createHorizontalGlue(), gbc);

        return box;
    }

    private void addSummaryRow(JPanel box, GridBagConstraints gbc,
                               int row, String title, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        box.add(UITheme.createFormLabel(title), gbc);

        JLabel label = new JLabel(value);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridx = 1;
        box.add(label, gbc);
    }

    private JScrollPane buildTable() {
        JTable table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(26);

        int[] widths = {40, 400, 110, 120, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // The last column is coloured so a wrong answer is easy to spot.
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable jTable, Object value,
                    boolean selected, boolean focused, int row, int column) {

                Component cell = super.getTableCellRendererComponent(
                        jTable, value, selected, focused, row, column);

                if (!selected) {
                    cell.setForeground("Correct".equals(value) ? UITheme.SUCCESS : UITheme.DANGER);
                }
                return cell;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(UITheme.createGroupBorder("Answer Sheet"));
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBackground(UITheme.BACKGROUND);

        JButton closeButton = UITheme.createButton("Close", UITheme.SIDEBAR);
        closeButton.addActionListener(e -> dispose());

        panel.add(closeButton);
        return panel;
    }

    private void fillAnswerSheet() {
        int number = 1;

        for (AnswerRecord answer : result.getAnswers()) {
            String outcome;
            if (answer.getSelectedOption() == null || answer.getSelectedOption().isBlank()) {
                outcome = "Not Attempted";
            } else {
                outcome = "Yes".equalsIgnoreCase(answer.getIsCorrect()) ? "Correct" : "Wrong";
            }

            tableModel.addRow(new Object[]{number++,
                                           answer.getQuestionText(),
                                           answer.getSelectedOptionForDisplay(),
                                           answer.getCorrectOption(),
                                           outcome});
        }
    }
}
