package com.example.DAO;

import com.example.model.AppointmentModel;
import com.example.utils.DatabaseConnector;
import com.example.model.FilterDate;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class HenKhamBenhDAO {

    public static AppointmentModel getAppointmentWithPatient(String maKhamBenh) {
        String sql = """
        SELECT h.MaKhamBenh, h.MaBenhNhan, h.LyDoKham, h.NgayKham, h.GioBatDau, h.GioKetThuc,
               h.MaBacSi,
               b.HoTen, b.NgaySinh, b.SDT, b.GioiTinh
        FROM HenKhamBenh h
        JOIN BenhNhan b ON h.MaBenhNhan = b.MaBenhNhan
        WHERE h.MaKhamBenh = ?
    """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maKhamBenh);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AppointmentModel model = new AppointmentModel();
                model.setMaKhamBenh(rs.getString("MaKhamBenh"));
                model.setMaBenhNhan(rs.getString("MaBenhNhan"));
                model.setLyDoKham(rs.getString("LyDoKham"));
                model.setNgayKham(rs.getDate("NgayKham").toLocalDate());
                model.setGioBatDau(rs.getTime("GioBatDau").toLocalTime());
                model.setGioKetThuc(rs.getTime("GioKetThuc").toLocalTime());
                model.setMaBacSi(rs.getString("MaBacSi"));

                model.setHoTen(rs.getString("HoTen"));
                model.setNgaySinh(rs.getDate("NgaySinh").toLocalDate());
                model.setSoDienThoai(rs.getString("SDT"));
                model.setGioiTinh(rs.getString("GioiTinh"));
                return model;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static List<AppointmentModel> getAll() {
        List<AppointmentModel> list = new ArrayList<>();

        String sql = """
        SELECT h.MaKhamBenh, h.MaBenhNhan, h.LyDoKham, h.NgayKham, h.GioBatDau, h.GioKetThuc,
               h.MaBacSi,
               b.Ho, b.Ten, b.NgaySinh, b.SDT, b.GioiTinh
        FROM HenKhamBenh h
        JOIN BenhNhan b ON h.MaBenhNhan = b.MaBenhNhan
        ORDER BY h.NgayKham DESC
    """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AppointmentModel model = new AppointmentModel();
                model.setMaKhamBenh(rs.getString("MaKhamBenh"));
                model.setMaBenhNhan(rs.getString("MaBenhNhan"));
                model.setLyDoKham(rs.getString("LyDoKham"));
                model.setNgayKham(rs.getDate("NgayKham").toLocalDate());
                model.setGioBatDau(rs.getTime("GioBatDau").toLocalTime());
                model.setGioKetThuc(rs.getTime("GioKetThuc").toLocalTime());
                model.setMaBacSi(rs.getString("MaBacSi"));

                String ho = rs.getString("Ho");
                String ten = rs.getString("Ten");
                model.setHoTen((ho != null ? ho : "") + " " + (ten != null ? ten : ""));

                model.setNgaySinh(rs.getDate("NgaySinh").toLocalDate());
                model.setSoDienThoai(rs.getString("SDT"));
                model.setGioiTinh(rs.getString("GioiTinh"));

                list.add(model);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


    public static boolean insert(AppointmentModel model) {
        String sql = """
        INSERT INTO HenKhamBenh (MaKhamBenh, MaBenhNhan, LyDoKham, NgayKham, GioBatDau, GioKetThuc, MaBacSi)
        VALUES (?, ?, ?, ?, ?, ?, ?)
    """;
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, model.getMaKhamBenh());
            stmt.setString(2, model.getMaBenhNhan());
            stmt.setString(3, model.getLyDoKham());
            stmt.setDate(4, Date.valueOf(model.getNgayKham()));
            stmt.setTime(5, Time.valueOf(model.getGioBatDau()));
            stmt.setTime(6, Time.valueOf(model.getGioKetThuc()));
            stmt.setString(7, model.getMaBacSi());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean update(AppointmentModel model) {
        String sql = """
        UPDATE HenKhamBenh
        SET LyDoKham = ?, NgayKham = ?, GioBatDau = ?, GioKetThuc = ?, MaBacSi = ?
        WHERE MaKhamBenh = ?
    """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, model.getLyDoKham());
            stmt.setDate(2, Date.valueOf(model.getNgayKham()));
            stmt.setTime(3, Time.valueOf(model.getGioBatDau()));
            stmt.setTime(4, Time.valueOf(model.getGioKetThuc()));
            stmt.setString(5, model.getMaBacSi());
            stmt.setString(6, model.getMaKhamBenh());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Xóa lịch hẹn theo mã
    public static boolean delete(String maKhamBenh) {
        String sql = "DELETE FROM HenKhamBenh WHERE MaKhamBenh = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maKhamBenh);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ✅ 5. Đếm số lượng bệnh nhân khác nhau theo ngày/tháng/năm
    public static int countDistinctPatientsByDate(FilterDate filter) {
        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(DISTINCT MaBenhNhan) AS SoBenhNhan
        FROM HenKhamBenh
        WHERE
    """);

        switch (filter.getMode()) {
            case "Năm" -> sql.append(" EXTRACT(YEAR FROM NgayKham) = ?");
            case "Tháng" -> sql.append(" EXTRACT(YEAR FROM NgayKham) = ? AND EXTRACT(MONTH FROM NgayKham) = ?");
            case "Ngày" -> sql.append(" NgayKham = ?");
            default -> throw new IllegalArgumentException("Chế độ lọc không hợp lệ: " + filter.getMode());
        }

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            switch (filter.getMode()) {
                case "Năm" -> stmt.setInt(1, filter.getYear().getValue());
                case "Tháng" -> {
                    stmt.setInt(1, filter.getYearMonth().getYear());
                    stmt.setInt(2, filter.getYearMonth().getMonthValue());
                }
                case "Ngày" -> stmt.setDate(1, Date.valueOf(filter.getLocalDate()));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("SoBenhNhan");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // ✅ 6. Đếm số lượng bệnh nhân khác nhau
    public static List<Integer> getPatientCountsBetween(FilterDate from, FilterDate to) {
        List<Integer> counts = new ArrayList<>();

        LocalDate start = from.getLocalDate();
        LocalDate end = to.getLocalDate();

        if (start == null || end == null) {
            throw new IllegalArgumentException("FilterDate phải ở chế độ 'Ngày'");
        }

        long daysBetween = ChronoUnit.DAYS.between(start, end);

        if (daysBetween <= 31) {
            // Đếm theo ngày
            for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
                FilterDate filter = new FilterDate("Ngày", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
                int count = countDistinctPatientsByDate(filter); // Không cần truyền connection
                counts.add(count);
            }
        } else {
            // Đếm theo tháng
            YearMonth startMonth = YearMonth.from(start);
            YearMonth endMonth = YearMonth.from(end);
            for (YearMonth ym = startMonth; !ym.isAfter(endMonth); ym = ym.plusMonths(1)) {
                FilterDate filter = new FilterDate("Tháng", 1, ym.getMonthValue(), ym.getYear());
                int count = countDistinctPatientsByDate(filter);
                counts.add(count);
            }
        }

        return counts;
    }

    public static int getNextIdNumber(String prefix) {
        String sql = "SELECT MaKhamBenh " +
                "FROM HenKhamBenh " +
                "WHERE MaKhamBenh LIKE ? " +
                "AND MaKhamBenh ~ ? " +
                "ORDER BY CAST(SUBSTRING(MaKhamBenh FROM ?) AS INTEGER) DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            stmt.setString(2, "^" + prefix + "[0-9]+$");
            stmt.setString(3, String.format("^.{%d}(\\d+)$", prefix.length()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String id = rs.getString("MaKhamBenh");
                if (id != null && id.startsWith(prefix)) {
                    try {
                        String numberPart = id.substring(prefix.length());
                        if (numberPart.matches("\\d+")) {
                            int currentNumber = Integer.parseInt(numberPart);
                            System.out.println("Found max MaKhamBenh: " + id + ", number: " + currentNumber);
                            return currentNumber + 1;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing number from: " + id);
                    }
                }
            }

            System.out.println("No existing MaKhamBenh found for prefix: " + prefix);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getNextIdNumber (HenKhamBenhDAO): " + e.getMessage());
            return 1;
        }
    }

}
