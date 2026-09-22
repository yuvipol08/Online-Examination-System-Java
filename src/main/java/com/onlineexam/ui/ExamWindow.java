package com.onlineexam.ui;

import com.onlineexam.dao.ResultDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.ExamSession;
import com.onlineexam.model.Question;
import com.onlineexam.model.Result;
import com.onlineexam.util.UITheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The examination screen. This is the main screen of the project.
 *
 * One question is shown at a time with its four options. A countdown
 * timer runs at the top, the numbered buttons on the right show which
 * questions have been answered, and the whole paper is written to the
 * database in one transaction when Submit is pressed or when the time
 * is over.
 *
 * The window is modal, so the student cannot use the dashboard menu
 * while the examination is going on.
 */
public class ExamWindow extends JDialog {

    /** The timer turns orange when this many seconds or fewer are left. */
    private static final int WARNING_SECONDS = 60;

    private final ExamSession session;
    private final int userId;
    private final ResultDAO resultDAO = new ResultDAO();

    private final JLabel timerLabel = new JLabel();
    private final JLabel numberLabel = new JLabel();
    private final JTextArea questionArea = new JTextArea(3, 40);
    private final JRadioButton[] optionButtons = new JRadioButton[4];
    private final ButtonGroup optionGroup = new ButtonGroup();
    private final List<JButton> paletteButtons = new ArrayList<>();
    private final JLabel statusLabel = new JLabel();

    private final JButton previousButton = UITheme.createButton("<< Previous", UITheme.SIDEBAR);
    private final JButton nextButton = UITheme.createButton("Next >>", UITheme.BUTTON);

    private Timer countdown;
    private int secondsLeft;
    private boolean submitted = false;

    /** The result of the attempt, read by the dashboard after the window closes. */
    private Result result;

    private static final String[] LETTERS = {"A", "B", "C", "D"};

    public ExamWindow(Window parent, ExamSession session, int userId) {
        super(parent, "Examination in progress", ModalityType.APPLICATION_MODAL);

        this.session = session;
        this.userId = userId;
        this.secondsLeft = session.getExam().getDurationMinutes() * 60;

        UITheme.setSizeWithinScreen(this, 960, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Closing with the X is not allowed to throw the attempt away silently.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                askBeforeClosing();
            }
        });

        add(buildHeader(), BorderLayout.NORTH);
        add(buildQuestionArea(), BorderLayout.CENTER);
        add(buildPalette(), BorderLayout.EAST);
        add(buildButtons(), BorderLayout.SOUTH);

        showQuestion();
        startCountdown();
    }

    // ----------------------------------------------------------------
    //  Building the screen
    // ----------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        Exam exam = session.getExam();

        JLabel title = new JLabel(exam.getExamTitle() + "   (" + exam.getSubjectName() + ")");
        title.setFont(UITheme.HEADING_FONT);
        title.setForeground(Color.WHITE);

        JLabel marks = new JLabel("Total Marks : " + exam.getTotalMarks()
                + "    Questions : " + session.getTotalQuestions());
        marks.setFont(UITheme.LABEL_FONT);
        marks.setForeground(Color.WHITE);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(UITheme.HEADER);
        left.add(title);
        left.add(marks);

        timerLabel.setFont(UITheme.TIMER_FONT);
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setOpaque(true);
        timerLabel.setBackground(UITheme.SUCCESS);
        timerLabel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        timerLabel.setText("Time Left  " + UITheme.formatTime(secondsLeft));

        header.add(left, BorderLayout.WEST);
        header.add(timerLabel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildQuestionArea() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 10, 8));

        numberLabel.setFont(UITheme.HEADING_FONT);
        numberLabel.setForeground(UITheme.HEADER);

        questionArea.setFont(UITheme.QUESTION_FONT);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);          // the paper cannot be typed into
        questionArea.setBackground(Color.WHITE);
        questionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setBackground(UITheme.BACKGROUND);
        top.add(numberLabel, BorderLayout.NORTH);
        top.add(questionArea, BorderLayout.CENTER);

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBackground(Color.WHITE);
        options.setBorder(UITheme.createGroupBorder("Choose one option"));

        for (int i = 0; i < optionButtons.length; i++) {
            final String letter = LETTERS[i];

            JRadioButton button = new JRadioButton();
            button.setFont(UITheme.OPTION_FONT);
            button.setBackground(Color.WHITE);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                session.selectOption(letter);
                refreshPalette();
                refreshStatus();
            });

            optionGroup.add(button);
            optionButtons[i] = button;
            options.add(button);
            options.add(Box.createVerticalStrut(6));
        }

        panel.add(top, BorderLayout.NORTH);
        panel.add(options, BorderLayout.CENTER);
        return panel;
    }

    /**
     * The numbered buttons on the right. Green means the question has been
     * answered, grey means it has not, and the question on the screen is
     * outlined so the student can see where they are.
     */
    private JPanel buildPalette() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 8, 10, 14));
        panel.setPreferredSize(new Dimension(230, 0));

        JPanel grid = new JPanel(new GridLayout(0, 5, 6, 6));
        grid.setBackground(Color.WHITE);

        // The grid sits at the top of a holder, otherwise the layout would
        // stretch the little numbered buttons over the whole height.
        JPanel gridHolder = new JPanel(new BorderLayout());
        gridHolder.setBackground(Color.WHITE);
        gridHolder.setBorder(UITheme.createGroupBorder("Questions"));
        gridHolder.add(grid, BorderLayout.NORTH);

        for (int i = 0; i < session.getTotalQuestions(); i++) {
            final int index = i;

            JButton button = new JButton(String.valueOf(i + 1));
            button.setUI(new BasicButtonUI());
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.setForeground(Color.WHITE);
            button.setOpaque(true);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setPreferredSize(new Dimension(34, 30));
            button.addActionListener(e -> {
                session.goTo(index);
                showQuestion();
            });

            paletteButtons.add(button);
            grid.add(button);
        }

        statusLabel.setFont(UITheme.LABEL_FONT);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scroll = new JScrollPane(gridHolder);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panel.setBackground(UITheme.BACKGROUND);

        JButton clearButton = UITheme.createButton("Clear Answer", UITheme.WARNING);
        JButton submitButton = UITheme.createButton("Submit Exam", UITheme.DANGER);

        previousButton.addActionListener(e -> {
            session.previous();
            showQuestion();
        });
        nextButton.addActionListener(e -> {
            session.next();
            showQuestion();
        });
        clearButton.addActionListener(e -> {
            session.clearCurrentAnswer();
            optionGroup.clearSelection();
            refreshPalette();
            refreshStatus();
        });
        submitButton.addActionListener(e -> askBeforeSubmitting());

        panel.add(previousButton);
        panel.add(clearButton);
        panel.add(nextButton);
        panel.add(submitButton);
        return panel;
    }

    // ----------------------------------------------------------------
    //  Showing one question
    // ----------------------------------------------------------------

    /** Puts the current question, its options and the saved answer on the screen. */
    private void showQuestion() {
        Question question = session.getCurrentQuestion();

        numberLabel.setText("Question " + (session.getCurrentIndex() + 1)
                + " of " + session.getTotalQuestions());
        questionArea.setText(question.getQuestionText());
        questionArea.setCaretPosition(0);

        optionGroup.clearSelection();
        String selected = session.getSelectedOption(question.getQuestionId());

        for (int i = 0; i < optionButtons.length; i++) {
            optionButtons[i].setText(LETTERS[i] + ".  " + question.getOption(LETTERS[i]));
            if (LETTERS[i].equals(selected)) {
                optionButtons[i].setSelected(true);
            }
        }

        previousButton.setEnabled(session.hasPrevious());
        nextButton.setEnabled(session.hasNext());

        refreshPalette();
        refreshStatus();
    }

    private void refreshPalette() {
        for (int i = 0; i < paletteButtons.size(); i++) {
            JButton button = paletteButtons.get(i);
            int questionId = session.getQuestions().get(i).getQuestionId();

            button.setBackground(session.isAnswered(questionId)
                    ? UITheme.SUCCESS : new Color(127, 140, 141));

            button.setBorder(i == session.getCurrentIndex()
                    ? BorderFactory.createLineBorder(UITheme.HEADER, 3)
                    : BorderFactory.createEmptyBorder(3, 3, 3, 3));
        }
    }

    private void refreshStatus() {
        statusLabel.setText("Answered : " + session.getAnsweredCount()
                + "    Left : " + session.getNotAnsweredCount());
    }

    // ----------------------------------------------------------------
    //  The countdown
    // ----------------------------------------------------------------

    /**
     * Starts the clock. The Swing timer fires once every second on the
     * event dispatch thread, so the label can be changed straight from
     * inside it. When the time reaches zero the paper is submitted on
     * its own.
     */
    private void startCountdown() {
        countdown = new Timer(1000, e -> {
            secondsLeft--;
            timerLabel.setText("Time Left  " + UITheme.formatTime(secondsLeft));

            if (secondsLeft <= WARNING_SECONDS) {
                timerLabel.setBackground(secondsLeft % 2 == 0 ? UITheme.DANGER : UITheme.WARNING);
            }

            if (secondsLeft <= 0) {
                countdown.stop();
                JOptionPane.showMessageDialog(this,
                        "The time is over. Your paper is being submitted.",
                        "Time Over", JOptionPane.INFORMATION_MESSAGE);
                submitExam();
            }
        });
        countdown.start();
    }

    // ----------------------------------------------------------------
    //  Submitting
    // ----------------------------------------------------------------

    private void askBeforeSubmitting() {
        String message = "Answered : " + session.getAnsweredCount()
                + "\nNot answered : " + session.getNotAnsweredCount()
                + "\n\nDo you want to submit the paper now?";

        int choice = JOptionPane.showConfirmDialog(this, message,
                "Submit Exam", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            submitExam();
        }
    }

    private void askBeforeClosing() {
        int choice = JOptionPane.showConfirmDialog(this,
                "The examination is still going on.\n"
                + "Closing this window will submit the paper as it is now.\n\n"
                + "Do you want to submit it?",
                "Submit Exam", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            submitExam();
        }
    }

    /**
     * Writes the attempt to the database. The timer is stopped first so
     * that it cannot fire again while the rows are being saved.
     */
    private void submitExam() {
        if (submitted) {
            return;
        }
        if (countdown != null) {
            countdown.stop();
        }

        try {
            result = resultDAO.submitExam(session, userId);
            submitted = true;
            dispose();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "The paper could not be saved :\n" + e.getMessage()
                    + "\n\nNothing has been written to the database. "
                    + "Please try to submit again.",
                    "Submit Exam", JOptionPane.ERROR_MESSAGE);

            if (secondsLeft > 0 && countdown != null) {
                countdown.start();   // let the student try again
            }
        }
    }

    /** The saved result, or null when the window was closed without submitting. */
    public Result getResult() {
        return result;
    }
}
