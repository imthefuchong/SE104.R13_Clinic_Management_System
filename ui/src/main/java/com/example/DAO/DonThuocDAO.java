package com.example.DAO;

import com.example.model.DonThuocModel;
import com.example.utils.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonThuocDAO {

    public static int getNextIdNumber(String prefix) {
        String sql = """
        SELECT MaDonThuoc
        FROM DonThuoc
        WHERE MaDonThuoc LIKE ?
          AND MaDonThuoc ~ ?
        ORDER BY CAST(SUBSTRING(MaDonThuoc FROM ?) AS INTEGER) DESC
        LIMIT 1
    """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            stmt.setString(2, "^" + prefix + "[0-9]+$"); // e.g., ^DT[0-9]+$
            stmt.setString(3, String.format("^.{%d}(\\d+)$", prefix.length())); // e.g., ^.{2}(\d+)$

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String maDonThuoc = rs.getString("MaDonThuoc");
                if (maDonThuoc != null && maDonThuoc.startsWith(prefix)) {
                    String numberPart = maDonThuoc.substring(prefix.length());
                    if (numberPart.matches("\\d+")) {
                        int currentNumber = Integer.parseInt(numberPart);
                        System.out.println("Found max MaDonThuoc: " + maDonThuoc + " → next: " + (currentNumber + 1));
                        return currentNumber + 1;
                    }
                }
            }

            System.out.println("No existing MaDonThuoc found with prefix: " + prefix);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getNextIdNumber: " + e.getMessage());
            return 1;
        }
    }


    public static boolean insert(DonThuocModel dt) {
        String sql = "INSERT INTO DonThuoc (MaDonThuoc, MaPhieuKham, NgayLapDon) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dt.getMaDonThuoc());
            stmt.setString(2, dt.getMaPhieuKham());
            stmt.setTimestamp(3, Timestamp.valueOf(dt.getNgayLapDon()));
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<DonThuocModel> getAll() {
        List<DonThuocModel> list = new ArrayList<>();
        String sql = "SELECT * FROM DonThuoc";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                DonThuocModel dt = new DonThuocModel(
                        rs.getString("MaDonThuoc"),
                        rs.getString("MaPhieuKham"),
                        rs.getTimestamp("NgayLapDon").toLocalDateTime()
                );
                list.add(dt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
