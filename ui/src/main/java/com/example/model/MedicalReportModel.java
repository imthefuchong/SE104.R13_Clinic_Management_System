package com.example.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicalReportModel {
    private String maKhamBenh;
    private String maPhieuKham;
    private String maBenhNhan;
    private String maBacSi;
    private String hoTen;
    private String tenBacSi;
    private LocalDate ngaySinh;
    private String soDienThoai;
    private String gioiTinh;
    private String lyDoKham;
    private LocalDateTime ngayLap;
    private String chanDoan;
    private BillModel hoaDon;
    private String ketQuaKham;
    private String dieuTri;
    private Double tienKham;
    private LocalDate ngayKham;

    public MedicalReportModel() {
    }

    public MedicalReportModel(String maKhamBenh, String maPhieuKham, String maBenhNhan, String maBacSi, String hoTen, String tenBacSi, LocalDate ngaySinh, String soDienThoai, String gioiTinh, String lyDoKham, LocalDateTime ngayLap, String chanDoan, BillModel hoaDon) {
        this.maKhamBenh = maKhamBenh;
        this.maPhieuKham = maPhieuKham;
        this.maBenhNhan = maBenhNhan;
        this.maBacSi = maBacSi;
        this.hoTen = hoTen;
        this.tenBacSi = tenBacSi;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.gioiTinh = gioiTinh;
        this.lyDoKham = lyDoKham;
        this.ngayLap = ngayLap;
        this.chanDoan = chanDoan;
        this.hoaDon = hoaDon;
    }


    public String getMaPhieuKham() {
        return maPhieuKham;
    }

    public void setMaPhieuKham(String maPhieuKham) {
        this.maPhieuKham = maPhieuKham;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        this.maBenhNhan = maBenhNhan;
    }

    public String getMaBacSi() {
        return maBacSi;
    }

    public void setMaBacSi(String maBacSi) {
        this.maBacSi = maBacSi;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getTenBacSi() {
        return tenBacSi;
    }

    public void setTenBacSi(String tenBacSi) {
        this.tenBacSi = tenBacSi;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        this.lyDoKham = lyDoKham;
    }

    public LocalDateTime getNgayLap() {
        return ngayLap;
    }

    public void setNgayKham(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public void setNgayLap(LocalDateTime ngayLap) {
        this.ngayLap = ngayLap;
    }

    public String getChanDoan() {
        return chanDoan;
    }

    public void setChanDoan(String chanDoan) {
        this.chanDoan = chanDoan;
    }

    public BillModel getHoaDon() {
        return hoaDon;
    }

    public void setHoaDon(BillModel hoaDon) {
        this.hoaDon = hoaDon;
    }

    public String getMaKhamBenh() {
        return maKhamBenh;
    }

    public void setMaKhamBenh(String maKhamBenh) {
        this.maKhamBenh = maKhamBenh;
    }

    public String getKetQuaKham() {
        return ketQuaKham;
    }

    public void setKetQuaKham(String ketQuaKham) {
        this.ketQuaKham = ketQuaKham;
    }

    public String getDieuTri() {
        return dieuTri;
    }

    public void setDieuTri(String dieuTri) {
        this.dieuTri = dieuTri;
    }

    public Double getTienKham() {
        return tienKham;
    }

    public void setTienKham(Double tienKham) {
        this.tienKham = tienKham;
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDate ngayKham) {
        this.ngayKham = ngayKham;
    }
}