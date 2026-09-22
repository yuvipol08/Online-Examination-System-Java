package com.onlineexam.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single place where the JDBC connection to MySQL is created.
 * Every DAO class calls DBConnection.getConnection() so the
 * database settings have to be changed in one file only.
 */
public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/online_exam_db?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    /**
     * Loads the MySQL driver and returns a new connection.
     * The caller closes it using try-with-resources.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. "
                    + "Please add mysql-connector-j to the project libraries.");
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Used by MainApp when the application starts, so that a wrong
     * password or a stopped MySQL service is reported immediately
     * instead of failing later on some screen.
     */
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null;
        } catch (SQLException e) {
            return false;
        }
    }
}
