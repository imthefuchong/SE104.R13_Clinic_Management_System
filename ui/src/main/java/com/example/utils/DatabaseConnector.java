package com.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:postgresql://localhost:5432/QLPM";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456";

    private static Connection connection = null;

    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Lỗi SQL: " + e.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy PostgreSQL Driver!");
            return null;
        }
    }

    // Phương thức kiểm tra kết nối
    public static boolean testConnection() {
        try (Connection conn = connect()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra kết nối: " + e.getMessage());
            return false;
        }
    }

    // Phương thức đóng connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối database.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }
}