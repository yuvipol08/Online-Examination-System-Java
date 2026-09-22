package com.onlineexam;

import com.onlineexam.db.DBConnection;
import com.onlineexam.ui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Starting point of the Online Examination System.
 * It first checks that MySQL can be reached and then opens the login window.
 */
public class MainApp {

    public static void main(String[] args) {

        // Use the look and feel of the operating system so the screens
        // look like normal Windows or Linux windows.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If it fails the default Swing look and feel is used, which is fine.
        }

        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Could not connect to the MySQL database.\n\n"
                    + "Please check that :\n"
                    + "  1. The MySQL service is running.\n"
                    + "  2. The database online_exam_db has been created using\n"
                    + "     the script database/online_exam_db.sql\n"
                    + "  3. The user name and password in DBConnection.java are correct.",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
