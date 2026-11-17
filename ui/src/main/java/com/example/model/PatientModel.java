package com.example.model;

import java.time.LocalDate;

public class PatientModel {
    private String maBenhNhan;
    private String hoTen;
    private LocalDate ngaySinh;
    private String soDienThoai;
    private String gioiTinh;
    private String diaChi;

    public PatientModel(String maBenhNhan, String hoTen, LocalDate ngaySinh, String soDienThoai, String gioiTinh) {
        this(maBenhNhan, hoTen, ngaySinh, soDienThoai, gioiTinh, "");
    }

    public PatientModel(String maBenhNhan, String hoTen, LocalDate ngaySinh, String soDienThoai, String gioiTinh, String diaChi) {
        this.maBenhNhan = maBenhNhan;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.soDienThoai = soDienThoai;
        this.gioiTinh = gioiTinh;
        this.diaChi = diaChi;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        this.maBenhNhan = maBenhNhan;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}
