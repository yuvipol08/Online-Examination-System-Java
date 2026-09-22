package com.onlineexam.util;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Locale;

/**
 * Common colours, fonts and small helper methods used by every screen,
 * so that all the forms of the application look the same.
 */
public class UITheme {

    public static final Color HEADER     = new Color(41, 76, 133);   // blue title bars
    public static final Color SIDEBAR    = new Color(33, 47, 61);    // dark menu strip
    public static final Color BACKGROUND = new Color(244, 246, 249);
    public static final Color BUTTON     = new Color(41, 128, 185);  // blue action buttons
    public static final Color SUCCESS    = new Color(39, 174, 96);   // green save buttons
    public static final Color DANGER     = new Color(192, 57, 43);   // red delete buttons
    public static final Color WARNING    = new Color(211, 84, 0);    // orange, timer warning

    public static final Font TITLE_FONT    = new Font("SansSerif", Font.BOLD, 22);
    public static final Font HEADING_FONT  = new Font("SansSerif", Font.BOLD, 16);
    public static final Font QUESTION_FONT = new Font("SansSerif", Font.BOLD, 15);
    public static final Font OPTION_FONT   = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font LABEL_FONT    = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FIELD_FONT    = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font TABLE_FONT    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font TIMER_FONT    = new Font("SansSerif", Font.BOLD, 18);

    /** Makes a coloured button with white text used all over the application. */
    public static JButton createButton(String text, Color background) {
        JButton button = new JButton(text);
        // Some look and feels, including the Windows one, paint the button
        // themselves and ignore setBackground. Using the plain button painter
        // keeps the colours the same on every computer.
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(background);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    /**
     * Formats a percentage with two decimals and always with a dot, whatever
     * the regional settings of the computer are. Without this a computer set
     * to a region that writes 62,50 would break the screens that read the
     * number back out of a table.
     */
    public static String percent(double value) {
        return String.format(Locale.ENGLISH, "%.2f", value);
    }

    /** Turns a number of seconds into the mm:ss shown by the exam timer. */
    public static String formatTime(int totalSeconds) {
        if (totalSeconds < 0) {
            totalSeconds = 0;
        }
        return String.format(Locale.ENGLISH, "%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    /**
     * Sets the size of a window but never larger than the screen really has.
     * On a small screen, or on Windows with display scaling switched on, the
     * window is made smaller instead of its bottom going off the screen.
     */
    public static void setSizeWithinScreen(Window window, int width, int height) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        window.setSize(Math.min(width, screen.width), Math.min(height, screen.height));
    }

    /** Blue bar with the screen name, placed at the top of every window. */
    public static JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(TITLE_FONT);
        label.setOpaque(true);
        label.setBackground(HEADER);
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        return label;
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(LABEL_FONT);
        return label;
    }

    /** Gives every data table the same row height, fonts and header colour. */
    public static void styleTable(JTable table) {
        table.setFont(TABLE_FONT);
        table.setRowHeight(24);
        table.setGridColor(new Color(210, 210, 210));
        table.setSelectionBackground(new Color(214, 234, 248));
        table.setSelectionForeground(Color.BLACK);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.getTableHeader().setBackground(SIDEBAR);
        table.getTableHeader().setForeground(Color.WHITE);
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
    }

    /** Simple grey box with a caption, used to group the fields of a form. */
    public static Border createGroupBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(189, 195, 199)), title,
                        javax.swing.border.TitledBorder.LEFT,
                        javax.swing.border.TitledBorder.TOP,
                        HEADING_FONT, SIDEBAR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }
}
