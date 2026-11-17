package com.example.DAO;

import com.example.model.AccountModel;
import com.example.utils.DatabaseConnector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {
    public static List<AccountModel> getAll() throws SQLException {
        String sql = """
                SELECT n.TenDangNhap, n.MaNhanVien, n.Ho, n.Ten, n.RoleID,
                       n.TrangThaiTaiKhoan
                FROM NhanVien n
                WHERE n.TenDangNhap IS NOT NULL
                  AND TRIM(n.TenDangNhap) <> ''
                ORDER BY n.TenDangNhap
                """;
        List<AccountModel> accounts = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String firstName = resultSet.getString("Ho");
                String lastName = resultSet.getString("Ten");
                accounts.add(new AccountModel(
                        resultSet.getString("TenDangNhap"),
                        resultSet.getString("MaNhanVien"),
                        ((firstName == null ? "" : firstName) + " "
                                + (lastName == null ? "" : lastName)).trim(),
                        resultSet.getString("RoleID"),
                        resultSet.getString("TrangThaiTaiKhoan"),
                        false
                ));
            }
        }
        return accounts;
    }

    public static List<String> getAvailableEmployeeLabels() throws SQLException {
        String sql = """
                SELECT MaNhanVien, Ho, Ten
                FROM NhanVien
                WHERE TenDangNhap IS NULL OR TRIM(TenDangNhap) = ''
                ORDER BY MaNhanVien
                """;
        List<String> employees = new ArrayList<>();
        try (Connection connection = connect();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                String fullName = ((resultSet.getString("Ho") == null ? "" : resultSet.getString("Ho"))
                        + " " + (resultSet.getString("Ten") == null ? "" : resultSet.getString("Ten"))).trim();
                employees.add(resultSet.getString("MaNhanVien") + " - " + fullName);
            }
        }
        return employees;
    }

    public static boolean create(
            String employeeId,
            String username,
            String password,
            String roleId,
            String status
    ) throws SQLException {
        String sql = """
                UPDATE NhanVien
                SET TenDangNhap = ?, MatKhau = ?, RoleID = ?, TrangThaiTaiKhoan = ?
                WHERE MaNhanVien = ?
                  AND (TenDangNhap IS NULL OR TRIM(TenDangNhap) = '')
                """;
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, roleId);
            statement.setString(4, status);
            statement.setString(5, employeeId);
            return statement.executeUpdate() == 1;
        }
    }

    public static boolean update(String employeeId, String roleId, String status) throws SQLException {
        String sql = """
                UPDATE NhanVien
                SET RoleID = ?, TrangThaiTaiKhoan = ?
                WHERE MaNhanVien = ? AND TenDangNhap IS NOT NULL
                """;
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roleId);
            statement.setString(2, status);
            statement.setString(3, employeeId);
            return statement.executeUpdate() == 1;
        }
    }

    public static boolean resetPassword(String employeeId, String password) throws SQLException {
        String sql = "UPDATE NhanVien SET MatKhau = ? WHERE MaNhanVien = ? AND TenDangNhap IS NOT NULL";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, password);
            statement.setString(2, employeeId);
            return statement.executeUpdate() == 1;
        }
    }

    public static boolean setStatus(String employeeId, String status) throws SQLException {
        String sql = "UPDATE NhanVien SET TrangThaiTaiKhoan = ? WHERE MaNhanVien = ? AND TenDangNhap IS NOT NULL";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setString(2, employeeId);
            return statement.executeUpdate() == 1;
        }
    }

    private static Connection connect() throws SQLException {
        Connection connection = DatabaseConnector.connect();
        if (connection == null) {
            throw new SQLException("Không thể kết nối CSDL tài khoản.");
        }
        return connection;
    }
}
