package com.example.model;

import java.time.LocalDate;
import java.util.List;

public class MedicineModel {
    private String maThuoc;
    private String tenThuoc;
    private String congDung;
    private int soLuong;
    private double giaTien;
    private String donVi;
    private String huongDanSuDung;
    private String trangThai;

    public MedicineModel(String maThuoc, String tenThuoc, String congDung, int soLuong, double giaTien, String donVi, String huongDanSuDung) {
        this(maThuoc, tenThuoc, congDung, soLuong, giaTien, donVi, huongDanSuDung, "Đang sử dụng");
    }

    public MedicineModel(String maThuoc, String tenThuoc, String congDung, int soLuong, double giaTien, String donVi, String huongDanSuDung, String trangThai) {
        this.maThuoc = maThuoc;
        this.tenThuoc = tenThuoc;
        this.congDung = congDung;
        this.soLuong = soLuong;
        this.giaTien = giaTien;
        this.donVi = donVi;
        this.huongDanSuDung = huongDanSuDung;
        this.trangThai = trangThai == null || trangThai.isBlank() ? "Đang sử dụng" : trangThai;
    }

    public MedicineModel() {
        this.trangThai = "Đang sử dụng";
    }

    public String getHuongDanSuDung() {
        return huongDanSuDung;
    }

    public void setHuongDanSuDung(String huongDanSuDung) {
        this.huongDanSuDung = huongDanSuDung;
    }

    public double getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(double giaTien) {
        this.giaTien = giaTien;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public String getCongDung() {
        return congDung;
    }

    public void setCongDung(String congDung) {
        this.congDung = congDung;
    }

    public String getTenThuoc() {
        return tenThuoc;
    }

    public void setTenThuoc(String tenThuoc) {
        this.tenThuoc = tenThuoc;
    }

    public String getMaThuoc() {
        return maThuoc;
    }

    public void setMaThuoc(String maThuoc) {
        this.maThuoc = maThuoc;
    }

    public String getDonVi() { return donVi; }

    public void setDonVi(String donVi) { this.donVi = donVi; }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
