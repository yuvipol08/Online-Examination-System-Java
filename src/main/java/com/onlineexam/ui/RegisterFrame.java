package com.onlineexam.ui;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;
import com.onlineexam.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Registration screen for a new student.
 * The account is always created with the role STUDENT, so a student
 * can never register as an administrator.
 */
public class RegisterFrame extends JDialog {

    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);
    private final JTextField rollField = new JTextField(18);
    private final JTextField courseField = new JTextField(18);

    private final UserDAO userDAO = new UserDAO();

    public RegisterFrame(JFrame parent) {
        super(parent, "Student Registration", true);
        UITheme.setSizeWithinScreen(this, 560, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(UITheme.createTitleLabel("NEW STUDENT REGISTRATION"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("Student Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;

        String[] labels = {"Full Name :", "Email :", "Password :",
                           "Confirm Password :", "Roll Number :", "Course :"};
        JComponent[] fields = {nameField, emailField, passwordField,
                               confirmField, rollField, courseField};

        for (int row = 0; row < labels.length; row++) {
            fields[row].setFont(UITheme.FIELD_FONT);
            gbc.gridx = 0; gbc.gridy = row;
            box.add(UITheme.createFormLabel(labels[row]), gbc);
            gbc.gridx = 1;
            box.add(fields[row], gbc);
        }

        JButton registerButton = UITheme.createButton("Register", UITheme.SUCCESS);
        JButton cancelButton = UITheme.createButton("Cancel", UITheme.DANGER);

        registerButton.addActionListener(e -> doRegister());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(buttonPanel, gbc);

        getRootPane().setDefaultButton(registerButton);

        panel.add(box);
        return panel;
    }

    /** Checks every box and then inserts the new student. */
    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        String roll = rollField.getText().trim();
        String course = courseField.getText().trim();

        if (Validator.isEmpty(name) || Validator.isEmpty(email)
                || Validator.isEmpty(password) || Validator.isEmpty(roll)
                || Validator.isEmpty(course)) {
            showMessage("All the fields are required.", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Validator.isValidEmail(email)) {
            showMessage("Please enter a valid email address, for example name@example.com",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            showMessage("The password must be at least 4 characters long.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            showMessage("The two passwords do not match.", JOptionPane.WARNING_MESSAGE);
            confirmField.setText("");
            return;
        }

        try {
            if (userDAO.emailExists(email)) {
                showMessage("This email is already registered. Please use another one.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            User student = new User(name, email, password, roll, course, "STUDENT");

            if (userDAO.register(student)) {
                showMessage("Registration successful. You can now log in with your email.",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                showMessage("The account could not be created. Please try again.",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            showMessage("Database error while registering :\n" + e.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "Registration", type);
    }
}
