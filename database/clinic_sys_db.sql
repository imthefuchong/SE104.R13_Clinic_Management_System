CREATE DATABASE QuanLyPhongMach;
GO
USE QuanLyPhongMach;
GO

-- 1. NhanVien
CREATE TABLE NhanVien (
    MaNhanVien VARCHAR(10) PRIMARY KEY,
    HoTen NVARCHAR(50) NOT NULL,
    GioiTinh NVARCHAR(3) CHECK (GioiTinh IN (N'Nam', N'Nữ')),
    NgaySinh DATE NOT NULL,
    DiaChi NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(10) NOT NULL,
    Email VARCHAR(50) NOT NULL,
    ChucVu NVARCHAR(50) NOT NULL,
    TrangThai NVARCHAR(30) DEFAULT N'Đang làm việc'
);

-- 2. TaiKhoan
CREATE TABLE TaiKhoan (
    TenDangNhap VARCHAR(50) PRIMARY KEY,
    MatKhau VARCHAR(255) NOT NULL,
    MaXacNhan VARCHAR(10) NULL,
    TrangThai NVARCHAR(30) DEFAULT N'Hoạt động',
    MaVaiTro NVARCHAR(30) NOT NULL,
    MaNhanVien VARCHAR(10) UNIQUE FOREIGN KEY REFERENCES NhanVien(MaNhanVien)
);

-- 3. LichTruc
CREATE TABLE LichTruc (
    MaLich VARCHAR(10) PRIMARY KEY,
    NgayTruc DATE NOT NULL,
    CaTruc NVARCHAR(20) CHECK (CaTruc IN (N'Sáng', N'Chiều', N'Tối')),
    MaNV VARCHAR(10) FOREIGN KEY REFERENCES NhanVien(MaNhanVien)
);

-- 4. ThamSo
CREATE TABLE ThamSo (
    MaThamSo VARCHAR(50) PRIMARY KEY,
    TenThamSo NVARCHAR(100) NOT NULL,
    GiaTri DECIMAL(18,2) NOT NULL CHECK (GiaTri >= 0),
);

-- 5. LoaiBenh
CREATE TABLE LoaiBenh (
    MaLoaiBenh VARCHAR(10) PRIMARY KEY,
    TenLoaiBenh NVARCHAR(100) NOT NULL UNIQUE,
);

-- 6. DonViTinh
CREATE TABLE DonViTinh (
    MaDVT VARCHAR(10) PRIMARY KEY,
    TenDVT NVARCHAR(50) NOT NULL UNIQUE
);

-- 7. CachDung
CREATE TABLE CachDung (
    MaCD VARCHAR(10) PRIMARY KEY,
    TenCD NVARCHAR(100) NOT NULL UNIQUE
);

-- 8. Thuoc
CREATE TABLE Thuoc (
    MaThuoc VARCHAR(10) PRIMARY KEY,
    TenThuoc NVARCHAR(100) NOT NULL,
    DonGia DECIMAL(18,2) NOT NULL CHECK (DonGia > 0),
    SoLuongTon INT NOT NULL DEFAULT 0 CHECK (SoLuongTon >= 0),
    TrangThai NVARCHAR(30) DEFAULT N'Đang sử dụng',
    MaDVT VARCHAR(10) FOREIGN KEY REFERENCES DonViTinh(MaDonViTinh),
);

-- 9. BenhNhan
CREATE TABLE BenhNhan (
    MaBN VARCHAR(10) PRIMARY KEY,
    HoTen NVARCHAR(50) NOT NULL,
    GioiTinh NVARCHAR(3) CHECK (GioiTinh IN (N'Nam', N'Nữ')),
    NamSinh INT NOT NULL CHECK (NamSinh > 1900),
    DiaChi NVARCHAR(100) NOT NULL,
    SDT VARCHAR(10) NOT NULL
);

-- 10. DanhSachKham
CREATE TABLE DanhSachKham (
    MaDS VARCHAR(10) PRIMARY KEY,
    NgayKham DATE NOT NULL UNIQUE
);

-- 11. CT_DanhSachKham
CREATE TABLE CT_DanhSachKham (
    MaDS VARCHAR(10) FOREIGN KEY REFERENCES DanhSachKham(MaDanhSach),
    MaBN VARCHAR(10) FOREIGN KEY REFERENCES BenhNhan(MaBenhNhan),
    STT INT NOT NULL CHECK (STT > 0),
    TrangThai NVARCHAR(20) NOT NULL CHECK (TrangThai IN (N'Đang chờ', N'Đã khám')),
    PRIMARY KEY (MaDS, MaBN)
);

-- 12. PhieuKham
CREATE TABLE PhieuKham (
    MaPhieu VARCHAR(10) PRIMARY KEY,
    NgayKham DATE NOT NULL,
    TrieuChung NVARCHAR(255) NOT NULL,
    MaBN VARCHAR(10) FOREIGN KEY REFERENCES BenhNhan(MaBenhNhan),
    MaLoaiBenh VARCHAR(10) FOREIGN KEY REFERENCES LoaiBenh(MaLoaiBenh),
    MaBS VARCHAR(10) FOREIGN KEY REFERENCES NhanVien(MaNhanVien)
);

-- 13. DonThuoc
CREATE TABLE DonThuoc (
    MaDon VARCHAR(10) PRIMARY KEY,
    MaPhieu VARCHAR(10) UNIQUE FOREIGN KEY REFERENCES PhieuKham(MaPhieuKham)
);

-- 14. CT_DonThuoc
CREATE TABLE CT_DonThuoc (
    MaDon VARCHAR(10) FOREIGN KEY REFERENCES DonThuoc(MaDonThuoc),
    MaThuoc VARCHAR(10) FOREIGN KEY REFERENCES Thuoc(MaThuoc),
    SoLuong INT NOT NULL CHECK (SoLuong > 0),
    DonGiaBan DECIMAL(18,2) NOT NULL CHECK (DonGiaBan >= 0),
    MaCD VARCHAR(10) FOREIGN KEY REFERENCES CachDung(MaCachDung),
    PRIMARY KEY (MaDon, MaThuoc)
);

-- 15. HoaDon
CREATE TABLE HoaDon (
    MaHD VARCHAR(10) PRIMARY KEY,
    TienKham DECIMAL(18,2) NOT NULL DEFAULT 30000 CHECK (TienKham >= 0),
    TienThuoc DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (TienThuoc >= 0),
    TongTien DECIMAL(18,2) NOT NULL,
    MaPhieu VARCHAR(10) UNIQUE FOREIGN KEY REFERENCES PhieuKham(MaPhieuKham),
    NguoiLap VARCHAR(10) FOREIGN KEY REFERENCES NhanVien(MaNhanVien)
);

-- INSERT dữ liệu mẫu
INSERT INTO ThamSo (MaThamSo, TenThamSo, GiaTri, GhiChu) VALUES 
('SoBNToiDa', N'Số bệnh nhân tối đa mỗi ngày', 40, N'Quy định 1'),
('TienKham', N'Tiền khám bệnh', 30000, N'Quy định 4');

INSERT INTO NhanVien (MaNhanVien, HoTen, GioiTinh, NgaySinh, DiaChi, SoDienThoai, Email, ChucVu) 
VALUES ('NV001', N'Lê Trần Phú Trọng', N'Nam', '2000-01-01', N'TP.HCM', '0901234567', 'admin@phongmach.com', N'Quản trị viên');

INSERT INTO TaiKhoan (TenDangNhap, MatKhau, MaVaiTro, MaNhanVien) 
VALUES ('admin', '123456', N'Quản trị viên', 'NV001');
