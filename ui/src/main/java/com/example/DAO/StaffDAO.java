package com.example.DAO;

import com.example.utils.DatabaseConnector;
import com.example.model.StaffModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    public static boolean insertStaff(StaffModel staff) {
        String sql = """
            INSERT INTO NhanVien (MaNhanVien, Ho, Ten, RoleID, Luong, NgaySinh, GioiTinh, CCCD, DiaChi, SDT, Email, MatKhau)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff.getId());
            stmt.setString(2, staff.getLastname());
            stmt.setString(3, staff.getFirstname());
            stmt.setString(4, staff.getRole());
            stmt.setDouble(5, staff.getLuong());
            stmt.setDate(6, Date.valueOf(staff.getBirthday()));
            stmt.setString(7, staff.getGender());
            stmt.setString(8, staff.getCccd());
            stmt.setString(9, staff.getAddress());
            stmt.setString(10, staff.getPhone());
            stmt.setString(11, staff.getEmail());
            stmt.setString(12, staff.getPassword());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e); // Ném lại ngoại lệ để xử lý ở controller
        }
    }

    public static boolean updateStaff(StaffModel staff) {
        String sql = """
            UPDATE NhanVien
            SET Ho = ?, Ten = ?, RoleID = ?, Luong = ?, NgaySinh = ?, GioiTinh = ?, CCCD = ?, DiaChi = ?, SDT = ?, Email = ?, MatKhau = ?
            WHERE MaNhanVien = ?
        """;
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff.getLastname());
            stmt.setString(2, staff.getFirstname());
            stmt.setString(3, staff.getRole());
            stmt.setDouble(4, staff.getLuong());
            stmt.setDate(5, Date.valueOf(staff.getBirthday()));
            stmt.setString(6, staff.getGender());
            stmt.setString(7, staff.getCccd());
            stmt.setString(8, staff.getAddress());
            stmt.setString(9, staff.getPhone());
            stmt.setString(10, staff.getEmail());
            stmt.setString(11, staff.getPassword());
            stmt.setString(12, staff.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e); // Ném lại ngoại lệ
        }
    }

    public static boolean deleteStaff(String id) {
        String sql = "DELETE FROM NhanVien WHERE MaNhanVien = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e); // Ném lại ngoại lệ
        }
    }

    public static List<StaffModel> getAll() {
        List<StaffModel> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien ORDER BY MaNhanVien"; // Thêm ORDER BY
        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(parseStaff(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e); // Ném lại ngoại lệ
        }
        return list;
    }

    private static StaffModel parseStaff(ResultSet rs) throws SQLException {
        StaffModel staff = new StaffModel();
        staff.setId(rs.getString("MaNhanVien"));
        staff.setLastname(rs.getString("Ho"));
        staff.setFirstname(rs.getString("Ten"));
        staff.setRole(rs.getString("RoleID"));
        staff.setLuong(rs.getDouble("Luong"));
        staff.setBirthday(rs.getDate("NgaySinh").toLocalDate());
        staff.setGender(rs.getString("GioiTinh"));
        staff.setCccd(rs.getString("CCCD"));
        staff.setAddress(rs.getString("DiaChi"));
        staff.setPhone(rs.getString("SDT"));
        staff.setEmail(rs.getString("Email"));
        staff.setPassword(rs.getString("MatKhau"));
        return staff;
    }


    public static int getNextIdNumber(String prefix) {
        // PostgreSQL: dùng ~ để regex, SUBSTRING(MaNhanVien FROM ...) để lấy phần số
        String sql = "SELECT MaNhanVien " +
                "FROM nhanvien " +
                "WHERE MaNhanVien LIKE ? " +
                "AND MaNhanVien ~ ? " +
                "ORDER BY CAST(SUBSTRING(MaNhanVien FROM ?) AS INTEGER) DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            stmt.setString(2, "^" + prefix + "[0-9]+$"); // Regex chuẩn PostgreSQL
            stmt.setString(3, String.format("^.{%d}(\\d+)$", prefix.length())); // Lấy số sau prefix

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String staffId = rs.getString("MaNhanVien");
                if (staffId != null && staffId.startsWith(prefix)) {
                    try {
                        String numberPart = staffId.substring(prefix.length());
                        if (numberPart.matches("\\d+")) {
                            int currentNumber = Integer.parseInt(numberPart);
                            System.out.println("Found max ID: " + staffId + ", number: " + currentNumber);
                            return currentNumber + 1;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing number from: " + staffId);
                    }
                }
            }

            System.out.println("No existing ID found for prefix: " + prefix);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getNextIdNumber: " + e.getMessage());
            return 1;
        }
    }

    public static StaffModel findByEmailAndId(String email, String maNhanVien) {
        String sql = "SELECT * FROM NhanVien WHERE Email = ? AND MaNhanVien = ?";
        Connection connection = DatabaseConnector.connect();
        if (connection == null) {
            return null;
        }
        try (Connection conn = connection;
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, maNhanVien);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return parseStaff(rs); // Hàm này bạn giữ nguyên
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static boolean resetPasswordByUsername(String username, String newPassword) {
        String sql = """
                UPDATE NhanVien
                SET MatKhau = ?
                WHERE TenDangNhap = ?
                """;
        Connection connection = DatabaseConnector.connect();
        if (connection == null) {
            return false;
        }
        try (Connection conn = connection;
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static StaffModel getByID(String maNhanVien) {
        String sql = "SELECT * FROM NhanVien WHERE MaNhanVien = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maNhanVien);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return parseStaff(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn nhân viên: " + e.getMessage());
        }
        return null;
    }

    public static List<String> getDoctorIds() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT MaNhanVien FROM NhanVien WHERE RoleID = 'DOCTOR' ORDER BY MaNhanVien";

        try (Connection conn = DatabaseConnector.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(rs.getString("MaNhanVien"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }



}