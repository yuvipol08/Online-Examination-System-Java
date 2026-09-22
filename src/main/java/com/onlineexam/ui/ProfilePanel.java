package com.onlineexam.ui;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;
import com.onlineexam.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Student screen for changing the own details.
 * The email is shown but cannot be changed, because it is the login
 * name of the account.
 */
public class ProfilePanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final User student;

    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField rollField = new JTextField(18);
    private final JTextField courseField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JPasswordField confirmField = new JPasswordField(18);

    public ProfilePanel(User student) {
        this.student = student;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        add(UITheme.createTitleLabel("MY PROFILE"), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);

        loadProfile();
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("Account Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.anchor = GridBagConstraints.WEST;

        emailField.setEditable(false);      // the login name cannot be changed
        emailField.setBackground(new Color(236, 240, 241));

        String[] labels = {"Full Name :", "Email :", "Roll Number :", "Course :",
                           "New Password :", "Confirm Password :"};
        JComponent[] fields = {nameField, emailField, rollField, courseField,
                               passwordField, confirmField};

        for (int row = 0; row < labels.length; row++) {
            fields[row].setFont(UITheme.FIELD_FONT);
            gbc.gridx = 0; gbc.gridy = row;
            box.add(UITheme.createFormLabel(labels[row]), gbc);
            gbc.gridx = 1;
            box.add(fields[row], gbc);
        }

        JLabel hint = new JLabel("Leave both password boxes empty to keep the present password.");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 12));
        gbc.gridx = 0; gbc.gridy = labels.length; gbc.gridwidth = 2;
        box.add(hint, gbc);

        JButton saveButton = UITheme.createButton("Save Changes", UITheme.SUCCESS);
        JButton resetButton = UITheme.createButton("Reset", UITheme.SIDEBAR);

        saveButton.addActionListener(e -> doSave());
        resetButton.addActionListener(e -> loadProfile());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        buttons.setBackground(Color.WHITE);
        buttons.add(saveButton);
        buttons.add(resetButton);

        gbc.gridy = labels.length + 1;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(buttons, gbc);

        panel.add(box);
        return panel;
    }

    /** Puts the details of the logged in student back into the boxes. */
    public void loadProfile() {
        nameField.setText(student.getFullName());
        emailField.setText(student.getEmail());
        rollField.setText(student.getRollNumber());
        courseField.setText(student.getCourse());
        passwordField.setText("");
        confirmField.setText("");
    }

    private void doSave() {
        String name = nameField.getText().trim();
        String roll = rollField.getText().trim();
        String course = courseField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (Validator.isEmpty(name) || Validator.isEmpty(roll) || Validator.isEmpty(course)) {
            showMessage("The name, roll number and course are required.",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // An empty password box means the present password is kept.
        String newPassword = student.getPassword();

        if (!password.isEmpty() || !confirm.isEmpty()) {
            if (password.length() < 4) {
                showMessage("The new password must be at least 4 characters long.",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                showMessage("The two passwords do not match.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            newPassword = password;
        }

        try {
            User updated = new User();
            updated.setUserId(student.getUserId());
            updated.setFullName(name);
            updated.setRollNumber(roll);
            updated.setCourse(course);
            updated.setPassword(newPassword);

            if (userDAO.updateProfile(updated)) {
                // keep the object of the logged in student in step with the database
                student.setFullName(name);
                student.setRollNumber(roll);
                student.setCourse(course);
                student.setPassword(newPassword);

                showMessage("Your profile was updated.", JOptionPane.INFORMATION_MESSAGE);
                loadProfile();
            } else {
                showMessage("The profile could not be updated.", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            showMessage("Database error while saving :\n" + e.getMessage(),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMessage(String message, int type) {
        JOptionPane.showMessageDialog(this, message, "My Profile", type);
    }
}
