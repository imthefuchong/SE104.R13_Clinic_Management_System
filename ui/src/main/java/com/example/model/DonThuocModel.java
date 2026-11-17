package com.example.model;

import java.time.LocalDateTime;

public class DonThuocModel {
    private String maDonThuoc;
    private String maPhieuKham;
    private LocalDateTime ngayLapDon;

    public DonThuocModel()
    {

    }

    public DonThuocModel(String maDonThuoc, String maPhieuKham, LocalDateTime ngayLapDon) {
        this.maDonThuoc = maDonThuoc;
        this.maPhieuKham = maPhieuKham;
        this.ngayLapDon = ngayLapDon;
    }

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public void setMaDonThuoc(String maDonThuoc) {
        this.maDonThuoc = maDonThuoc;
    }

    public String getMaPhieuKham() {
        return maPhieuKham;
    }

    public void setMaPhieuKham(String maPhieuKham) {
        this.maPhieuKham = maPhieuKham;
    }

    public LocalDateTime getNgayLapDon() {
        return ngayLapDon;
    }

    public void setNgayLapDon(LocalDateTime ngayLapDon) {
        this.ngayLapDon = ngayLapDon;
    }
}
