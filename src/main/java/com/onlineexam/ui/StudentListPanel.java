package com.onlineexam.ui;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen that lists the registered students.
 * The admin can only view and remove students here, the accounts
 * themselves are created by the students on the registration screen.
 */
public class StudentListPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Full Name", "Email", "Roll Number", "Course"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JLabel countLabel = new JLabel(" ");

    public StudentListPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(UITheme.createTitleLabel("REGISTERED STUDENTS"), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        loadStudents();
    }

    private JScrollPane buildTable() {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {50, 200, 230, 110, 120};
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

        countLabel.setFont(UITheme.LABEL_FONT);

        JButton refreshButton = UITheme.createButton("Refresh", UITheme.BUTTON);
        JButton deleteButton = UITheme.createButton("Delete Student", UITheme.DANGER);

        refreshButton.addActionListener(e -> loadStudents());
        deleteButton.addActionListener(e -> doDelete());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(UITheme.BACKGROUND);
        right.add(refreshButton);
        right.add(deleteButton);

        panel.add(countLabel, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    /** Reads every student from the users table into the table. */
    public void loadStudents() {
        try {
            tableModel.setRowCount(0);
            List<User> students = userDAO.getAllStudents();

            for (User student : students) {
                tableModel.addRow(new Object[]{student.getUserId(),
                                               student.getFullName(),
                                               student.getEmail(),
                                               student.getRollNumber(),
                                               student.getCourse()});
            }
            countLabel.setText("Total registered students : " + students.size());

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not read the students :\n" + e.getMessage(),
                    "Students", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doDelete() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select the student you want to delete.",
                    "Students", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(row, 0);
        String name = String.valueOf(tableModel.getValueAt(row, 1));

        try {
            int resultCount = userDAO.countResults(userId);
            if (resultCount > 0) {
                JOptionPane.showMessageDialog(this,
                        "This student cannot be deleted because " + resultCount
                        + " examination result(s) belong to this account.",
                        "Students", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete the student \"" + name + "\" ?",
                    "Delete Student", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION && userDAO.deleteStudent(userId)) {
                JOptionPane.showMessageDialog(this, "The student was deleted.",
                        "Students", JOptionPane.INFORMATION_MESSAGE);
                loadStudents();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete the student :\n" + e.getMessage(),
                    "Students", JOptionPane.ERROR_MESSAGE);
        }
    }
}
