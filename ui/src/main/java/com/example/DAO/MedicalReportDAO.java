package com.example.DAO;


import com.example.model.BillModel;
import com.example.utils.DatabaseConnector;
import com.example.model.MedicalReportModel;
import com.example.model.MedicineModel;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MedicalReportDAO {

    // INSERT - Thêm PhieuKhamBenh
    public static boolean insertPhieuKhamBenh(MedicalReportModel medicalReport, LocalDateTime ngayKham, LocalDateTime ngayLapPhieu, String dieuTri, String ketQuaKham) {
        String insertPhieuKham = "INSERT INTO PhieuKhamBenh (MaPhieuKham, MaBenhNhan, NgayKham, NgayLapPhieu, ChanDoan, KetQuaKham, DieuTri, TienKham, MaBacSi) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        double tienKham = QuiDinhDAO.getGiaTri("DEFAULT_TIEN_KHAM").doubleValue();

        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false);
            try {
                // Insert into PhieuKhamBenh
                try (PreparedStatement stmtPhieu = conn.prepareStatement(insertPhieuKham)) {
                    stmtPhieu.setString(1, medicalReport.getMaPhieuKham());
                    stmtPhieu.setString(2, medicalReport.getMaBenhNhan());
                    stmtPhieu.setDate(3, Date.valueOf(ngayKham.toLocalDate()));
                    stmtPhieu.setTimestamp(4, Timestamp.valueOf(ngayLapPhieu));
                    stmtPhieu.setString(5, medicalReport.getChanDoan());
                    stmtPhieu.setString(6, ketQuaKham);
                    stmtPhieu.setString(7, dieuTri);
                    stmtPhieu.setDouble(8, tienKham);
                    stmtPhieu.setString(9, medicalReport.getMaBacSi());

                    int result = stmtPhieu.executeUpdate();
                    conn.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm phiếu khám bệnh: " + e.getMessage());
        }
        return false;
    }

    // READ - Lấy tất cả medical reports
    public static List<MedicalReportModel> getAllMedicalReports() {
        List<MedicalReportModel> reports = new ArrayList<>();
        String sql = """
    SELECT 
        h.MaKhamBenh,
        p.MaPhieuKham,
        h.MaBenhNhan,
        h.MaBacSi,
        CONCAT(bn.Ho, ' ', bn.Ten) AS HoTenBenhNhan,
        CONCAT(nv.Ho, ' ', nv.Ten) AS TenBacSi,
        bn.NgaySinh,
        bn.SDT AS SoDienThoai,
        bn.GioiTinh,
        h.LyDoKham,
        p.NgayLapPhieu,
        p.ChanDoan,
        hd.MaHoaDon
    FROM HenKhamBenh h
    INNER JOIN PhieuKhamBenh p 
        ON h.MaBenhNhan = p.MaBenhNhan 
        AND h.NgayKham = p.NgayKham
    INNER JOIN BenhNhan bn ON h.MaBenhNhan = bn.MaBenhNhan
    INNER JOIN NhanVien nv ON h.MaBacSi = nv.MaNhanVien
    LEFT JOIN HoaDon hd ON p.MaPhieuKham = hd.MaPhieuKham
    WHERE nv.RoleID = 'DOCTOR' OR nv.RoleID = 'BS'
    ORDER BY p.NgayLapPhieu DESC
""";


        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MedicalReportModel report = new MedicalReportModel();
                report.setMaKhamBenh(rs.getString("MaKhamBenh"));
                report.setMaPhieuKham(rs.getString("MaPhieuKham"));
                report.setMaBenhNhan(rs.getString("MaBenhNhan"));
                report.setMaBacSi(rs.getString("MaBacSi"));
                report.setHoTen(rs.getString("HoTenBenhNhan"));
                report.setTenBacSi(rs.getString("TenBacSi"));

                Date ngaySinh = rs.getDate("NgaySinh");
                if (ngaySinh != null) {
                    report.setNgaySinh(ngaySinh.toLocalDate());
                }

                report.setSoDienThoai(rs.getString("SoDienThoai"));
                report.setGioiTinh(rs.getString("GioiTinh"));
                report.setLyDoKham(rs.getString("LyDoKham"));

                Timestamp ngayLap = rs.getTimestamp("NgayLapPhieu");
                if (ngayLap != null) {
                    report.setNgayKham(ngayLap.toLocalDateTime());
                }

                report.setChanDoan(rs.getString("ChanDoan"));

                // Tạo BillModel nếu có thông tin tiền khám
                String maHoaDon = rs.getString("MaHoaDon");
                if (maHoaDon != null && !maHoaDon.isEmpty()) {
                    BillModel bill = BillDAO.getBillById(maHoaDon);
                    report.setHoaDon(bill);
                }

                reports.add(report);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách phiếu khám bệnh: " + e.getMessage());
        }

        return reports;
    }

    // READ - Lấy medical report theo ID
    public static MedicalReportModel getMedicalReportById(String maKhamBenh) {
        String sql = """
    SELECT 
        h.MaKhamBenh,
        p.MaPhieuKham,
        h.MaBenhNhan,
        h.MaBacSi,
        CONCAT(bn.Ho, ' ', bn.Ten) AS HoTenBenhNhan,
        CONCAT(nv.Ho, ' ', nv.Ten) AS TenBacSi,
        bn.NgaySinh,
        bn.SDT AS SoDienThoai,
        bn.GioiTinh,
        h.LyDoKham,
        p.NgayLapPhieu,
        p.ChanDoan,
        hd.MaHoaDon
    FROM HenKhamBenh h
    INNER JOIN PhieuKhamBenh p 
        ON h.MaBenhNhan = p.MaBenhNhan 
        AND h.NgayKham = p.NgayKham
    INNER JOIN BenhNhan bn ON h.MaBenhNhan = bn.MaBenhNhan
    INNER JOIN NhanVien nv ON h.MaBacSi = nv.MaNhanVien
    LEFT JOIN HoaDon hd ON p.MaPhieuKham = hd.MaPhieuKham
    WHERE h.MaKhamBenh = ? AND nv.RoleID = 'DOCTOR'
    ORDER BY p.NgayLapPhieu DESC
""";



        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maKhamBenh);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MedicalReportModel report = new MedicalReportModel();
                    report.setMaKhamBenh(rs.getString("MaKhamBenh"));
                    report.setMaPhieuKham(rs.getString("MaPhieuKham"));
                    report.setMaBenhNhan(rs.getString("MaBenhNhan"));
                    report.setMaBacSi(rs.getString("MaBacSi"));
                    report.setHoTen(rs.getString("HoTenBenhNhan"));
                    report.setTenBacSi(rs.getString("TenBacSi"));

                    Date ngaySinh = rs.getDate("NgaySinh");
                    if (ngaySinh != null) {
                        report.setNgaySinh(ngaySinh.toLocalDate());
                    }

                    report.setSoDienThoai(rs.getString("SoDienThoai"));
                    report.setGioiTinh(rs.getString("GioiTinh"));
                    report.setLyDoKham(rs.getString("LyDoKham"));

                    Timestamp ngayLap = rs.getTimestamp("NgayLapPhieu");
                    if (ngayLap != null) {
                        report.setNgayKham(ngayLap.toLocalDateTime());
                    }

                    report.setChanDoan(rs.getString("ChanDoan"));

                    // Tạo BillModel nếu có thông tin tiền khám
                    String maHoaDon = rs.getString("MaHoaDon");
                    if (maHoaDon != null && !maHoaDon.isEmpty()) {
                        BillModel bill = BillDAO.getBillById(maHoaDon);
                        report.setHoaDon(bill);
                    }

                    return report;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy phiếu khám bệnh theo ID: " + e.getMessage());
        }

        return null;
    }
    public static List<MedicalReportModel> getMedicalReportsByPatientId(String maBenhNhan) {
        List<MedicalReportModel> reports = new ArrayList<>();

        String sql = """
    SELECT 
        h.MaKhamBenh,
        p.MaPhieuKham,
        h.MaBenhNhan,
        h.MaBacSi,
        CONCAT(bn.Ho, ' ', bn.Ten) AS HoTenBenhNhan,
        CONCAT(nv.Ho, ' ', nv.Ten) AS TenBacSi,
        bn.NgaySinh,
        bn.SDT AS SoDienThoai,
        bn.GioiTinh,
        h.LyDoKham,
        p.NgayLapPhieu,
        p.ChanDoan,
        hd.MaHoaDon
    FROM HenKhamBenh h
    INNER JOIN PhieuKhamBenh p 
        ON h.MaBenhNhan = p.MaBenhNhan 
        AND h.NgayKham = p.NgayKham
    INNER JOIN BenhNhan bn ON h.MaBenhNhan = bn.MaBenhNhan
    INNER JOIN NhanVien nv ON h.MaBacSi = nv.MaNhanVien
    LEFT JOIN HoaDon hd ON p.MaPhieuKham = hd.MaPhieuKham
    WHERE h.MaBenhNhan = ? AND nv.RoleID = 'DOCTOR'
    ORDER BY p.NgayLapPhieu DESC
""";


        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maBenhNhan);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicalReportModel report = new MedicalReportModel();
                    report.setMaKhamBenh(rs.getString("MaKhamBenh"));
                    report.setMaPhieuKham(rs.getString("MaPhieuKham"));
                    report.setMaBenhNhan(rs.getString("MaBenhNhan"));
                    report.setMaBacSi(rs.getString("MaBacSi"));
                    report.setHoTen(rs.getString("HoTenBenhNhan"));
                    report.setTenBacSi(rs.getString("TenBacSi"));

                    Date ngaySinh = rs.getDate("NgaySinh");
                    if (ngaySinh != null) {
                        report.setNgaySinh(ngaySinh.toLocalDate());
                    }

                    report.setSoDienThoai(rs.getString("SoDienThoai"));
                    report.setGioiTinh(rs.getString("GioiTinh"));
                    report.setLyDoKham(rs.getString("LyDoKham"));

                    Timestamp ngayLap = rs.getTimestamp("NgayLapPhieu");
                    if (ngayLap != null) {
                        report.setNgayKham(ngayLap.toLocalDateTime());
                    }

                    report.setChanDoan(rs.getString("ChanDoan"));

                    String maHoaDon = rs.getString("MaHoaDon");
                    if (maHoaDon != null && !maHoaDon.isEmpty()) {
                        BillModel bill = BillDAO.getBillById(maHoaDon);
                        report.setHoaDon(bill);
                    }

                    reports.add(report);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy medical reports: " + e.getMessage());
        }

        return reports;
    }

    public static List<MedicalReportModel> getMedicalReportsByDate(LocalDate date) {
        List<MedicalReportModel> reports = new ArrayList<>();

        String sql = """
    SELECT 
        h.MaKhamBenh,
        p.MaPhieuKham,
        h.MaBenhNhan,
        h.MaBacSi,
        CONCAT(bn.Ho, ' ', bn.Ten) AS HoTenBenhNhan,
        CONCAT(nv.Ho, ' ', nv.Ten) AS TenBacSi,
        bn.NgaySinh,
        bn.SDT AS SoDienThoai,
        bn.GioiTinh,
        h.LyDoKham,
        p.KetQuaKham,
        p.ChanDoan,
        p.DieuTri,
        p.TienKham,
        p.NgayKham,
        hd.MaHoaDon
    FROM HenKhamBenh h
    INNER JOIN PhieuKhamBenh p 
        ON h.MaBenhNhan = p.MaBenhNhan 
        AND h.NgayKham = p.NgayKham
    INNER JOIN BenhNhan bn ON h.MaBenhNhan = bn.MaBenhNhan
    INNER JOIN NhanVien nv ON h.MaBacSi = nv.MaNhanVien
    LEFT JOIN HoaDon hd ON p.MaPhieuKham = hd.MaPhieuKham
    WHERE p.NgayKham = ? AND nv.RoleID = 'DOCTOR'
    ORDER BY p.NgayKham DESC
    LIMIT 40
""";


        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicalReportModel report = new MedicalReportModel();
                    report.setMaKhamBenh(rs.getString("MaKhamBenh"));
                    report.setMaPhieuKham(rs.getString("MaPhieuKham"));
                    report.setMaBenhNhan(rs.getString("MaBenhNhan"));
                    report.setMaBacSi(rs.getString("MaBacSi"));
                    report.setHoTen(rs.getString("HoTenBenhNhan"));
                    report.setTenBacSi(rs.getString("TenBacSi"));

                    Date ngaySinh = rs.getDate("NgaySinh");
                    if (ngaySinh != null) {
                        report.setNgaySinh(ngaySinh.toLocalDate());
                    }

                    report.setSoDienThoai(rs.getString("SoDienThoai"));
                    report.setGioiTinh(rs.getString("GioiTinh"));
                    report.setLyDoKham(rs.getString("LyDoKham"));
                    report.setKetQuaKham(rs.getString("KetQuaKham"));
                    report.setChanDoan(rs.getString("ChanDoan"));
                    report.setDieuTri(rs.getString("DieuTri"));
                    report.setTienKham(rs.getDouble("TienKham"));

                    Timestamp ngayKham = rs.getTimestamp("NgayKham");
                    if (ngayKham != null) {
                        report.setNgayKham(ngayKham.toLocalDateTime());
                    }

                    String maHoaDon = rs.getString("MaHoaDon");
                    if (maHoaDon != null && !maHoaDon.isEmpty()) {
                        BillModel bill = BillDAO.getBillById(maHoaDon);
                        report.setHoaDon(bill);
                    }

                    reports.add(report);
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách phiếu khám bệnh theo ngày: " + e.getMessage());
        }

        return reports;
    }


    // UPDATE - Cập nhật medical report
    public static boolean updatePhieuKhamBenh(MedicalReportModel medicalReport, LocalDateTime ngayKham, LocalDateTime ngayLapPhieu, String dieuTri, String ketQuaKham, double tienKham) {
        String updatePhieuKham = "UPDATE PhieuKhamBenh SET MaBenhNhan = ?, NgayKham = ?, NgayLapPhieu = ?, ChanDoan = ?, KetQuaKham = ?, DieuTri = ?, TienKham = ?, MaBacSi = ? " +
                "WHERE MaPhieuKham = ?";

        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmtPhieu = conn.prepareStatement(updatePhieuKham)) {
                    stmtPhieu.setString(1, medicalReport.getMaBenhNhan());
                    stmtPhieu.setDate(2, Date.valueOf(ngayKham.toLocalDate()));
                    stmtPhieu.setTimestamp(3, Timestamp.valueOf(ngayLapPhieu));
                    stmtPhieu.setString(4, medicalReport.getChanDoan());
                    stmtPhieu.setString(5, ketQuaKham);
                    stmtPhieu.setString(6, dieuTri);
                    stmtPhieu.setDouble(7, tienKham);
                    stmtPhieu.setString(8, medicalReport.getMaBacSi());
                    stmtPhieu.setString(9, medicalReport.getMaPhieuKham()); // WHERE condition

                    int result = stmtPhieu.executeUpdate();
                    conn.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật phiếu khám bệnh: " + e.getMessage());
        }
        return false;
    }

    // DELETE - Xóa medical report
    public static boolean deleteMedicalReport(String maKhamBenh) {
        String deletePhieuKham = "DELETE FROM PhieuKhamBenh WHERE MaPhieuKham = ?";

        try (Connection conn = DatabaseConnector.connect()) {
            conn.setAutoCommit(false);
            try {
                // Delete PhieuKhamBenh first (foreign key constraint)
                try (PreparedStatement stmtPhieu = conn.prepareStatement(deletePhieuKham)) {
                    stmtPhieu.setString(1, maKhamBenh);
                    int result = stmtPhieu.executeUpdate();
                    conn.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa phiếu khám bệnh: " + e.getMessage());
        }
        return false;
    }

    public static MedicalReportModel getByMaPhieuKham(String maPhieuKham) {
        String sql = "SELECT * FROM PhieuKhamBenh WHERE MaPhieuKham = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maPhieuKham);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MedicalReportModel report = new MedicalReportModel();
                    report.setMaPhieuKham(rs.getString("MaPhieuKham"));
                    // Có thể set thêm các trường khác nếu cần
                    return report;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra mã phiếu khám: " + e.getMessage());
        }
        return null;
    }

    // Phương thức mới: Lấy đầy đủ thông tin phiếu khám bệnh bao gồm hóa đơn và đơn thuốc
    public static MedicalReportModel getCompleteMedicalReportByMaKhamBenh(String maKhamBenh) {
        String sql = """
                SELECT 
                    h.MaKhamBenh,
                    p.MaPhieuKham,
                    h.MaBenhNhan,
                    h.MaBacSi,
                    CONCAT(bn.Ho, ' ', bn.Ten) as HoTenBenhNhan,
                    CONCAT(nv.Ho, ' ', nv.Ten) as TenBacSi,
                    bn.NgaySinh,
                    bn.SDT as SoDienThoai,
                    bn.GioiTinh,
                    h.LyDoKham,
                    p.NgayKham,
                    p.NgayLapPhieu,
                    p.ChanDoan,
                    p.KetQuaKham,
                    p.DieuTri,
                    p.TienKham,
                    hd.MaHoaDon,
                    hd.TenHoaDon,
                    hd.NgayLapHoaDon,
                    hd.GiaTien as TongTienHoaDon,
                    hd.TrangThai,
                    dt.MaDonThuoc,
                    dt.NgayLapDon
                FROM HenKhamBenh h
                INNER JOIN PhieuKhamBenh p ON h.MaBenhNhan = p.MaBenhNhan AND h.NgayKham = p.NgayKham
                INNER JOIN BenhNhan bn ON h.MaBenhNhan = bn.MaBenhNhan
                INNER JOIN NhanVien nv ON h.MaBacSi = nv.MaNhanVien
                LEFT JOIN HoaDon hd ON p.MaPhieuKham = hd.MaPhieuKham
                LEFT JOIN DonThuoc dt ON p.MaPhieuKham = dt.MaPhieuKham
                WHERE h.MaKhamBenh = ?
            """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maKhamBenh);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MedicalReportModel report = new MedicalReportModel();
                    
                    // Thông tin cơ bản
                    report.setMaKhamBenh(rs.getString("MaKhamBenh"));
                    report.setMaPhieuKham(rs.getString("MaPhieuKham"));
                    report.setMaBenhNhan(rs.getString("MaBenhNhan"));
                    report.setMaBacSi(rs.getString("MaBacSi"));
                    report.setHoTen(rs.getString("HoTenBenhNhan"));
                    report.setTenBacSi(rs.getString("TenBacSi"));
                    report.setLyDoKham(rs.getString("LyDoKham"));
                    report.setChanDoan(rs.getString("ChanDoan"));
                    report.setKetQuaKham(rs.getString("KetQuaKham"));
                    report.setDieuTri(rs.getString("DieuTri"));
                    report.setTienKham(rs.getDouble("TienKham"));

                    // Thông tin bệnh nhân
                    Date ngaySinh = rs.getDate("NgaySinh");
                    if (ngaySinh != null) {
                        report.setNgaySinh(ngaySinh.toLocalDate());
                    }
                    report.setSoDienThoai(rs.getString("SoDienThoai"));
                    report.setGioiTinh(rs.getString("GioiTinh"));

                    // Thông tin ngày
                    Timestamp ngayKham = rs.getTimestamp("NgayKham");
                    if (ngayKham != null) {
                        report.setNgayKham(ngayKham.toLocalDateTime());
                    }
                    
                    // Ngày lập phiếu - sử dụng trường ngayLap trong MedicalReportModel
                    Timestamp ngayLapPhieu = rs.getTimestamp("NgayLapPhieu");
                    if (ngayLapPhieu != null) {
                        report.setNgayLap(ngayLapPhieu.toLocalDateTime());
                    }

                    // Tạo BillModel nếu có hóa đơn
                    String maHoaDon = rs.getString("MaHoaDon");
                    if (maHoaDon != null && !maHoaDon.isEmpty()) {
                        BillModel bill = new BillModel();
                        bill.setMaHoaDon(maHoaDon);
                        bill.setMaPhieuKham(rs.getString("MaPhieuKham"));
                        bill.setMaDonThuoc(rs.getString("MaDonThuoc"));
                        bill.setTienKham(rs.getDouble("TienKham"));
                        bill.setTongTien(rs.getDouble("TongTienHoaDon"));
                        bill.setTrangThai(rs.getString("TrangThai"));
                        
                        Timestamp ngayLapHoaDon = rs.getTimestamp("NgayLapHoaDon");
                        if (ngayLapHoaDon != null) {
                            bill.setNgayLapDon(ngayLapHoaDon.toLocalDateTime());
                        }

                        // Lấy danh sách thuốc từ đơn thuốc
                        String maDonThuoc = rs.getString("MaDonThuoc");
                        if (maDonThuoc != null && !maDonThuoc.isEmpty()) {
                            List<MedicineModel> danhSachThuoc = getMedicinesByDonThuoc(maDonThuoc);
                            bill.setDanhSachThuoc(danhSachThuoc);
                        }

                        report.setHoaDon(bill);
                    }

                    return report;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy phiếu khám bệnh đầy đủ: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Phương thức hỗ trợ: Lấy danh sách thuốc theo mã đơn thuốc
    private static List<MedicineModel> getMedicinesByDonThuoc(String maDonThuoc) {
        List<MedicineModel> medicines = new ArrayList<>();
        String sql = """
                SELECT t.MaThuoc, t.TenThuoc, t.CongDung, ct.SoLuong, 
                       ct.GiaTien, ct.HuongDanSuDung, t.DonVi
                FROM CTDonThuoc ct 
                JOIN Thuoc t ON ct.MaThuoc = t.MaThuoc 
                WHERE ct.MaDonThuoc = ?
            """;

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, maDonThuoc);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MedicineModel medicine = new MedicineModel();
                    medicine.setMaThuoc(rs.getString("MaThuoc"));
                    medicine.setTenThuoc(rs.getString("TenThuoc"));
                    medicine.setCongDung(rs.getString("CongDung"));
                    medicine.setSoLuong(rs.getInt("SoLuong"));
                    medicine.setGiaTien(rs.getDouble("GiaTien"));
                    medicine.setHuongDanSuDung(rs.getString("HuongDanSuDung"));
                    medicine.setDonVi(rs.getString("DonVi"));
                    
                    medicines.add(medicine);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách thuốc: " + e.getMessage());
        }
        return medicines;
    }
    public static int getNextIdNumber(String prefix) {
        String sql = "SELECT MaPhieuKham " +
                "FROM PhieuKhamBenh " +
                "WHERE MaPhieuKham LIKE ? " +
                "AND MaPhieuKham ~ ? " +
                "ORDER BY CAST(SUBSTRING(MaPhieuKham FROM ?) AS INTEGER) DESC " +
                "LIMIT 1";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, prefix + "%");
            stmt.setString(2, "^" + prefix + "[0-9]+$");
            stmt.setString(3, String.format("^.{%d}(\\d+)$", prefix.length()));

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String id = rs.getString("MaPhieuKham");
                if (id != null && id.startsWith(prefix)) {
                    try {
                        String numberPart = id.substring(prefix.length());
                        if (numberPart.matches("\\d+")) {
                            int currentNumber = Integer.parseInt(numberPart);
                            System.out.println("Found max MaPhieuKham: " + id + ", number: " + currentNumber);
                            return currentNumber + 1;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing number from: " + id);
                    }
                }
            }

            System.out.println("No existing MaPhieuKham found for prefix: " + prefix);
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error in getNextIdNumber (MedicalReportDAO): " + e.getMessage());
            return 1;
        }
    }

}