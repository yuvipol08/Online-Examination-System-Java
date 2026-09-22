package com.onlineexam.ui;

import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * Main window of the administrator.
 * A menu strip is shown on the left and the selected screen is displayed
 * on the right using a CardLayout, so only one panel is visible at a time.
 */
public class AdminDashboard extends JFrame {

    private final User admin;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final SubjectPanel subjectPanel = new SubjectPanel();
    private final ExamPanel examPanel = new ExamPanel();
    private final QuestionPanel questionPanel = new QuestionPanel();
    private final StudentListPanel studentPanel = new StudentListPanel();
    private final AdminResultPanel resultPanel = new AdminResultPanel();

    public AdminDashboard(User admin) {
        this.admin = admin;

        setTitle("Online Examination System - Admin Dashboard");
        UITheme.setSizeWithinScreen(this, 1100, 660);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER);

        JLabel title = new JLabel("  ONLINE EXAMINATION SYSTEM  -  ADMIN PANEL");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel welcome = new JLabel("Logged in as : " + admin.getFullName() + "   ");
        welcome.setFont(UITheme.LABEL_FONT);
        welcome.setForeground(Color.WHITE);

        header.add(title, BorderLayout.WEST);
        header.add(welcome, BorderLayout.EAST);
        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        sidebar.add(createMenuButton("Dashboard Home", "HOME"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Subjects", "SUBJECT"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Exams", "EXAM"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Questions", "QUESTION"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("Manage Students", "STUDENT"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("View Results", "RESULT"));
        sidebar.add(Box.createVerticalGlue());

        JButton logout = UITheme.createButton("Logout", UITheme.DANGER);
        logout.setAlignmentX(Component.CENTER_ALIGNMENT);
        logout.setMaximumSize(new Dimension(196, 38));
        logout.addActionListener(e -> doLogout());
        sidebar.add(logout);

        return sidebar;
    }

    private JButton createMenuButton(String text, String cardName) {
        JButton button = new JButton(text);
        // The plain button painter is used so the menu keeps its colours
        // on every look and feel, including the Windows one.
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(new Color(62, 84, 108));
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(196, 40));
        button.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        button.addActionListener(e -> {
            cardLayout.show(contentPanel, cardName);
            refreshCard(cardName);
        });
        return button;
    }

    /** Reloads the data of a screen every time it is opened. */
    private void refreshCard(String cardName) {
        switch (cardName) {
            case "SUBJECT" -> subjectPanel.loadSubjects();
            case "EXAM" -> examPanel.refresh();
            case "QUESTION" -> questionPanel.refresh();
            case "STUDENT" -> studentPanel.loadStudents();
            case "RESULT" -> resultPanel.loadResults();
            default -> { }
        }
    }

    private JPanel buildContent() {
        contentPanel.add(buildHomePanel(), "HOME");
        contentPanel.add(subjectPanel, "SUBJECT");
        contentPanel.add(examPanel, "EXAM");
        contentPanel.add(questionPanel, "QUESTION");
        contentPanel.add(studentPanel, "STUDENT");
        contentPanel.add(resultPanel, "RESULT");
        cardLayout.show(contentPanel, "HOME");
        return contentPanel;
    }

    /** Simple welcome screen shown when the admin dashboard opens. */
    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(UITheme.createGroupBorder("Welcome"));

        String[] lines = {
                "Welcome to the Admin Panel of the Online Examination System.",
                " ",
                "Use the menu on the left side to :",
                "     -  add, update and delete subjects",
                "     -  create examinations and set their duration and marks",
                "     -  add the questions of an examination with their four options",
                "     -  view the registered students",
                "     -  view the results of every attempt with the full answer sheet",
                " ",
                "Click Logout when the work is finished."
        };

        for (String line : lines) {
            JLabel label = new JLabel(line);
            label.setFont(UITheme.LABEL_FONT);
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }

        panel.add(card);
        return panel;
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Do you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
}
