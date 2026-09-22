package com.onlineexam.ui;

import com.onlineexam.model.User;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;

/**
 * Main window of a student.
 * It works the same way as the admin dashboard : a menu on the left and
 * one panel at a time on the right inside a CardLayout.
 */
public class StudentDashboard extends JFrame {

    private final User student;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final AvailableExamPanel examPanel;
    private final MyResultsPanel resultPanel;
    private final ProfilePanel profilePanel;

    public StudentDashboard(User student) {
        this.student = student;
        this.examPanel = new AvailableExamPanel(this, student);
        this.resultPanel = new MyResultsPanel(student);
        this.profilePanel = new ProfilePanel(student);

        setTitle("Online Examination System - Student Dashboard");
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

        JLabel title = new JLabel("  ONLINE EXAMINATION SYSTEM  -  STUDENT PANEL");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(Color.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));

        JLabel welcome = new JLabel("Logged in as : " + student.getFullName()
                + "  (" + student.getRollNumber() + ")   ");
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
        sidebar.add(createMenuButton("Available Exams", "EXAM"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("My Results", "RESULT"));
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createMenuButton("My Profile", "PROFILE"));
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

    private void refreshCard(String cardName) {
        switch (cardName) {
            case "EXAM" -> examPanel.loadExams();
            case "RESULT" -> resultPanel.loadResults();
            case "PROFILE" -> profilePanel.loadProfile();
            default -> { }
        }
    }

    private JPanel buildContent() {
        contentPanel.add(buildHomePanel(), "HOME");
        contentPanel.add(examPanel, "EXAM");
        contentPanel.add(resultPanel, "RESULT");
        contentPanel.add(profilePanel, "PROFILE");
        cardLayout.show(contentPanel, "HOME");
        return contentPanel;
    }

    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(UITheme.createGroupBorder("Welcome"));

        String[] lines = {
                "Welcome " + student.getFullName() + ".",
                " ",
                "Use the menu on the left side to :",
                "     -  see the examinations that are open and start one",
                "     -  see the result and the answer sheet of every exam you wrote",
                "     -  change your name, roll number, course or password",
                " ",
                "While writing an examination :",
                "     -  a timer at the top shows the time left",
                "     -  the paper is submitted automatically when the time is over",
                "     -  you may move between questions with Previous and Next",
                " ",
                "Click Logout when you are finished."
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

    /** Called by the exam screen after a paper is submitted. */
    public void showResultsAfterExam() {
        resultPanel.loadResults();
        cardLayout.show(contentPanel, "RESULT");
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
