package com.onlineexam.ui;

import com.onlineexam.dao.UserDAO;
import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;
import com.onlineexam.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * First screen of the application.
 * The email and password are checked against the users table and the
 * role of the user decides whether the admin dashboard or the student
 * dashboard is opened.
 */
public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final UserDAO userDAO = new UserDAO();

    public LoginFrame() {
        setTitle("Online Examination System - Login");
        UITheme.setSizeWithinScreen(this, 640, 430);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(UITheme.createTitleLabel("ONLINE EXAMINATION SYSTEM"), BorderLayout.NORTH);
        add(buildLoginPanel(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("User Login"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        emailField.setFont(UITheme.FIELD_FONT);
        passwordField.setFont(UITheme.FIELD_FONT);

        gbc.gridx = 0; gbc.gridy = 0;
        box.add(UITheme.createFormLabel("Email :"), gbc);
        gbc.gridx = 1;
        box.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        box.add(UITheme.createFormLabel("Password :"), gbc);
        gbc.gridx = 1;
        box.add(passwordField, gbc);

        JButton loginButton = UITheme.createButton("Login", UITheme.BUTTON);
        JButton registerButton = UITheme.createButton("New Student? Register", UITheme.SUCCESS);
        JButton exitButton = UITheme.createButton("Exit", UITheme.DANGER);

        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> new RegisterFrame(this).setVisible(true));
        exitButton.addActionListener(e -> System.exit(0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        box.add(buttonPanel, gbc);

        // Pressing Enter in the password box works the same as clicking Login
        getRootPane().setDefaultButton(loginButton);

        panel.add(box);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(UITheme.SIDEBAR);
        JLabel label = new JLabel("Administrator and students use the same login screen");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footer.add(label);
        return footer;
    }

    /** Reads the two boxes, checks them and opens the correct dashboard. */
    private void doLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (Validator.isEmpty(email) || Validator.isEmpty(password)) {
            showError("Please enter both the email and the password.");
            return;
        }

        try {
            User user = userDAO.login(email, password);

            if (user == null) {
                showError("Invalid email or password. Please try again.");
                passwordField.setText("");
                return;
            }

            dispose();
            if (user.isAdmin()) {
                new AdminDashboard(user).setVisible(true);
            } else {
                new StudentDashboard(user).setVisible(true);
            }

        } catch (SQLException e) {
            showError("Database error while logging in :\n" + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Login", JOptionPane.WARNING_MESSAGE);
    }
}
