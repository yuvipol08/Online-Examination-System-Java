package com.onlineexam.ui;

import com.onlineexam.dao.SubjectDAO;
import com.onlineexam.model.Subject;
import com.onlineexam.util.UITheme;
import com.onlineexam.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen for the subjects.
 * The table on the left shows every subject and the form on the right
 * is used to add a new one or to change the selected one.
 */
public class SubjectPanel extends JPanel {

    private final SubjectDAO subjectDAO = new SubjectDAO();

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new String[]{"ID", "Subject Name", "Description"}, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;   // the table is only for reading
                }
            };

    private final JTable table = new JTable(tableModel);
    private final JTextField nameField = new JTextField(16);
    private final JTextField descriptionField = new JTextField(16);

    private int selectedId = 0;

    public SubjectPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(UITheme.createTitleLabel("MANAGE SUBJECTS"), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.EAST);

        loadSubjects();
    }

    private JScrollPane buildTable() {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        int[] widths = {45, 175, 300};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return scroll;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);
        panel.setPreferredSize(new Dimension(330, 0));

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("Subject Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.anchor = GridBagConstraints.WEST;

        nameField.setFont(UITheme.FIELD_FONT);
        descriptionField.setFont(UITheme.FIELD_FONT);

        gbc.gridx = 0; gbc.gridy = 0;
        box.add(UITheme.createFormLabel("Subject Name :"), gbc);
        gbc.gridx = 1;
        box.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        box.add(UITheme.createFormLabel("Description :"), gbc);
        gbc.gridx = 1;
        box.add(descriptionField, gbc);

        JButton addButton = UITheme.createButton("Add", UITheme.SUCCESS);
        JButton updateButton = UITheme.createButton("Update", UITheme.BUTTON);
        JButton deleteButton = UITheme.createButton("Delete", UITheme.DANGER);
        JButton clearButton = UITheme.createButton("Clear", UITheme.SIDEBAR);

        addButton.addActionListener(e -> doAdd());
        updateButton.addActionListener(e -> doUpdate());
        deleteButton.addActionListener(e -> doDelete());
        clearButton.addActionListener(e -> clearForm());

        JPanel buttons = new JPanel(new GridLayout(2, 2, 8, 8));
        buttons.setBackground(Color.WHITE);
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        box.add(buttons, gbc);

        panel.add(box);
        return panel;
    }

    /** Reads every subject from the database into the table. */
    public void loadSubjects() {
        try {
            tableModel.setRowCount(0);
            List<Subject> subjects = subjectDAO.getAllSubjects();

            for (Subject subject : subjects) {
                tableModel.addRow(new Object[]{subject.getSubjectId(),
                                               subject.getSubjectName(),
                                               subject.getDescription()});
            }
            clearForm();

        } catch (SQLException e) {
            showError("Could not read the subjects :\n" + e.getMessage());
        }
    }

    /** Copies the selected row into the form so it can be changed. */
    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = (int) tableModel.getValueAt(row, 0);
        nameField.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        Object description = tableModel.getValueAt(row, 2);
        descriptionField.setText(description == null ? "" : String.valueOf(description));
    }

    private void doAdd() {
        if (!isFormValid()) {
            return;
        }
        try {
            Subject subject = new Subject(0, nameField.getText().trim(),
                                          descriptionField.getText().trim());
            if (subjectDAO.addSubject(subject)) {
                showInfo("The subject was added.");
                loadSubjects();
            }
        } catch (SQLException e) {
            showError(duplicateOr(e, "A subject with this name already exists."));
        }
    }

    private void doUpdate() {
        if (selectedId == 0) {
            showWarning("Please select the subject you want to update.");
            return;
        }
        if (!isFormValid()) {
            return;
        }
        try {
            Subject subject = new Subject(selectedId, nameField.getText().trim(),
                                          descriptionField.getText().trim());
            if (subjectDAO.updateSubject(subject)) {
                showInfo("The subject was updated.");
                loadSubjects();
            }
        } catch (SQLException e) {
            showError(duplicateOr(e, "Another subject already has this name."));
        }
    }

    private void doDelete() {
        if (selectedId == 0) {
            showWarning("Please select the subject you want to delete.");
            return;
        }
        try {
            int examCount = subjectDAO.countExams(selectedId);
            if (examCount > 0) {
                showWarning("This subject cannot be deleted because " + examCount
                        + " examination(s) belong to it.\n"
                        + "Please delete those examinations first.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete the subject \"" + nameField.getText().trim() + "\" ?",
                    "Delete Subject", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION && subjectDAO.deleteSubject(selectedId)) {
                showInfo("The subject was deleted.");
                loadSubjects();
            }

        } catch (SQLException e) {
            showError("Could not delete the subject :\n" + e.getMessage());
        }
    }

    private boolean isFormValid() {
        if (Validator.isEmpty(nameField.getText())) {
            showWarning("Please enter the subject name.");
            return false;
        }
        return true;
    }

    private void clearForm() {
        selectedId = 0;
        nameField.setText("");
        descriptionField.setText("");
        table.clearSelection();
    }

    /** Turns the MySQL duplicate key error into a message the user understands. */
    private String duplicateOr(SQLException e, String friendly) {
        return e.getErrorCode() == 1062 ? friendly : "Database error :\n" + e.getMessage();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Subjects", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Subjects", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Subjects", JOptionPane.ERROR_MESSAGE);
    }
}
