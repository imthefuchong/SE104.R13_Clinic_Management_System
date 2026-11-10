\xef\xbb\xbf/*==============================================================================
  HỆ THỐNG QUẢN LÝ PHÒNG MẠCH TƯ  (SE104 - R13)
  SQL Server - Script tổng hợp hoàn chỉnh

  Nguồn tổng hợp:
    - 23520714.sql : ThamSo, LoaiBenh, DonViTinh, CachDung, Thuoc
    - 24521143.sql : NhanVien, TaiKhoan, LichTruc
    - TranDong_BenhNhan_DanhSachKham.sql : BenhNhan, DanhSachKham, CT_DanhSachKham
    - 23521728.sql : PhieuKham, DonThuoc, CT_DonThuoc
    - TaiChinh_BaoCaoThongKe.sql : HoaDon, các bảng báo cáo
    - SE104_R13_tasks.docx : thiết kế dữ liệu (ERD) dùng để thống nhất tên cột/bảng

  Thứ tự chạy: Database -> Bảng -> View/Function -> Trigger -> Thủ tục -> Dữ liệu mẫu
==============================================================================*/

SET NOCOUNT ON;
GO

IF DB_ID(N'QuanLyPhongMach') IS NULL
    CREATE DATABASE QuanLyPhongMach;
GO
USE QuanLyPhongMach;
GO

SET XACT_ABORT ON;
GO

/*==============================================================================
  PHẦN 1: TẠO BẢNG
==============================================================================*/
BEGIN TRANSACTION;

/* ---------- 1.1 Danh mục & tham số (Bước 1, 2, 4, 6, 7) ---------- */

-- THAMSO: tham số hóa QĐ1 (số BN tối đa/ngày), QĐ4 (tiền khám)
CREATE TABLE dbo.ThamSo (
    MaThamSo    VARCHAR(20)    NOT NULL,
    TenThamSo   NVARCHAR(100)  NOT NULL,
    GiaTri      DECIMAL(18,2)  NOT NULL,
    GhiChu      NVARCHAR(255)  NULL,
    CONSTRAINT PK_ThamSo PRIMARY KEY (MaThamSo),
    CONSTRAINT UQ_ThamSo_Ten UNIQUE (TenThamSo),
    CONSTRAINT CK_ThamSo_Ma     CHECK (LEN(LTRIM(RTRIM(MaThamSo))) > 0),
    CONSTRAINT CK_ThamSo_Ten    CHECK (LEN(LTRIM(RTRIM(TenThamSo))) > 0),
    CONSTRAINT CK_ThamSo_GiaTri CHECK (GiaTri >= 0)
);

CREATE TABLE dbo.LoaiBenh (
    MaLoaiBenh          VARCHAR(10)    NOT NULL,
    TenLoaiBenh         NVARCHAR(100)  NOT NULL,
    TrieuChungDacTrung  NVARCHAR(255)  NULL,
    CONSTRAINT PK_LoaiBenh PRIMARY KEY (MaLoaiBenh),
    CONSTRAINT CK_LoaiBenh_Ma  CHECK (LEN(LTRIM(RTRIM(MaLoaiBenh))) > 0),
    CONSTRAINT CK_LoaiBenh_Ten CHECK (LEN(LTRIM(RTRIM(TenLoaiBenh))) > 0),
    CONSTRAINT UQ_LoaiBenh_Ten UNIQUE (TenLoaiBenh)
);

CREATE TABLE dbo.DonViTinh (
    MaDVT   VARCHAR(10)   NOT NULL,
    TenDVT  NVARCHAR(50)  NOT NULL,
    CONSTRAINT PK_DonViTinh PRIMARY KEY (MaDVT),
    CONSTRAINT CK_DonViTinh_Ma  CHECK (LEN(LTRIM(RTRIM(MaDVT))) > 0),
    CONSTRAINT CK_DonViTinh_Ten CHECK (LEN(LTRIM(RTRIM(TenDVT))) > 0),
    CONSTRAINT UQ_DonViTinh_Ten UNIQUE (TenDVT)
);

CREATE TABLE dbo.CachDung (
    MaCachDung   VARCHAR(10)    NOT NULL,
    TenCachDung  NVARCHAR(100)  NOT NULL,
    CONSTRAINT PK_CachDung PRIMARY KEY (MaCachDung),
    CONSTRAINT CK_CachDung_Ma  CHECK (LEN(LTRIM(RTRIM(MaCachDung))) > 0),
    CONSTRAINT CK_CachDung_Ten CHECK (LEN(LTRIM(RTRIM(TenCachDung))) > 0),
    CONSTRAINT UQ_CachDung_Ten UNIQUE (TenCachDung)
);

CREATE TABLE dbo.Thuoc (
    MaThuoc     VARCHAR(10)    NOT NULL,
    TenThuoc    NVARCHAR(100)  NOT NULL,
    MaDVT       VARCHAR(10)    NOT NULL,
    MaCachDung  VARCHAR(10)    NOT NULL,   -- cách dùng mặc định gợi ý khi kê đơn
    DonGia      DECIMAL(18,2)  NOT NULL,
    SoLuongTon  INT            NOT NULL CONSTRAINT DF_Thuoc_SoLuongTon DEFAULT 0,
    TrangThai   NVARCHAR(30)   NOT NULL CONSTRAINT DF_Thuoc_TrangThai DEFAULT N'Đang sử dụng',
    CongDung    NVARCHAR(255)  NULL,
    CONSTRAINT PK_Thuoc PRIMARY KEY (MaThuoc),
    CONSTRAINT CK_Thuoc_Ma         CHECK (LEN(LTRIM(RTRIM(MaThuoc))) > 0),
    CONSTRAINT CK_Thuoc_Ten        CHECK (LEN(LTRIM(RTRIM(TenThuoc))) > 0),
    CONSTRAINT CK_Thuoc_DonGia     CHECK (DonGia > 0),
    CONSTRAINT CK_Thuoc_SoLuongTon CHECK (SoLuongTon >= 0),
    CONSTRAINT CK_Thuoc_TrangThai  CHECK (TrangThai IN (N'Đang sử dụng', N'Ngưng sử dụng')),
    CONSTRAINT FK_Thuoc_DonViTinh  FOREIGN KEY (MaDVT)      REFERENCES dbo.DonViTinh(MaDVT),
    CONSTRAINT FK_Thuoc_CachDung   FOREIGN KEY (MaCachDung) REFERENCES dbo.CachDung(MaCachDung)
);

/* ---------- 1.2 Nhân viên, vai trò, tài khoản, lịch trực (Bước 8, 9) ---------- */

CREATE TABLE dbo.VaiTro (
    MaVaiTro   VARCHAR(10)   NOT NULL,
    TenVaiTro  NVARCHAR(30)  NOT NULL,
    CONSTRAINT PK_VaiTro PRIMARY KEY (MaVaiTro),
    CONSTRAINT CK_VaiTro_Ma  CHECK (LEN(LTRIM(RTRIM(MaVaiTro))) > 0),
    CONSTRAINT CK_VaiTro_Ten CHECK (LEN(LTRIM(RTRIM(TenVaiTro))) > 0),
    CONSTRAINT UQ_VaiTro_Ten UNIQUE (TenVaiTro)
);

CREATE TABLE dbo.NhanVien (
    MaNV         VARCHAR(10)    NOT NULL,
    HoTen        NVARCHAR(50)   NOT NULL,
    GioiTinh     NVARCHAR(3)    NOT NULL,
    NgaySinh     DATE           NOT NULL,
    DiaChi       NVARCHAR(100)  NOT NULL,
    SoDienThoai  VARCHAR(10)    NOT NULL,
    Email        VARCHAR(50)    NOT NULL,
    ChucVu       NVARCHAR(50)   NOT NULL,
    TrangThai    NVARCHAR(30)   NOT NULL CONSTRAINT DF_NhanVien_TrangThai DEFAULT N'Đang làm việc',
    CONSTRAINT PK_NhanVien PRIMARY KEY (MaNV),
    CONSTRAINT CK_NhanVien_HoTen       CHECK (LEN(LTRIM(RTRIM(HoTen))) > 0),
    CONSTRAINT CK_NhanVien_GioiTinh    CHECK (GioiTinh IN (N'Nam', N'Nữ')),
    CONSTRAINT CK_NhanVien_NgaySinh    CHECK (NgaySinh < CAST(GETDATE() AS DATE)),
    CONSTRAINT CK_NhanVien_SoDienThoai CHECK (LEN(SoDienThoai) = 10 AND SoDienThoai NOT LIKE '%[^0-9]%'),
    CONSTRAINT CK_NhanVien_Email       CHECK (Email LIKE '%_@__%.__%'),
    CONSTRAINT CK_NhanVien_ChucVu      CHECK (ChucVu IN (N'Bác sĩ', N'Y tá', N'Quản lý', N'Quản trị viên')),
    CONSTRAINT CK_NhanVien_TrangThai   CHECK (TrangThai IN (N'Đang làm việc', N'Nghỉ việc'))
);

CREATE TABLE dbo.TaiKhoan (
    MaTK          VARCHAR(10)   NOT NULL,
    MaNV          VARCHAR(10)   NOT NULL,
    MaVaiTro      VARCHAR(10)   NOT NULL,
    TenDangNhap   VARCHAR(50)   NOT NULL,
    MatKhau       VARCHAR(255)  NOT NULL,
    TrangThai     NVARCHAR(30)  NOT NULL CONSTRAINT DF_TaiKhoan_TrangThai DEFAULT N'Hoạt động',
    MaXacNhan     VARCHAR(10)   NULL,
    HanMaXacNhan  DATETIME      NULL,
    CONSTRAINT PK_TaiKhoan PRIMARY KEY (MaTK),
    CONSTRAINT UQ_TaiKhoan_TenDangNhap UNIQUE (TenDangNhap),
    CONSTRAINT UQ_TaiKhoan_MaNV UNIQUE (MaNV),            -- mỗi nhân viên chỉ có 1 tài khoản
    CONSTRAINT CK_TaiKhoan_TenDangNhap CHECK (LEN(LTRIM(RTRIM(TenDangNhap))) > 0),
    CONSTRAINT CK_TaiKhoan_TrangThai   CHECK (TrangThai IN (N'Hoạt động', N'Khóa')),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (MaNV)     REFERENCES dbo.NhanVien(MaNV),
    CONSTRAINT FK_TaiKhoan_VaiTro   FOREIGN KEY (MaVaiTro) REFERENCES dbo.VaiTro(MaVaiTro)
);

-- CaTruc lưu chuỗi tự do (Sáng, Chiều, ...) theo thiết kế Bước 8b để linh hoạt đổi khung giờ
CREATE TABLE dbo.LichTruc (
    MaLT      VARCHAR(10)    NOT NULL,
    MaNV      VARCHAR(10)    NOT NULL,
    NgayTruc  DATE           NOT NULL,
    CaTruc    NVARCHAR(20)   NOT NULL,
    GhiChu    NVARCHAR(255)  NULL,
    CONSTRAINT PK_LichTruc PRIMARY KEY (MaLT),
    CONSTRAINT CK_LichTruc_CaTruc CHECK (LEN(LTRIM(RTRIM(CaTruc))) > 0),
    CONSTRAINT UQ_LichTruc_NhanVien_Ngay_Ca UNIQUE (MaNV, NgayTruc, CaTruc),  -- không trùng lịch trực
    CONSTRAINT FK_LichTruc_NhanVien FOREIGN KEY (MaNV) REFERENCES dbo.NhanVien(MaNV)
);

/* ---------- 1.3 Bệnh nhân & danh sách khám (Bước 1, 3) ---------- */

CREATE TABLE dbo.BenhNhan (
    MaBN         VARCHAR(10)    NOT NULL,
    HoTen        NVARCHAR(50)   NOT NULL,
    GioiTinh     NVARCHAR(3)    NOT NULL,
    NamSinh      INT            NOT NULL,
    DiaChi       NVARCHAR(100)  NOT NULL,
    SoDienThoai  VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_BenhNhan PRIMARY KEY (MaBN),
    CONSTRAINT CK_BenhNhan_HoTen       CHECK (LEN(LTRIM(RTRIM(HoTen))) > 0),
    CONSTRAINT CK_BenhNhan_GioiTinh    CHECK (GioiTinh IN (N'Nam', N'Nữ')),
    CONSTRAINT CK_BenhNhan_NamSinh     CHECK (NamSinh > 1900 AND NamSinh <= YEAR(GETDATE())),
    CONSTRAINT CK_BenhNhan_DiaChi      CHECK (LEN(LTRIM(RTRIM(DiaChi))) > 0),
    CONSTRAINT CK_BenhNhan_SoDienThoai CHECK (LEN(SoDienThoai) = 10 AND SoDienThoai NOT LIKE '%[^0-9]%')
);

CREATE TABLE dbo.DanhSachKham (
    MaDSK     VARCHAR(10)  NOT NULL,
    NgayKham  DATE         NOT NULL,
    CONSTRAINT PK_DanhSachKham PRIMARY KEY (MaDSK),
    CONSTRAINT UQ_DanhSachKham_NgayKham UNIQUE (NgayKham)
);

CREATE TABLE dbo.CT_DanhSachKham (
    MaDSK      VARCHAR(10)   NOT NULL,
    MaBN       VARCHAR(10)   NOT NULL,
    STT        INT           NOT NULL,
    TrangThai  NVARCHAR(20)  NOT NULL CONSTRAINT DF_CTDSK_TrangThai DEFAULT N'Đang chờ',
    CONSTRAINT PK_CT_DanhSachKham PRIMARY KEY (MaDSK, MaBN),
    CONSTRAINT UQ_CTDSK_STT UNIQUE (MaDSK, STT),
    CONSTRAINT CK_CTDSK_STT       CHECK (STT > 0),
    CONSTRAINT CK_CTDSK_TrangThai CHECK (TrangThai IN (N'Đang chờ', N'Đã khám')),
    CONSTRAINT FK_CTDSK_DanhSachKham FOREIGN KEY (MaDSK) REFERENCES dbo.DanhSachKham(MaDSK),
    CONSTRAINT FK_CTDSK_BenhNhan     FOREIGN KEY (MaBN)  REFERENCES dbo.BenhNhan(MaBN)
);

/* ---------- 1.4 Phiếu khám, đơn thuốc (Bước 2) ---------- */

CREATE TABLE dbo.PhieuKham (
    MaPK          VARCHAR(10)   NOT NULL,
    NgayKham      DATE          NOT NULL,
    TrieuChung    NVARCHAR(255) NOT NULL,
    MaBN          VARCHAR(10)   NOT NULL,
    MaLoaiBenh    VARCHAR(10)   NOT NULL,
    MaBS          VARCHAR(10)   NOT NULL,
    DaThanhToan   BIT           NOT NULL CONSTRAINT DF_PhieuKham_DaThanhToan DEFAULT 0,
    CONSTRAINT PK_PhieuKham PRIMARY KEY (MaPK),
    CONSTRAINT CK_PhieuKham_Ma         CHECK (LEN(LTRIM(RTRIM(MaPK))) > 0),
    CONSTRAINT CK_PhieuKham_TrieuChung CHECK (LEN(LTRIM(RTRIM(TrieuChung))) > 0),
    CONSTRAINT FK_PhieuKham_BenhNhan FOREIGN KEY (MaBN)       REFERENCES dbo.BenhNhan(MaBN),
    CONSTRAINT FK_PhieuKham_LoaiBenh FOREIGN KEY (MaLoaiBenh) REFERENCES dbo.LoaiBenh(MaLoaiBenh),
    CONSTRAINT FK_PhieuKham_BacSi    FOREIGN KEY (MaBS)       REFERENCES dbo.NhanVien(MaNV)
);

CREATE TABLE dbo.DonThuoc (
    MaDT    VARCHAR(10)  NOT NULL,
    MaPK    VARCHAR(10)  NOT NULL,
    NgayKe  DATE         NOT NULL CONSTRAINT DF_DonThuoc_NgayKe DEFAULT (CAST(GETDATE() AS DATE)),
    CONSTRAINT PK_DonThuoc PRIMARY KEY (MaDT),
    CONSTRAINT CK_DonThuoc_Ma CHECK (LEN(LTRIM(RTRIM(MaDT))) > 0),
    CONSTRAINT UQ_DonThuoc_MaPK UNIQUE (MaPK),            -- 1 phiếu khám có tối đa 1 đơn thuốc
    CONSTRAINT FK_DonThuoc_PhieuKham FOREIGN KEY (MaPK) REFERENCES dbo.PhieuKham(MaPK)
);

CREATE TABLE dbo.CT_DonThuoc (
    MaDT        VARCHAR(10)    NOT NULL,
    MaThuoc     VARCHAR(10)    NOT NULL,
    SoLuong     INT            NOT NULL,
    DonGia      DECIMAL(18,2)  NOT NULL,   -- giá tại thời điểm kê đơn (QĐ4: hóa đơn cũ giữ nguyên giá)
    MaCachDung  VARCHAR(10)    NOT NULL,
    CONSTRAINT PK_CT_DonThuoc PRIMARY KEY (MaDT, MaThuoc),
    CONSTRAINT CK_CT_DonThuoc_SoLuong CHECK (SoLuong > 0),
    CONSTRAINT CK_CT_DonThuoc_DonGia  CHECK (DonGia >= 0),
    CONSTRAINT FK_CT_DonThuoc_Don      FOREIGN KEY (MaDT)       REFERENCES dbo.DonThuoc(MaDT),
    CONSTRAINT FK_CT_DonThuoc_Thuoc    FOREIGN KEY (MaThuoc)    REFERENCES dbo.Thuoc(MaThuoc),
    CONSTRAINT FK_CT_DonThuoc_CachDung FOREIGN KEY (MaCachDung) REFERENCES dbo.CachDung(MaCachDung)
);

/* ---------- 1.5 Hóa đơn (Bước 4) ---------- */

CREATE TABLE dbo.HoaDon (
    MaHD       VARCHAR(10)    NOT NULL,
    MaPK       VARCHAR(10)    NOT NULL,
    NguoiLap   VARCHAR(10)    NOT NULL,   -- y tá thu tiền
    NgayLap    DATE           NOT NULL CONSTRAINT DF_HoaDon_NgayLap DEFAULT (CAST(GETDATE() AS DATE)),
    TienKham   DECIMAL(18,2)  NOT NULL,   -- sao chép từ ThamSo tại thời điểm lập
    TienThuoc  DECIMAL(18,2)  NOT NULL CONSTRAINT DF_HoaDon_TienThuoc DEFAULT 0,
    TongTien   DECIMAL(18,2)  NOT NULL,
    CONSTRAINT PK_HoaDon PRIMARY KEY (MaHD),
    CONSTRAINT UQ_HoaDon_MaPK UNIQUE (MaPK),              -- 1 phiếu khám chỉ thanh toán 1 lần
    CONSTRAINT CK_HoaDon_Ma       CHECK (LEN(LTRIM(RTRIM(MaHD))) > 0),
    CONSTRAINT CK_HoaDon_TienKham  CHECK (TienKham >= 0),
    CONSTRAINT CK_HoaDon_TienThuoc CHECK (TienThuoc >= 0),
    CONSTRAINT CK_HoaDon_TongTien  CHECK (TongTien >= 0 AND TongTien = TienKham + TienThuoc),
    CONSTRAINT FK_HoaDon_PhieuKham FOREIGN KEY (MaPK)     REFERENCES dbo.PhieuKham(MaPK),
    CONSTRAINT FK_HoaDon_NhanVien  FOREIGN KEY (NguoiLap) REFERENCES dbo.NhanVien(MaNV)
);

/* ---------- 1.6 Báo cáo tháng (Bước 5) ---------- */

CREATE TABLE dbo.BaoCaoDoanhThu (
    MaBCDT         VARCHAR(10)    NOT NULL,
    Thang          INT            NOT NULL,
    Nam            INT            NOT NULL,
    TongDoanhThu   DECIMAL(18,2)  NOT NULL CONSTRAINT DF_BCDT_Tong DEFAULT 0,
    CONSTRAINT PK_BaoCaoDoanhThu PRIMARY KEY (MaBCDT),
    CONSTRAINT UQ_BaoCaoDoanhThu_ThangNam UNIQUE (Thang, Nam),
    CONSTRAINT CK_BCDT_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CK_BCDT_Nam   CHECK (Nam > 2000),
    CONSTRAINT CK_BCDT_Tong  CHECK (TongDoanhThu >= 0)
);

CREATE TABLE dbo.CT_BaoCaoDoanhThu (
    MaBCDT    VARCHAR(10)    NOT NULL,
    Ngay      DATE           NOT NULL,
    SoBN      INT            NOT NULL,
    DoanhThu  DECIMAL(18,2)  NOT NULL,
    TyLe      DECIMAL(5,2)   NOT NULL,   -- % đóng góp so với doanh thu cả tháng
    CONSTRAINT PK_CT_BaoCaoDoanhThu PRIMARY KEY (MaBCDT, Ngay),
    CONSTRAINT CK_CTBCDT_SoBN     CHECK (SoBN >= 0),
    CONSTRAINT CK_CTBCDT_DoanhThu CHECK (DoanhThu >= 0),
    CONSTRAINT CK_CTBCDT_TyLe     CHECK (TyLe >= 0 AND TyLe <= 100),
    CONSTRAINT FK_CTBCDT_BCDT FOREIGN KEY (MaBCDT) REFERENCES dbo.BaoCaoDoanhThu(MaBCDT)
);

CREATE TABLE dbo.BaoCaoSDThuoc (
    MaBCSDT  VARCHAR(10)  NOT NULL,
    Thang    INT          NOT NULL,
    Nam      INT          NOT NULL,
    CONSTRAINT PK_BaoCaoSDThuoc PRIMARY KEY (MaBCSDT),
    CONSTRAINT UQ_BaoCaoSDThuoc_ThangNam UNIQUE (Thang, Nam),
    CONSTRAINT CK_BCSDT_Thang CHECK (Thang BETWEEN 1 AND 12),
    CONSTRAINT CK_BCSDT_Nam   CHECK (Nam > 2000)
);

CREATE TABLE dbo.CT_BaoCaoSDThuoc (
    MaBCSDT    VARCHAR(10)  NOT NULL,
    MaThuoc    VARCHAR(10)  NOT NULL,
    MaDVT      VARCHAR(10)  NOT NULL,
    SoLuong    INT          NOT NULL,   -- tổng lượng thuốc đã xuất trong tháng
    SoLanDung  INT          NOT NULL,   -- số đơn thuốc có kê thuốc này
    CONSTRAINT PK_CT_BaoCaoSDThuoc PRIMARY KEY (MaBCSDT, MaThuoc),
    CONSTRAINT CK_CTBCSDT_SoLuong   CHECK (SoLuong >= 0),
    CONSTRAINT CK_CTBCSDT_SoLanDung CHECK (SoLanDung >= 0),
    CONSTRAINT FK_CTBCSDT_BCSDT  FOREIGN KEY (MaBCSDT) REFERENCES dbo.BaoCaoSDThuoc(MaBCSDT),
    CONSTRAINT FK_CTBCSDT_Thuoc  FOREIGN KEY (MaThuoc) REFERENCES dbo.Thuoc(MaThuoc),
    CONSTRAINT FK_CTBCSDT_DVT    FOREIGN KEY (MaDVT)   REFERENCES dbo.DonViTinh(MaDVT)
);

/* ---------- 1.7 Index hỗ trợ truy vấn ---------- */
CREATE NONCLUSTERED INDEX IX_LoaiBenh_Ten      ON dbo.LoaiBenh (TenLoaiBenh);
CREATE NONCLUSTERED INDEX IX_Thuoc_Ten         ON dbo.Thuoc (TenThuoc);
CREATE NONCLUSTERED INDEX IX_Thuoc_MaDVT       ON dbo.Thuoc (MaDVT);
CREATE NONCLUSTERED INDEX IX_Thuoc_MaCachDung  ON dbo.Thuoc (MaCachDung);
CREATE NONCLUSTERED INDEX IX_NhanVien_HoTen    ON dbo.NhanVien (HoTen);
CREATE NONCLUSTERED INDEX IX_LichTruc_NgayTruc ON dbo.LichTruc (NgayTruc);
CREATE NONCLUSTERED INDEX IX_BenhNhan_HoTen    ON dbo.BenhNhan (HoTen);
CREATE NONCLUSTERED INDEX IX_CTDSK_MaBN        ON dbo.CT_DanhSachKham (MaBN);
CREATE NONCLUSTERED INDEX IX_PhieuKham_MaBN_NgayKham ON dbo.PhieuKham (MaBN, NgayKham);
CREATE NONCLUSTERED INDEX IX_PhieuKham_MaLoaiBenh    ON dbo.PhieuKham (MaLoaiBenh);
CREATE NONCLUSTERED INDEX IX_PhieuKham_MaBS          ON dbo.PhieuKham (MaBS);
CREATE NONCLUSTERED INDEX IX_CT_DonThuoc_MaThuoc     ON dbo.CT_DonThuoc (MaThuoc);
CREATE NONCLUSTERED INDEX IX_CT_DonThuoc_MaCachDung  ON dbo.CT_DonThuoc (MaCachDung);
CREATE NONCLUSTERED INDEX IX_HoaDon_NgayLap          ON dbo.HoaDon (NgayLap);
CREATE NONCLUSTERED INDEX IX_HoaDon_NguoiLap         ON dbo.HoaDon (NguoiLap);

COMMIT;
GO

/*==============================================================================
  PHẦN 2: FUNCTION & VIEW
==============================================================================*/

-- Số bệnh nhân tối đa mỗi ngày (QĐ1), đọc từ THAMSO, mặc định 40
CREATE FUNCTION dbo.fn_SoBenhNhanToiDa ()
RETURNS INT
AS
BEGIN
    DECLARE @Max INT;
    SELECT @Max = CAST(GiaTri AS INT) FROM dbo.ThamSo WHERE MaThamSo = 'SoBNToiDa';
    RETURN ISNULL(@Max, 40);
END;
GO

-- BM1: Danh sách khám bệnh theo ngày
CREATE VIEW dbo.v_DanhSachKhamBenh
AS
SELECT  ds.NgayKham,
        ct.STT,
        bn.MaBN,
        bn.HoTen,
        bn.GioiTinh,
        bn.NamSinh,
        bn.DiaChi,
        ct.TrangThai
FROM    dbo.DanhSachKham ds
JOIN    dbo.CT_DanhSachKham ct ON ct.MaDSK = ds.MaDSK
JOIN    dbo.BenhNhan bn        ON bn.MaBN  = ct.MaBN;
GO

-- BM3: Tra cứu bệnh nhân (Họ tên, Ngày khám, Loại bệnh, Triệu chứng)
CREATE VIEW dbo.v_TraCuuBenhNhan
AS
SELECT  bn.MaBN,
        bn.HoTen,
        pk.MaPK,
        pk.NgayKham,
        lb.TenLoaiBenh,
        pk.TrieuChung
FROM    dbo.BenhNhan bn
LEFT JOIN dbo.PhieuKham pk ON pk.MaBN = bn.MaBN
LEFT JOIN dbo.LoaiBenh  lb ON lb.MaLoaiBenh = pk.MaLoaiBenh;
GO

-- Tổng tiền thuốc của từng đơn
CREATE VIEW dbo.vw_TongTienDonThuoc
AS
SELECT  d.MaDT,
        d.MaPK,
        COALESCE(SUM(c.SoLuong * c.DonGia), 0) AS TongTienThuoc
FROM    dbo.DonThuoc d
LEFT JOIN dbo.CT_DonThuoc c ON c.MaDT = d.MaDT
GROUP BY d.MaDT, d.MaPK;
GO

/*==============================================================================
  PHẦN 3: TRIGGER
==============================================================================*/

-- QĐ1: mỗi ngày khám tối đa N bệnh nhân (N lấy từ THAMSO)
CREATE TRIGGER dbo.trg_CTDSK_KiemTraQD1
ON dbo.CT_DanhSachKham
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT ct.MaDSK
        FROM dbo.CT_DanhSachKham ct
        WHERE ct.MaDSK IN (SELECT MaDSK FROM inserted)
        GROUP BY ct.MaDSK
        HAVING COUNT(*) > dbo.fn_SoBenhNhanToiDa()
    )
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50060, N'Đã đủ số bệnh nhân khám trong ngày (QĐ1).', 1;
    END
END;
GO

-- Người lập phiếu khám phải là nhân viên có chức vụ Bác sĩ
CREATE TRIGGER dbo.trg_PhieuKham_KiemTraBacSi
ON dbo.PhieuKham
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.NhanVien n ON n.MaNV = i.MaBS
        WHERE n.ChucVu <> N'Bác sĩ'
    )
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50030, N'Người khám bệnh phải là nhân viên có chức vụ Bác sĩ.', 1;
    END
END;
GO

-- Người lập hóa đơn phải là nhân viên có chức vụ Y tá
CREATE TRIGGER dbo.trg_HoaDon_KiemTraNguoiLap
ON dbo.HoaDon
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    IF EXISTS (
        SELECT 1
        FROM inserted i
        JOIN dbo.NhanVien n ON n.MaNV = i.NguoiLap
        WHERE n.ChucVu <> N'Y tá'
    )
    BEGIN
        ROLLBACK TRANSACTION;
        THROW 50045, N'Người lập hóa đơn phải là nhân viên có chức vụ Y tá.', 1;
    END
END;
GO

/*==============================================================================
  PHẦN 4: THỦ TỤC
==============================================================================*/

/* ---------- 4.1 UC01 - Thay đổi quy định (QĐ6) ---------- */

CREATE PROCEDURE dbo.sp_CapNhatThamSo
    @MaThamSo   VARCHAR(20),
    @GiaTriMoi  DECIMAL(18,2)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @GiaTriMoi IS NULL OR @GiaTriMoi < 0
        THROW 50020, N'Giá trị tham số không được âm.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.ThamSo WHERE MaThamSo = @MaThamSo)
        THROW 50021, N'Tham số quy định không tồn tại.', 1;

    IF @MaThamSo = 'SoBNToiDa' AND @GiaTriMoi < 1
        THROW 50024, N'Số bệnh nhân tối đa mỗi ngày phải lớn hơn 0.', 1;

    UPDATE dbo.ThamSo
    SET GiaTri = @GiaTriMoi
    WHERE MaThamSo = @MaThamSo;
END;
GO

CREATE PROCEDURE dbo.sp_CapNhatDonGiaThuoc
    @MaThuoc    VARCHAR(10),
    @DonGiaMoi  DECIMAL(18,2)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @DonGiaMoi IS NULL OR @DonGiaMoi <= 0
        THROW 50022, N'Đơn giá thuốc phải lớn hơn 0.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.Thuoc WHERE MaThuoc = @MaThuoc)
        THROW 50023, N'Thuốc không tồn tại trong danh mục.', 1;

    -- Chỉ đổi giá danh mục; CT_DonThuoc đã lưu giá cũ nên đơn/hóa đơn cũ không bị ảnh hưởng
    UPDATE dbo.Thuoc
    SET DonGia = @DonGiaMoi
    WHERE MaThuoc = @MaThuoc;
END;
GO

/* ---------- 4.2 UC03 - Nhân viên ---------- */

CREATE PROCEDURE dbo.sp_ThemNhanVien
    @HoTen        NVARCHAR(50),
    @GioiTinh     NVARCHAR(3),
    @NgaySinh     DATE,
    @DiaChi       NVARCHAR(100),
    @SoDienThoai  VARCHAR(10),
    @Email        VARCHAR(50),
    @ChucVu       NVARCHAR(50),
    @MaNV         VARCHAR(10) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
        DECLARE @So INT;

        SELECT @So = ISNULL(MAX(CAST(SUBSTRING(MaNV, 3, 8) AS INT)), 0) + 1
        FROM dbo.NhanVien WITH (UPDLOCK, HOLDLOCK)
        WHERE MaNV LIKE 'NV%';

        SET @MaNV = 'NV' + RIGHT('000000' + CAST(@So AS VARCHAR(8)), 6);

        INSERT INTO dbo.NhanVien (MaNV, HoTen, GioiTinh, NgaySinh, DiaChi, SoDienThoai, Email, ChucVu)
        VALUES (@MaNV, LTRIM(RTRIM(@HoTen)), @GioiTinh, @NgaySinh, @DiaChi, @SoDienThoai, @Email, @ChucVu);
    COMMIT TRANSACTION;
END;
GO

/* ---------- 4.3 UC06, UC08 - Bệnh nhân & danh sách khám ---------- */

CREATE PROCEDURE dbo.sp_ThemBenhNhan
    @HoTen        NVARCHAR(50),
    @GioiTinh     NVARCHAR(3),
    @NamSinh      INT,
    @DiaChi       NVARCHAR(100),
    @SoDienThoai  VARCHAR(10),
    @MaBN         VARCHAR(10) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRANSACTION;
        DECLARE @So INT;

        SELECT @So = ISNULL(MAX(CAST(SUBSTRING(MaBN, 3, 8) AS INT)), 0) + 1
        FROM dbo.BenhNhan WITH (UPDLOCK, HOLDLOCK)
        WHERE MaBN LIKE 'BN%';

        SET @MaBN = 'BN' + RIGHT('000000' + CAST(@So AS VARCHAR(8)), 6);

        INSERT INTO dbo.BenhNhan (MaBN, HoTen, GioiTinh, NamSinh, DiaChi, SoDienThoai)
        VALUES (@MaBN, LTRIM(RTRIM(@HoTen)), @GioiTinh, @NamSinh, @DiaChi, @SoDienThoai);
    COMMIT TRANSACTION;
END;
GO

CREATE PROCEDURE dbo.sp_TimBenhNhanTheoTen
    @HoTen NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT MaBN, HoTen, GioiTinh, NamSinh, DiaChi, SoDienThoai
    FROM   dbo.BenhNhan
    WHERE  HoTen LIKE N'%' + LTRIM(RTRIM(@HoTen)) + N'%'
    ORDER BY HoTen;
END;
GO

CREATE PROCEDURE dbo.sp_ThemVaoDanhSachKham
    @NgayKham  DATE,
    @MaBN      VARCHAR(10),
    @STT       INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @NgayKham < CAST(GETDATE() AS DATE)
        THROW 50061, N'Không thể thêm bệnh nhân vào danh sách của ngày đã qua.', 1;

    IF NOT EXISTS (SELECT 1 FROM dbo.BenhNhan WHERE MaBN = @MaBN)
        THROW 50062, N'Bệnh nhân không tồn tại.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @MaDSK VARCHAR(10);

        SELECT @MaDSK = MaDSK
        FROM dbo.DanhSachKham WITH (UPDLOCK, HOLDLOCK)
        WHERE NgayKham = @NgayKham;

        IF @MaDSK IS NULL
        BEGIN
            SET @MaDSK = 'DS' + CONVERT(VARCHAR(8), @NgayKham, 112);
            INSERT INTO dbo.DanhSachKham (MaDSK, NgayKham) VALUES (@MaDSK, @NgayKham);
        END

        IF EXISTS (SELECT 1 FROM dbo.CT_DanhSachKham WHERE MaDSK = @MaDSK AND MaBN = @MaBN)
            THROW 50063, N'Bệnh nhân đã có trong danh sách khám của ngày.', 1;

        IF (SELECT COUNT(*) FROM dbo.CT_DanhSachKham WHERE MaDSK = @MaDSK) >= dbo.fn_SoBenhNhanToiDa()
            THROW 50064, N'Đã đủ số bệnh nhân khám trong ngày (QĐ1).', 1;

        SELECT @STT = ISNULL(MAX(STT), 0) + 1
        FROM dbo.CT_DanhSachKham
        WHERE MaDSK = @MaDSK;

        INSERT INTO dbo.CT_DanhSachKham (MaDSK, MaBN, STT, TrangThai)
        VALUES (@MaDSK, @MaBN, @STT, N'Đang chờ');

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

CREATE PROCEDURE dbo.sp_XoaKhoiDanhSachKham
    @NgayKham  DATE,
    @MaBN      VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @MaDSK VARCHAR(10);

        SELECT @MaDSK = MaDSK
        FROM dbo.DanhSachKham WITH (UPDLOCK, HOLDLOCK)
        WHERE NgayKham = @NgayKham;

        IF NOT EXISTS (SELECT 1 FROM dbo.CT_DanhSachKham
                       WHERE MaDSK = @MaDSK AND MaBN = @MaBN AND TrangThai = N'Đang chờ')
            THROW 50065, N'Chỉ xóa được bệnh nhân đang chờ trong danh sách của ngày.', 1;

        DELETE FROM dbo.CT_DanhSachKham
        WHERE MaDSK = @MaDSK AND MaBN = @MaBN;

        -- Đánh lại STT liên tục. Làm 2 bước để không vi phạm UNIQUE (MaDSK, STT) giữa chừng.
        UPDATE dbo.CT_DanhSachKham
        SET STT = STT + 100000
        WHERE MaDSK = @MaDSK;

        ;WITH STTMoi AS (
            SELECT STT, ROW_NUMBER() OVER (ORDER BY STT) AS STTNew
            FROM dbo.CT_DanhSachKham
            WHERE MaDSK = @MaDSK
        )
        UPDATE STTMoi SET STT = STTNew;

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

/* ---------- 4.4 UC11 - Kê đơn: thêm thuốc vào đơn (kiểm tra & trừ tồn kho) ---------- */

CREATE PROCEDURE dbo.usp_ThemThuocVaoDon
    @MaDT        VARCHAR(10),
    @MaThuoc     VARCHAR(10),
    @SoLuong     INT,
    @MaCachDung  VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @SoLuong IS NULL OR @SoLuong <= 0
        THROW 50010, N'Số lượng thuốc phải lớn hơn 0.', 1;

    IF @@TRANCOUNT > 0
        THROW 50011, N'Hãy gọi thủ tục này ngoài giao dịch đang mở.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @Gia DECIMAL(18,2);
        DECLARE @TrangThai NVARCHAR(30);
        DECLARE @SoLuongTon INT;
        DECLARE @DaThanhToan BIT;

        -- Đơn phải tồn tại và phiếu khám chưa thanh toán
        SELECT @DaThanhToan = pk.DaThanhToan
        FROM dbo.DonThuoc dt
        JOIN dbo.PhieuKham pk ON pk.MaPK = dt.MaPK
        WHERE dt.MaDT = @MaDT;

        IF @DaThanhToan IS NULL
            THROW 50014, N'Đơn thuốc không tồn tại.', 1;
        IF @DaThanhToan = 1
            THROW 50015, N'Phiếu khám đã thanh toán, không thể sửa đơn thuốc.', 1;

        -- Lấy giá hiện hành + tồn kho, khóa dòng chống tương tranh
        SELECT @Gia = DonGia, @TrangThai = TrangThai, @SoLuongTon = SoLuongTon
        FROM dbo.Thuoc WITH (UPDLOCK, HOLDLOCK)
        WHERE MaThuoc = @MaThuoc;

        IF @Gia IS NULL OR @TrangThai <> N'Đang sử dụng'
            THROW 50012, N'Thuốc không tồn tại hoặc đã ngừng sử dụng.', 1;

        IF @SoLuongTon < @SoLuong
            THROW 50013, N'Số lượng thuốc trong kho không đủ để kê đơn.', 1;

        INSERT dbo.CT_DonThuoc (MaDT, MaThuoc, SoLuong, DonGia, MaCachDung)
        VALUES (@MaDT, @MaThuoc, @SoLuong, @Gia, @MaCachDung);

        UPDATE dbo.Thuoc
        SET SoLuongTon = SoLuongTon - @SoLuong
        WHERE MaThuoc = @MaThuoc;

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

/* ---------- 4.5 UC09 - Lập hóa đơn thanh toán (QĐ4) ---------- */

CREATE PROCEDURE dbo.sp_LapHoaDon
    @MaPK      VARCHAR(10),
    @NguoiLap  VARCHAR(10),
    @NgayLap   DATE = NULL,            -- NULL = ngày hiện tại
    @MaHD      VARCHAR(10) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @@TRANCOUNT > 0
        THROW 50040, N'Hãy gọi thủ tục này ngoài giao dịch đang mở.', 1;

    BEGIN TRY
        BEGIN TRANSACTION;

        DECLARE @DaThanhToan BIT, @TienKham DECIMAL(18,2), @TienThuoc DECIMAL(18,2), @So INT;

        SELECT @DaThanhToan = DaThanhToan
        FROM dbo.PhieuKham WITH (UPDLOCK, HOLDLOCK)
        WHERE MaPK = @MaPK;

        IF @DaThanhToan IS NULL
            THROW 50041, N'Phiếu khám không tồn tại.', 1;
        IF @DaThanhToan = 1
            THROW 50042, N'Phiếu khám này đã được thanh toán.', 1;

        IF NOT EXISTS (SELECT 1 FROM dbo.NhanVien
                       WHERE MaNV = @NguoiLap AND ChucVu = N'Y tá' AND TrangThai = N'Đang làm việc')
            THROW 50043, N'Người lập hóa đơn phải là Y tá đang làm việc.', 1;

        -- Tiền khám lấy từ THAMSO và được sao chép cố định vào hóa đơn
        SELECT @TienKham = GiaTri FROM dbo.ThamSo WHERE MaThamSo = 'TienKham';
        IF @TienKham IS NULL
            THROW 50044, N'Chưa cấu hình tham số TienKham.', 1;

        -- Tiền thuốc tính từ đơn giá đã lưu trong CT_DonThuoc
        SELECT @TienThuoc = ISNULL(SUM(c.SoLuong * c.DonGia), 0)
        FROM dbo.DonThuoc d
        JOIN dbo.CT_DonThuoc c ON c.MaDT = d.MaDT
        WHERE d.MaPK = @MaPK;

        SELECT @So = ISNULL(MAX(CAST(SUBSTRING(MaHD, 3, 8) AS INT)), 0) + 1
        FROM dbo.HoaDon WITH (UPDLOCK, HOLDLOCK)
        WHERE MaHD LIKE 'HD%';

        SET @MaHD = 'HD' + RIGHT('000000' + CAST(@So AS VARCHAR(8)), 6);

        INSERT dbo.HoaDon (MaHD, MaPK, NguoiLap, NgayLap, TienKham, TienThuoc, TongTien)
        VALUES (@MaHD, @MaPK, @NguoiLap, ISNULL(@NgayLap, CAST(GETDATE() AS DATE)),
                @TienKham, @TienThuoc, @TienKham + @TienThuoc);

        UPDATE dbo.PhieuKham SET DaThanhToan = 1 WHERE MaPK = @MaPK;

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

/* ---------- 4.6 UC04 - Lập báo cáo tháng ---------- */

-- BM5.1: Báo cáo doanh thu theo tháng (chạy lại sẽ tính lại báo cáo của tháng đó)
CREATE PROCEDURE dbo.sp_LapBaoCaoDoanhThu
    @Thang INT,
    @Nam   INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @Thang NOT BETWEEN 1 AND 12 OR @Nam <= 2000
        THROW 50050, N'Tháng hoặc năm không hợp lệ.', 1;

    DECLARE @MaBCDT VARCHAR(10) = 'DT' + RIGHT('0' + CAST(@Thang AS VARCHAR(2)), 2) + CAST(@Nam AS VARCHAR(4));
    DECLARE @TuNgay DATE = DATEFROMPARTS(@Nam, @Thang, 1);
    DECLARE @DenNgay DATE = DATEADD(MONTH, 1, @TuNgay);
    DECLARE @Tong DECIMAL(18,2);

    BEGIN TRY
        BEGIN TRANSACTION;

        SELECT @Tong = ISNULL(SUM(TongTien), 0)
        FROM dbo.HoaDon
        WHERE NgayLap >= @TuNgay AND NgayLap < @DenNgay;

        DELETE ct
        FROM dbo.CT_BaoCaoDoanhThu ct
        JOIN dbo.BaoCaoDoanhThu b ON b.MaBCDT = ct.MaBCDT
        WHERE b.Thang = @Thang AND b.Nam = @Nam;

        DELETE FROM dbo.BaoCaoDoanhThu WHERE Thang = @Thang AND Nam = @Nam;

        INSERT dbo.BaoCaoDoanhThu (MaBCDT, Thang, Nam, TongDoanhThu)
        VALUES (@MaBCDT, @Thang, @Nam, @Tong);

        INSERT dbo.CT_BaoCaoDoanhThu (MaBCDT, Ngay, SoBN, DoanhThu, TyLe)
        SELECT  @MaBCDT,
                NgayLap,
                COUNT(*),
                SUM(TongTien),
                CASE WHEN @Tong = 0 THEN 0
                     ELSE CAST(SUM(TongTien) * 100.0 / @Tong AS DECIMAL(5,2)) END
        FROM dbo.HoaDon
        WHERE NgayLap >= @TuNgay AND NgayLap < @DenNgay
        GROUP BY NgayLap;

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

-- BM5.2: Báo cáo sử dụng thuốc theo tháng (tính theo ngày kê đơn)
CREATE PROCEDURE dbo.sp_LapBaoCaoSDThuoc
    @Thang INT,
    @Nam   INT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    IF @Thang NOT BETWEEN 1 AND 12 OR @Nam <= 2000
        THROW 50051, N'Tháng hoặc năm không hợp lệ.', 1;

    DECLARE @MaBCSDT VARCHAR(10) = 'SD' + RIGHT('0' + CAST(@Thang AS VARCHAR(2)), 2) + CAST(@Nam AS VARCHAR(4));
    DECLARE @TuNgay DATE = DATEFROMPARTS(@Nam, @Thang, 1);
    DECLARE @DenNgay DATE = DATEADD(MONTH, 1, @TuNgay);

    BEGIN TRY
        BEGIN TRANSACTION;

        DELETE ct
        FROM dbo.CT_BaoCaoSDThuoc ct
        JOIN dbo.BaoCaoSDThuoc b ON b.MaBCSDT = ct.MaBCSDT
        WHERE b.Thang = @Thang AND b.Nam = @Nam;

        DELETE FROM dbo.BaoCaoSDThuoc WHERE Thang = @Thang AND Nam = @Nam;

        INSERT dbo.BaoCaoSDThuoc (MaBCSDT, Thang, Nam)
        VALUES (@MaBCSDT, @Thang, @Nam);

        INSERT dbo.CT_BaoCaoSDThuoc (MaBCSDT, MaThuoc, MaDVT, SoLuong, SoLanDung)
        SELECT  @MaBCSDT,
                c.MaThuoc,
                t.MaDVT,
                SUM(c.SoLuong),
                COUNT(DISTINCT c.MaDT)
        FROM dbo.DonThuoc d
        JOIN dbo.CT_DonThuoc c ON c.MaDT = d.MaDT
        JOIN dbo.Thuoc t       ON t.MaThuoc = c.MaThuoc
        WHERE d.NgayKe >= @TuNgay AND d.NgayKe < @DenNgay
        GROUP BY c.MaThuoc, t.MaDVT;

        COMMIT;
    END TRY
    BEGIN CATCH
        IF XACT_STATE() <> 0 ROLLBACK;
        THROW;
    END CATCH;
END;
GO

/*==============================================================================
  PHẦN 5: DỮ LIỆU MẪU
==============================================================================*/

-- 5.1 Tham số (QĐ1, QĐ4)
INSERT INTO dbo.ThamSo (MaThamSo, TenThamSo, GiaTri, GhiChu) VALUES
('SoBNToiDa', N'Số bệnh nhân tối đa mỗi ngày', 40,    N'Số bệnh nhân tối đa khám trong một ngày (QĐ1)'),
('TienKham',  N'Tiền khám bệnh',               30000, N'Tiền khám mặc định cho một lượt khám (QĐ4)');
GO

-- 5.2 Danh mục (QĐ2): 5 loại bệnh, 2 đơn vị tính, 4 cách dùng, 30 loại thuốc
INSERT INTO dbo.LoaiBenh (MaLoaiBenh, TenLoaiBenh, TrieuChungDacTrung) VALUES
('LB01', N'Loại bệnh 1', N'Triệu chứng đặc trưng của loại bệnh 1'),
('LB02', N'Loại bệnh 2', N'Triệu chứng đặc trưng của loại bệnh 2'),
('LB03', N'Loại bệnh 3', N'Triệu chứng đặc trưng của loại bệnh 3'),
('LB04', N'Loại bệnh 4', N'Triệu chứng đặc trưng của loại bệnh 4'),
('LB05', N'Loại bệnh 5', N'Triệu chứng đặc trưng của loại bệnh 5');
GO

INSERT INTO dbo.DonViTinh (MaDVT, TenDVT) VALUES
('DVT01', N'Viên'),
('DVT02', N'Chai');
GO

INSERT INTO dbo.CachDung (MaCachDung, TenCachDung) VALUES
('CD01', N'Cách dùng 1'),
('CD02', N'Cách dùng 2'),
('CD03', N'Cách dùng 3'),
('CD04', N'Cách dùng 4');
GO

INSERT INTO dbo.Thuoc (MaThuoc, TenThuoc, MaDVT, MaCachDung, DonGia, SoLuongTon, TrangThai) VALUES
('TH01', N'Thuốc 01', 'DVT01', 'CD01',  2000, 100, N'Đang sử dụng'),
('TH02', N'Thuốc 02', 'DVT01', 'CD02',  3000, 100, N'Đang sử dụng'),
('TH03', N'Thuốc 03', 'DVT02', 'CD01', 25000, 100, N'Đang sử dụng'),
('TH04', N'Thuốc 04', 'DVT01', 'CD03',  1500, 100, N'Đang sử dụng'),
('TH05', N'Thuốc 05', 'DVT01', 'CD04',  5000, 100, N'Đang sử dụng'),
('TH06', N'Thuốc 06', 'DVT02', 'CD04', 40000, 100, N'Đang sử dụng'),
('TH07', N'Thuốc 07', 'DVT01', 'CD01',  4500, 100, N'Đang sử dụng'),
('TH08', N'Thuốc 08', 'DVT01', 'CD02',  6000, 100, N'Đang sử dụng'),
('TH09', N'Thuốc 09', 'DVT02', 'CD01', 35000, 100, N'Đang sử dụng'),
('TH10', N'Thuốc 10', 'DVT01', 'CD03',  2200, 100, N'Đang sử dụng'),
('TH11', N'Thuốc 11', 'DVT01', 'CD01',  3200, 100, N'Đang sử dụng'),
('TH12', N'Thuốc 12', 'DVT02', 'CD04', 50000, 100, N'Đang sử dụng'),
('TH13', N'Thuốc 13', 'DVT01', 'CD02',  1800, 100, N'Đang sử dụng'),
('TH14', N'Thuốc 14', 'DVT01', 'CD03',  4000, 100, N'Đang sử dụng'),
('TH15', N'Thuốc 15', 'DVT02', 'CD01', 65000, 100, N'Đang sử dụng'),
('TH16', N'Thuốc 16', 'DVT01', 'CD01',  2700, 100, N'Đang sử dụng'),
('TH17', N'Thuốc 17', 'DVT01', 'CD02',  3800, 100, N'Đang sử dụng'),
('TH18', N'Thuốc 18', 'DVT02', 'CD04', 18000, 100, N'Đang sử dụng'),
('TH19', N'Thuốc 19', 'DVT01', 'CD03',  5500, 100, N'Đang sử dụng'),
('TH20', N'Thuốc 20', 'DVT01', 'CD04',  1200, 100, N'Đang sử dụng'),
('TH21', N'Thuốc 21', 'DVT02', 'CD01', 80000, 100, N'Đang sử dụng'),
('TH22', N'Thuốc 22', 'DVT01', 'CD01',  3100, 100, N'Đang sử dụng'),
('TH23', N'Thuốc 23', 'DVT01', 'CD02',  4200, 100, N'Đang sử dụng'),
('TH24', N'Thuốc 24', 'DVT02', 'CD04', 22000, 100, N'Đang sử dụng'),
('TH25', N'Thuốc 25', 'DVT01', 'CD03',  6500, 100, N'Đang sử dụng'),
('TH26', N'Thuốc 26', 'DVT01', 'CD01',  1900, 100, N'Đang sử dụng'),
('TH27', N'Thuốc 27', 'DVT02', 'CD01', 95000, 100, N'Đang sử dụng'),
('TH28', N'Thuốc 28', 'DVT01', 'CD02',  4800, 100, N'Đang sử dụng'),
('TH29', N'Thuốc 29', 'DVT01', 'CD03',  7000, 100, N'Đang sử dụng'),
('TH30', N'Thuốc 30', 'DVT02', 'CD04', 30000, 100, N'Đang sử dụng');
GO

-- 5.3 Vai trò, nhân viên, tài khoản, lịch trực
INSERT INTO dbo.VaiTro (MaVaiTro, TenVaiTro) VALUES
('VT01', N'Quản trị viên'),
('VT02', N'Quản lý'),
('VT03', N'Bác sĩ'),
('VT04', N'Y tá');
GO

INSERT INTO dbo.NhanVien (MaNV, HoTen, GioiTinh, NgaySinh, DiaChi, SoDienThoai, Email, ChucVu, TrangThai) VALUES
('NV000001', N'Nguyễn Văn A', N'Nam', '2004-11-15', N'TP.HCM', '0987654321', 'anguyen@gmail.com', N'Quản trị viên', N'Đang làm việc'),
('NV000002', N'Trần Văn Năm', N'Nam', '2005-02-20', N'TP.HCM', '0912345678', 'namtv@gmail.com',   N'Bác sĩ',        N'Đang làm việc'),
('NV000003', N'Lý Thị Sáu',   N'Nữ',  '2005-01-01', N'TP.HCM', '0901234567', 'sault@gmail.com',   N'Y tá',          N'Đang làm việc');
GO

-- Lưu ý: mật khẩu mẫu để dạng thô cho dễ thử nghiệm; khi triển khai thật nên băm (hash) trước khi lưu.
INSERT INTO dbo.TaiKhoan (MaTK, MaNV, MaVaiTro, TenDangNhap, MatKhau, TrangThai) VALUES
('TK000001', 'NV000001', 'VT01', 'admin',  '123456', N'Hoạt động'),
('TK000002', 'NV000002', 'VT03', 'dongbs', '123456', N'Hoạt động'),
('TK000003', 'NV000003', 'VT04', 'tuanyt', '123456', N'Hoạt động');
GO

INSERT INTO dbo.LichTruc (MaLT, MaNV, NgayTruc, CaTruc, GhiChu) VALUES
('LT000001', 'NV000002', '2026-10-27', N'Sáng',  N'Trực phòng khám 1'),
('LT000002', 'NV000003', '2026-10-27', N'Chiều', N'Hỗ trợ tiếp nhận');
GO

-- 5.4 Bệnh nhân & danh sách khám
INSERT INTO dbo.BenhNhan (MaBN, HoTen, GioiTinh, NamSinh, DiaChi, SoDienThoai) VALUES
('BN000001', N'Nguyễn Văn An',  N'Nam', 1985, N'12 Lê Lợi, Quận 1, TP.HCM',      '0901234567'),
('BN000002', N'Trần Thị Bình',  N'Nữ',  1992, N'45 Nguyễn Trãi, Quận 5, TP.HCM', '0912345678'),
('BN000003', N'Lê Hoàng Cường', N'Nam', 1978, N'8 Võ Văn Ngân, TP. Thủ Đức',     '0923456789'),
('BN000004', N'Phạm Thị Dung',  N'Nữ',  2001, N'102 Cách Mạng Tháng 8, Quận 3',  '0934567890'),
('BN000005', N'Hoàng Minh Đức', N'Nam', 1965, N'27 Phan Đăng Lưu, Bình Thạnh',   '0945678901');
GO

INSERT INTO dbo.DanhSachKham (MaDSK, NgayKham) VALUES
('DS20261026', '2026-10-26'),
('DS20261027', '2026-10-27');
GO

INSERT INTO dbo.CT_DanhSachKham (MaDSK, MaBN, STT, TrangThai) VALUES
('DS20261026', 'BN000001', 1, N'Đã khám'),
('DS20261026', 'BN000002', 2, N'Đã khám'),
('DS20261026', 'BN000003', 3, N'Đang chờ'),
('DS20261027', 'BN000004', 1, N'Đang chờ'),
('DS20261027', 'BN000001', 2, N'Đang chờ');
GO

-- 5.5 Phiếu khám & đơn thuốc (2 bệnh nhân đã khám ngày 26/10/2026)
INSERT INTO dbo.PhieuKham (MaPK, NgayKham, TrieuChung, MaBN, MaLoaiBenh, MaBS) VALUES
('PK000001', '2026-10-26', N'Sốt, đau đầu, ho khan', 'BN000001', 'LB01', 'NV000002'),
('PK000002', '2026-10-26', N'Đau bụng, buồn nôn',    'BN000002', 'LB02', 'NV000002');
GO

INSERT INTO dbo.DonThuoc (MaDT, MaPK, NgayKe) VALUES
('DT000001', 'PK000001', '2026-10-26'),
('DT000002', 'PK000002', '2026-10-26');
GO

-- Thêm thuốc qua thủ tục để kiểm tra tồn kho, lưu đơn giá và trừ tồn
EXEC dbo.usp_ThemThuocVaoDon 'DT000001', 'TH01', 10, 'CD01';
EXEC dbo.usp_ThemThuocVaoDon 'DT000001', 'TH03', 1,  'CD02';
EXEC dbo.usp_ThemThuocVaoDon 'DT000002', 'TH02', 6,  'CD03';
GO

-- 5.6 Hóa đơn: thanh toán cho PK000001 (PK000002 để chưa thanh toán)
DECLARE @MaHD VARCHAR(10);
EXEC dbo.sp_LapHoaDon @MaPK = 'PK000001', @NguoiLap = 'NV000003', @NgayLap = '2026-10-26', @MaHD = @MaHD OUTPUT;
GO

-- 5.7 Báo cáo tháng 10/2026
EXEC dbo.sp_LapBaoCaoDoanhThu @Thang = 10, @Nam = 2026;
EXEC dbo.sp_LapBaoCaoSDThuoc  @Thang = 10, @Nam = 2026;
GO

/*==============================================================================
  KIỂM TRA NHANH (bỏ comment để chạy)
==============================================================================*/
-- SELECT * FROM dbo.v_DanhSachKhamBenh ORDER BY NgayKham, STT;
-- SELECT * FROM dbo.v_TraCuuBenhNhan;
-- SELECT * FROM dbo.vw_TongTienDonThuoc;
-- SELECT * FROM dbo.HoaDon;
-- SELECT * FROM dbo.CT_BaoCaoDoanhThu;
-- SELECT * FROM dbo.CT_BaoCaoSDThuoc;
