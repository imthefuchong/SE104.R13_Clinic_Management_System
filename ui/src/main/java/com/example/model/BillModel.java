package com.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BillModel {
    private String maHoaDon;
    private double tongTien;
    private double tienKham;
    private String trangThai;
    private String maDonThuoc;
    private LocalDateTime ngayLapDon;
    private List<MedicineModel> danhSachThuoc;
    private String maPhieuKham;

    public BillModel() {
    }

    public BillModel(String maHoaDon, double tongTien, double tienKham, String trangThai, String maDonThuoc,
                     LocalDateTime ngayLapDon, List<MedicineModel> danhSachThuoc, String maPhieuKham) {
        this.maHoaDon = maHoaDon;
        this.tongTien = tongTien;
        this.tienKham = tienKham;
        this.trangThai = trangThai;
        this.maDonThuoc = maDonThuoc;
        this.ngayLapDon = ngayLapDon;
        this.danhSachThuoc = danhSachThuoc;
        this.maPhieuKham = maPhieuKham;
    }



    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public void setMaDonThuoc(String maDonThuoc) {
        this.maDonThuoc = maDonThuoc;
    }

    public LocalDateTime getNgayLapDon() {
        return ngayLapDon;
    }

    public void setNgayLapDon(LocalDateTime ngayLapDon) {
        this.ngayLapDon = ngayLapDon;
    }

    public List<MedicineModel> getDanhSachThuoc() {
        return danhSachThuoc;
    }

    public void setDanhSachThuoc(List<MedicineModel> danhSachThuoc) {
        this.danhSachThuoc = danhSachThuoc;
    }

    public double getTienKham() {
        return tienKham;
    }

    public void setTienKham(double tienKham) {
        this.tienKham = tienKham;
    }

    public String getMaPhieuKham() {
        return maPhieuKham;
    }

    public void setMaPhieuKham(String maPhieuKham) {
        this.maPhieuKham = maPhieuKham;
    }
}
