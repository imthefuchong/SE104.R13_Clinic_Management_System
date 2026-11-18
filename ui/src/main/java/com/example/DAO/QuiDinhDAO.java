package com.example.DAO;

import com.example.utils.DatabaseConnector;

import java.sql.*;
import java.math.BigDecimal;

public class QuiDinhDAO {

    // Lấy giá trị của một quy định (kiểu BigDecimal)
    public static BigDecimal getGiaTri(String tenQuiDinh) {
        String sql = "SELECT GiaTri FROM QuiDinh WHERE TenQuiDinh = ?";

        Connection connection = DatabaseConnector.connect();
        if (connection == null) {
            return null;
        }
        try (Connection conn = connection;
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tenQuiDinh);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("GiaTri");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy giá trị quy định: " + e.getMessage());
        }

        return null;
    }

    // Cập nhật giá trị của một quy định
    public static boolean updateGiaTri(String tenQuiDinh, BigDecimal newGiaTri) {
        String sql = "UPDATE QuiDinh SET GiaTri = ? WHERE TenQuiDinh = ?";

        Connection connection = DatabaseConnector.connect();
        if (connection == null) {
            return false;
        }
        try (Connection conn = connection;
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBigDecimal(1, newGiaTri);
            stmt.setString(2, tenQuiDinh);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật giá trị quy định: " + e.getMessage());
            return false;
        }
    }
}
