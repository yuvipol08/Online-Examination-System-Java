package com.onlineexam.ui;

import com.onlineexam.dao.ExamDAO;
import com.onlineexam.dao.QuestionDAO;
import com.onlineexam.model.Exam;
import com.onlineexam.model.Question;
import com.onlineexam.util.UITheme;
import com.onlineexam.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Admin screen for the questions of one examination.
 * The examination is picked in the combo box at the top, the table then
 * shows the questions of that paper and the form below is used to add,
 * change or delete a question.
 */
public class QuestionPanel extends JPanel {

    private final QuestionDAO questionDAO = new QuestionDAO();
    private final ExamDAO examDAO = new ExamDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Question", "A", "B", "C", "D", "Correct"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(tableModel);
    private final JComboBox<Exam> examBox = new JComboBox<>();

    private final JTextArea questionArea = new JTextArea(3, 30);
    private final JTextField optionAField = new JTextField(14);
    private final JTextField optionBField = new JTextField(14);
    private final JTextField optionCField = new JTextField(14);
    private final JTextField optionDField = new JTextField(14);
    private final JComboBox<String> correctBox =
            new JComboBox<>(new String[]{"A", "B", "C", "D"});

    private int selectedId = 0;

    public QuestionPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        JPanel north = new JPanel(new BorderLayout());
        north.add(UITheme.createTitleLabel("MANAGE QUESTIONS"), BorderLayout.NORTH);
        north.add(buildExamChooser(), BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildExamChooser() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(UITheme.BACKGROUND);

        JLabel label = UITheme.createFormLabel("Select Exam :");
        examBox.setFont(UITheme.FIELD_FONT);
        examBox.addActionListener(e -> loadQuestions());

        panel.add(label);
        panel.add(examBox);
        return panel;
    }

    private JScrollPane buildTable() {
        UITheme.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> fillFormFromTable());

        int[] widths = {40, 300, 100, 100, 100, 100, 70};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 6, 10));
        return scroll;
    }

    private JPanel buildForm() {
        JPanel box = new JPanel(new GridBagLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(UITheme.createGroupBorder("Question Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 6, 5, 6);
        gbc.anchor = GridBagConstraints.WEST;

        questionArea.setFont(UITheme.FIELD_FONT);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199)));

        gbc.gridx = 0; gbc.gridy = 0;
        box.add(UITheme.createFormLabel("Question :"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        box.add(new JScrollPane(questionArea), gbc);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;

        optionAField.setFont(UITheme.FIELD_FONT);
        optionBField.setFont(UITheme.FIELD_FONT);
        optionCField.setFont(UITheme.FIELD_FONT);
        optionDField.setFont(UITheme.FIELD_FONT);

        gbc.gridx = 0; gbc.gridy = 1;
        box.add(UITheme.createFormLabel("Option A :"), gbc);
        gbc.gridx = 1;
        box.add(optionAField, gbc);
        gbc.gridx = 2;
        box.add(UITheme.createFormLabel("Option B :"), gbc);
        gbc.gridx = 3;
        box.add(optionBField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        box.add(UITheme.createFormLabel("Option C :"), gbc);
        gbc.gridx = 1;
        box.add(optionCField, gbc);
        gbc.gridx = 2;
        box.add(UITheme.createFormLabel("Option D :"), gbc);
        gbc.gridx = 3;
        box.add(optionDField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        box.add(UITheme.createFormLabel("Correct Option :"), gbc);
        gbc.gridx = 1;
        box.add(correctBox, gbc);

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

        gbc.gridx = 2; gbc.gridy = 3; gbc.gridwidth = 2;
        box.add(buttons, gbc);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.BACKGROUND);
        wrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        wrapper.add(box, BorderLayout.CENTER);
        return wrapper;
    }

    /** Reloads the examination list and then the questions of the first one. */
    public void refresh() {
        try {
            Exam previous = (Exam) examBox.getSelectedItem();
            examBox.removeAllItems();

            for (Exam exam : examDAO.getAllExams()) {
                examBox.addItem(exam);
                if (previous != null && previous.getExamId() == exam.getExamId()) {
                    examBox.setSelectedItem(exam);
                }
            }
            loadQuestions();

        } catch (SQLException e) {
            showError("Could not read the examinations :\n" + e.getMessage());
        }
    }

    private void loadQuestions() {
        Exam exam = (Exam) examBox.getSelectedItem();
        tableModel.setRowCount(0);

        if (exam == null) {
            return;
        }
        try {
            List<Question> questions = questionDAO.getQuestionsByExam(exam.getExamId());

            for (Question question : questions) {
                tableModel.addRow(new Object[]{question.getQuestionId(),
                                               question.getQuestionText(),
                                               question.getOptionA(),
                                               question.getOptionB(),
                                               question.getOptionC(),
                                               question.getOptionD(),
                                               question.getCorrectOption()});
            }
            clearForm();

        } catch (SQLException e) {
            showError("Could not read the questions :\n" + e.getMessage());
        }
    }

    private void fillFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        selectedId = (int) tableModel.getValueAt(row, 0);
        questionArea.setText(String.valueOf(tableModel.getValueAt(row, 1)));
        optionAField.setText(String.valueOf(tableModel.getValueAt(row, 2)));
        optionBField.setText(String.valueOf(tableModel.getValueAt(row, 3)));
        optionCField.setText(String.valueOf(tableModel.getValueAt(row, 4)));
        optionDField.setText(String.valueOf(tableModel.getValueAt(row, 5)));
        correctBox.setSelectedItem(String.valueOf(tableModel.getValueAt(row, 6)));
    }

    private void doAdd() {
        Question question = readForm();
        if (question == null) {
            return;
        }
        try {
            if (questionDAO.addQuestion(question)) {
                showInfo("The question was added.");
                loadQuestions();
            }
        } catch (SQLException e) {
            showError("Could not add the question :\n" + e.getMessage());
        }
    }

    private void doUpdate() {
        if (selectedId == 0) {
            showWarning("Please select the question you want to update.");
            return;
        }
        Question question = readForm();
        if (question == null) {
            return;
        }
        question.setQuestionId(selectedId);
        try {
            if (questionDAO.updateQuestion(question)) {
                showInfo("The question was updated.");
                loadQuestions();
            }
        } catch (SQLException e) {
            showError("Could not update the question :\n" + e.getMessage());
        }
    }

    private void doDelete() {
        if (selectedId == 0) {
            showWarning("Please select the question you want to delete.");
            return;
        }
        try {
            int answerCount = questionDAO.countAnswers(selectedId);
            if (answerCount > 0) {
                showWarning("This question cannot be deleted because it already appears\n"
                        + "on " + answerCount + " answer sheet(s) of students who wrote "
                        + "this examination.");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(this,
                    "Delete the selected question?", "Delete Question",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION && questionDAO.deleteQuestion(selectedId)) {
                showInfo("The question was deleted.");
                loadQuestions();
            }

        } catch (SQLException e) {
            showError("Could not delete the question :\n" + e.getMessage());
        }
    }

    /** Checks the boxes and builds a Question object, or returns null. */
    private Question readForm() {
        Exam exam = (Exam) examBox.getSelectedItem();
        if (exam == null) {
            showWarning("Please create an examination first, then add its questions.");
            return null;
        }

        String text = questionArea.getText().trim();
        String optionA = optionAField.getText().trim();
        String optionB = optionBField.getText().trim();
        String optionC = optionCField.getText().trim();
        String optionD = optionDField.getText().trim();
        String correct = String.valueOf(correctBox.getSelectedItem());

        if (Validator.isEmpty(text)) {
            showWarning("Please enter the question.");
            return null;
        }
        if (text.length() > 500) {
            showWarning("The question is too long. Please keep it under 500 characters.");
            return null;
        }
        if (Validator.isEmpty(optionA) || Validator.isEmpty(optionB)
                || Validator.isEmpty(optionC) || Validator.isEmpty(optionD)) {
            showWarning("All the four options are required.");
            return null;
        }
        if (!Validator.isValidOption(correct)) {
            showWarning("The correct option must be A, B, C or D.");
            return null;
        }

        Question question = new Question();
        question.setExamId(exam.getExamId());
        question.setQuestionText(text);
        question.setOptionA(optionA);
        question.setOptionB(optionB);
        question.setOptionC(optionC);
        question.setOptionD(optionD);
        question.setCorrectOption(correct);
        return question;
    }

    private void clearForm() {
        selectedId = 0;
        questionArea.setText("");
        optionAField.setText("");
        optionBField.setText("");
        optionCField.setText("");
        optionDField.setText("");
        correctBox.setSelectedIndex(0);
        table.clearSelection();
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Questions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Questions", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Questions", JOptionPane.ERROR_MESSAGE);
    }
}
