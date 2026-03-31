package org.example.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Thông tin kết nối mặc định của WampServer
    private static final String URL = "jdbc:mysql://localhost:3306/db_haucan";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Wamp mặc định không có pass

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Lỗi kết nối CSDL: " + e.getMessage());
            return null;
        }
    }
}