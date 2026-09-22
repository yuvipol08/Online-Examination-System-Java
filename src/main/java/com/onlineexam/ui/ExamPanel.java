package com.onlineexam.ui;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.SubjectDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Subject;
import com.onlineexam.util.UITheme;
import com.onlineexam.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen for the examinations.
 * Every examination belongs to a subject, which is picked from a combo
 * box filled from the subjects table, and carries a duration in minutes,
 * the marks of one question and the status Active or Inactive.
 */
public class ExamPanel extends JPanel {

    private final ExamDAO examDAO = new ExamDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Exam Title", "Subject", "Duration (min)",
                         "Marks / Question", "Questions", "Total Marks", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);

    private final JTextField titleField = new JTextField(18);
    private final JComboBox<Subject> subjectBox = new JComboBox<>();
    private final JTextField durationField = new JTextField(10);
    private final JTextField marksField = new JTextField(10);
    private final JComboBox<String> statusBox =
            new JComboBox<>(new String[]{"Active", "Inactive"});

    private int selectedId = 0;

    public ExamPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(UITheme.createTitleLabel("MANAGE EXAMS"), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        refresh();
    }

    private JScrollPane buildTable() {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        int[] widths = {45, 200, 180, 110, 130, 95, 105, 80};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        return scroll;
    }

    private JPanel buildForm() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("Exam Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        titleField.setFont(UITheme.FIELD_FONT);
        durationField.setFont(UITheme.FIELD_FONT);
        marksField.setFont(UITheme.FIELD_FONT);

        gbc.gridx = 0; gbc.gridy = 0;
        box.add(UITheme.createFormLabel("Exam Title :"), gbc);
        gbc.gridx = 1;
        box.add(titleField, gbc);
        gbc.gridx = 2;
        box.add(UITheme.createFormLabel("Subject :"), gbc);
        gbc.gridx = 3;
        box.add(subjectBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        box.add(UITheme.createFormLabel("Duration (min) :"), gbc);
        gbc.gridx = 1;
        box.add(durationField, gbc);
        gbc.gridx = 2;
        box.add(UITheme.createFormLabel("Marks / Question :"), gbc);
        gbc.gridx = 3;
        box.add(marksField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        box.add(UITheme.createFormLabel("Status :"), gbc);
        gbc.gridx = 1;
        box.add(statusBox, gbc);

        JButton addButton = UITheme.createButton("Add", UITheme.SUCCESS);
        JButton updateButton = UITheme.createButton("Update", UITheme.BUTTON);
        JButton deleteButton = UITheme.createButton("Delete", UITheme.DANGER);
        JButton clearButton = UITheme.createButton("Clear", UITheme.SIDEBAR);

        addButton.addActionListener(e -> doAdd());
        updateButton.addActionListener(e -> doUpdate());
        deleteButton.addActionListener(e -> doDelete());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        buttons.setBackground(Color.WHITE);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridx = 2; gbc.gridy = 2; gbc.gridwidth = 2;
        box.add(buttons, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        wrapper.add(box, BorderLayout.CENTER);
        return wrapper;
    }

    /** Reloads the subject combo box and the table of examinations. */
    public void refresh() {
        loadSubjects();
        loadExams();
    }

    private void loadSubjects() {
        try {
            subjectBox.removeAllItems();
            for (Subject subject : subjectDAO.getAllSubjects()) {
                subjectBox.addItem(subject);
            }
        } catch (SQLException e) {
            showError("Could not read the subjects :\n" + e.getMessage());
        }
    }

    private void loadExams() {
        try {
            tableModel.setRowCount(0);
            List<Exam> exams = examDAO.getAllExams();

            for (Exam exam : exams) {
                tableModel.addRow(new Object[]{exam.getExamId(),
                                               exam.getExamTitle(),
                                               exam.getSubjectName(),
                                               exam.getDurationMinutes(),
                                               exam.getMarksPerQuestion(),
                                               exam.getQuestionCount(),
                                               exam.getTotalMarks(),
                                               exam.getStatus()});
            }
            clearForm();

        } catch (SQLException e) {
            showError("Could not read the examinations :\n" + e.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = (int) tableModel.getValueAt(row, 0);
        titleField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        durationField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        marksField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        statusBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 7)));

        String subjectName = String.valueOf(tableModel.getValueAt(row, 2));
        for (int i = 0; i < subjectBox.getItemCount(); i++) {
            if (subjectBox.getItemAt(i).getSubjectName().equals(subjectName)) {
                subjectBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void doAdd() {
        Exam exam = readForm();
        if (exam == null) {
            return;
        }
        try {
            if (examDAO.addExam(exam)) {
                showInfo("The examination was created.\n"
                        + "Use Manage Questions to add its questions.");
                loadExams();
            }
        } catch (SQLException e) {
            showError("Could not create the examination :\n" + e.getMessage());
        }
    }

    private void doUpdate() {
        if (selectedId == 0) {
            showWarning("Please select the examination you want to update.");
            return;
        }
        Exam exam = readForm();
        if (exam == null) {
            return;
        }
        exam.setExamId(selectedId);
        try {
            if (examDAO.updateExam(exam)) {
                showInfo("The examination was updated.");
                loadExams();
            }
        } catch (SQLException e) {
            showError("Could not update the examination :\n" + e.getMessage());
        }
    }

    private void doDelete() {
        if (selectedId == 0) {
            showWarning("Please select the examination you want to delete.");
            return;
        }
        try {
            int resultCount = examDAO.countResults(selectedId);
            if (resultCount > 0) {
                showWarning("This examination cannot be deleted because "
                        + resultCount + " student result(s) belong to it.\n"
                        + "Mark it Inactive instead, so that students no longer see it.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete the examination \"" + titleField.getText().trim()
                    + "\" together with all its questions?",
                    "Delete Exam", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION && examDAO.deleteExam(selectedId)) {
                showInfo("The examination and its questions were deleted.");
                loadExams();
            }

        } catch (SQLException e) {
            showError("Could not delete the examination :\n" + e.getMessage());
        }
    }

    /** Checks the boxes and builds an Exam object, or returns null. */
    private Exam readForm() {
        String title = titleField.getText().trim();
        Subject subject = (Subject) subjectBox.getSelectedItem();
        String duration = durationField.getText().trim();
        String marks = marksField.getText().trim();

        if (Validator.isEmpty(title)) {
            showWarning("Please enter the examination title.");
            return null;
        }
        if (subject == null) {
            showWarning("Please add a subject first, then create the examination.");
            return null;
        }
        if (!Validator.isValidDuration(duration)) {
            showWarning("The duration must be a whole number of minutes between 1 and "
                    + Validator.MAX_DURATION + ".");
            return null;
        }
        if (!Validator.isValidMarks(marks)) {
            showWarning("The marks of one question must be a whole number between 1 and "
                    + Validator.MAX_MARKS + ".");
            return null;
        }

        Exam exam = new Exam();
        exam.setExamTitle(title);
        exam.setSubjectId(subject.getSubjectId());
        exam.setDurationMinutes(Integer.parseInt(duration));
        exam.setMarksPerQuestion(Integer.parseInt(marks));
        exam.setStatus(String.valueOf(statusBox.getSelectedItem()));
        return exam;
    }

    private void clearForm() {
        selectedId = 0;
        titleField.setText("");
        durationField.setText("");
        marksField.setText("");
        statusBox.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Exams", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Exams", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Exams", JOptionPane.ERROR_MESSAGE);
    }
}
