package com.example.model;

import java.time.LocalDate;

public class DutyShiftModel {
    private String maLichTruc;   // ✅ Mới
    private String maBacSi;
    private String tenNguoiTruc;
    private Role vaiTro;
    private LocalDate ngay;
    private String caTruc;

    public DutyShiftModel(String maLichTruc, String maBacSi, String tenNguoiTruc, Role vaiTro, LocalDate ngay, String caTruc) {
        this.maLichTruc = maLichTruc;
        this.maBacSi = maBacSi;
        this.tenNguoiTruc = tenNguoiTruc;
        this.vaiTro = vaiTro;
        this.ngay = ngay;
        this.caTruc = caTruc;
    }


    public String getMaLichTruc() {
        return maLichTruc;
    }

    public void setMaLichTruc(String maLichTruc) {
        this.maLichTruc = maLichTruc;
    }

    public String getMaBacSi() {
        return maBacSi;
    }

    public void setMaBacSi(String maBacSi) {
        this.maBacSi = maBacSi;
    }

    public String getTenNguoiTruc() {
        return tenNguoiTruc;
    }

    public void setTenNguoiTruc(String tenNguoiTruc) {
        this.tenNguoiTruc = tenNguoiTruc;
    }

    public Role getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(Role vaiTro) {
        this.vaiTro = vaiTro;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public void setNgay(LocalDate ngay) {
        this.ngay = ngay;
    }

    public String getCaTruc() {
        return caTruc;
    }

    public void setCaTruc(String caTruc) {
        this.caTruc = caTruc;
    }
}
