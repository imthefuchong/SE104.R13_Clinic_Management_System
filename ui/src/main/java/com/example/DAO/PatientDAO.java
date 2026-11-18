package com.example.DAO;

import com.example.model.FilterDate;
import com.example.model.PatientModel;
import com.example.utils.DatabaseConnector;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class PatientDAO {
    // CREATE
    public static boolean insert(PatientModel patient) {
        BigDecimal maxValue = QuiDinhDAO.getGiaTri("MAX_PATIENT_PER_DAY");
        int maxPatientsPerDay = (maxValue != null) ? maxValue.intValue() : 50;
        // Đếm số bệnh nhân đã có lịch hẹn trong ngày hôm nay
        FilterDate filter = FilterDate.fromLocalDate(LocalDate.now()); // mode: "Ngày"
        int currentCount = HenKhamBenhDAO.countDistinctPatientsByDate(filter);
        if (currentCount >= maxPatientsPerDay) {
            System.err.println("Đã đạt số lượng bệnh nhân tối đa trong ngày.");
            return false;
        }
        String[] nameParts = patient.getHoTen().split(" ", 2);
        String ho = nameParts.length > 1 ? nameParts[0] : "";
        String ten = nameParts.length > 1 ? nameParts[1] : nameParts[0];

        try (Connection conn = DatabaseConnector.connect()) {
            boolean hasAddress = hasColumn(conn, "BenhNhan", "DiaChi");
            String sql = hasAddress
                    ? "INSERT INTO BenhNhan (MaBenhNhan, Ho, Ten, NgaySinh, SDT, GioiTinh, DiaChi) VALUES (?, ?, ?, ?, ?, ?, ?)"
                    : "INSERT INTO BenhNhan (MaBenhNhan, Ho, Ten, NgaySinh, SDT, GioiTinh) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, patient.getMaBenhNhan());
                stmt.setString(2, ho);
                stmt.setString(3, ten);
                stmt.setDate(4, Date.valueOf(patient.getNgaySinh()));
                stmt.setString(5, patient.getSoDienThoai());
                stmt.setString(6, patient.getGioiTinh());
                if (hasAddress) {
                    stmt.setString(7, patient.getDiaChi());
                }
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm bệnh nhân: " + e.getMessage());
        }
        return false;
    }

    // READ
    public static PatientModel getById(String maBenhNhan) {
        String sql = "SELECT * FROM BenhNhan WHERE MaBenhNhan = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maBenhNhan);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hoTen = rs.getString("Ho") + " " + rs.getString("Ten");
                return new PatientModel(
                        rs.getString("MaBenhNhan"),
                        hoTen,
                        rs.getDate("NgaySinh").toLocalDate(),
                        rs.getString("SDT"),
                        rs.getString("GioiTinh"),
                        getOptionalString(rs, "DiaChi")
                );
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy thông tin bệnh nhân: " + e.getMessage());
        }

        return null;
    }

    // UPDATE
    public static boolean update(PatientModel patient) {
        String[] nameParts = patient.getHoTen().split(" ", 2);
        String ho = nameParts.length > 1 ? nameParts[0] : "";
        String ten = nameParts.length > 1 ? nameParts[1] : nameParts[0];

        try (Connection conn = DatabaseConnector.connect()) {
            boolean hasAddress = hasColumn(conn, "BenhNhan", "DiaChi");
            String sql = hasAddress
                    ? "UPDATE BenhNhan SET Ho = ?, Ten = ?, NgaySinh = ?, SDT = ?, GioiTinh = ?, DiaChi = ? WHERE MaBenhNhan = ?"
                    : "UPDATE BenhNhan SET Ho = ?, Ten = ?, NgaySinh = ?, SDT = ?, GioiTinh = ? WHERE MaBenhNhan = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, ho);
                stmt.setString(2, ten);
                stmt.setDate(3, Date.valueOf(patient.getNgaySinh()));
                stmt.setString(4, patient.getSoDienThoai());
                stmt.setString(5, patient.getGioiTinh());
                if (hasAddress) {
                    stmt.setString(6, patient.getDiaChi());
                    stmt.setString(7, patient.getMaBenhNhan());
                } else {
                    stmt.setString(6, patient.getMaBenhNhan());
                }
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật bệnh nhân: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public static boolean delete(String maBenhNhan) {
        String sql = "DELETE FROM BenhNhan WHERE MaBenhNhan = ?";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maBenhNhan);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa bệnh nhân: " + e.getMessage());
        }
        return false;
    }

    // READ ALL
    public static List<PatientModel> getAll() {
        List<PatientModel> list = new ArrayList<>();
        String sql = "SELECT * FROM BenhNhan ORDER BY MaBenhNhan";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String hoTen = rs.getString("Ho") + " " + rs.getString("Ten");
                list.add(new PatientModel(
                        rs.getString("MaBenhNhan"),
                        hoTen,
                        rs.getDate("NgaySinh").toLocalDate(),
                        rs.getString("SDT"),
                        rs.getString("GioiTinh"),
                        getOptionalString(rs, "DiaChi")
                ));
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách bệnh nhân: " + e.getMessage());
        }
        return list;
    }

    // COUNT
    public static int getPatientCount() {
        String sql = "SELECT COUNT(*) as total FROM BenhNhan";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi đếm số lượng bệnh nhân: " + e.getMessage());
        }
        return 0;
    }

    public static int getNextIdNumber(String prefix) {
        String sql = "SELECT MaBenhNhan " +
                "FROM BenhNhan " +
                "WHERE MaBenhNhan LIKE ? " +
                "AND MaBenhNhan ~ ? " +
                "ORDER BY CAST(SUBSTRING(MaBenhNhan FROM ?) AS INTEGER) DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            stmt.setString(2, "^" + prefix + "[0-9]+$");
            stmt.setString(3, String.format("^.{%d}(\\d+)$", prefix.length()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String patientId = rs.getString("MaBenhNhan");
                if (patientId != null && patientId.startsWith(prefix)) {
                    try {
                        String numberPart = patientId.substring(prefix.length());
                        if (numberPart.matches("\\d+")) {
                            int currentNumber = Integer.parseInt(numberPart);
                            System.out.println("Found max ID: " + patientId + ", number: " + currentNumber);
                            return currentNumber + 1;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing number from: " + patientId);
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

    private static boolean hasColumn(Connection conn, String table, String column) throws SQLException {
        try (Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " + table + " WHERE 1 = 0")) {
            ResultSetMetaData metadata = rs.getMetaData();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                if (column.equalsIgnoreCase(metadata.getColumnLabel(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getOptionalString(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData metadata = rs.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metadata.getColumnLabel(i))) {
                return rs.getString(i);
            }
        }
        return "";
    }

}
