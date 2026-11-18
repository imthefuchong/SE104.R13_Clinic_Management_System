package com.example.DAO;

import com.example.utils.DatabaseConnector;
import com.example.model.PatientReportModel;
import java.util.stream.Collectors;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PatientReportDAO {

    public static List<PatientReportModel> getDailyPatientReports() {
        List<PatientReportModel> reportList = new ArrayList<>();

        String dateSql = "SELECT DISTINCT NgayLapHoaDon FROM HoaDon WHERE NgayLapHoaDon IS NOT NULL ORDER BY NgayLapHoaDon";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(dateSql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("NgayLapHoaDon").toLocalDate();
                int patientCount = getDistinctPatientCountByDate(date, conn);
                double revenue = getRevenueByDate(date, conn);
                double monthlyRevenue = getMonthlyRevenue(date.getYear(), date.getMonthValue(), conn);
                double rate = monthlyRevenue > 0 ? revenue / monthlyRevenue : 0;

                PatientReportModel model = new PatientReportModel(date, patientCount, revenue, rate);
                reportList.add(model);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reportList;
    }

    private static int getDistinctPatientCountByDate(LocalDate date, Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(DISTINCT pkb.MaBenhNhan)
            FROM HoaDon hd
            JOIN PhieuKhamBenh pkb ON hd.MaPhieuKham = pkb.MaPhieuKham
            WHERE hd.NgayLapHoaDon = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }

        return 0;
    }

    private static double getRevenueByDate(LocalDate date, Connection conn) throws SQLException {
        String sql = """
            SELECT SUM(GiaTien)
            FROM HoaDon
            WHERE NgayLapHoaDon = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }

        return 0;
    }

    private static double getMonthlyRevenue(int year, int month, Connection conn) throws SQLException {
        String sql = """
            SELECT SUM(GiaTien)
            FROM HoaDon
            WHERE EXTRACT(YEAR FROM NgayLapHoaDon) = ? AND EXTRACT(MONTH FROM NgayLapHoaDon) = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }

        return 0;
    }

    public static List<PatientReportModel> getDailyPatientReportsByMonth(int year, int month) {
        List<PatientReportModel> fullList = getDailyPatientReports();
        return fullList.stream()
                .filter(model -> model.getDate().getYear() == year && model.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

}

