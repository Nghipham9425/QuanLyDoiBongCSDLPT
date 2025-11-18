------------------------------------------------------------
-- RESET DATABASE
------------------------------------------------------------
USE master;
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'QL_BongDa')
BEGIN
    ALTER DATABASE QL_BongDa SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE QL_BongDa;
END;
GO

CREATE DATABASE QL_BongDa;
GO
USE QL_BongDa;
GO

------------------------------------------------------------
-- TẠO BẢNG
------------------------------------------------------------
CREATE TABLE DoiBong (
    MaDB CHAR(4) PRIMARY KEY,
    TenDB NVARCHAR(50),
    CLB NVARCHAR(20)
);

CREATE TABLE CauThu (
    MaCT VARCHAR(10) PRIMARY KEY,
    HoTen NVARCHAR(50),
    ViTri NVARCHAR(20),
    MaDB CHAR(4) NOT NULL,
    FOREIGN KEY (MaDB) REFERENCES DoiBong(MaDB)
);

CREATE TABLE TranDau (
    MaTD CHAR(4) PRIMARY KEY,
    MaDB1 CHAR(4) NOT NULL,
    MaDB2 CHAR(4) NOT NULL,
    TrongTai NVARCHAR(50),
    SanDau NVARCHAR(20),
    FOREIGN KEY (MaDB1) REFERENCES DoiBong(MaDB),
    FOREIGN KEY (MaDB2) REFERENCES DoiBong(MaDB)
);

CREATE TABLE ThamGia (
    MaTD CHAR(4),
    MaCT VARCHAR(10),
    SoTrai INT CHECK (SoTrai >= 0),
    PRIMARY KEY (MaTD, MaCT),
    FOREIGN KEY (MaTD) REFERENCES TranDau(MaTD),
    FOREIGN KEY (MaCT) REFERENCES CauThu(MaCT)
);
GO

------------------------------------------------------------
-- ĐỘI BÓNG
------------------------------------------------------------
INSERT INTO DoiBong VALUES
('DB01', N'Manchester City', N'CLB1'),
('DB02', N'Arsenal', N'CLB1'),
('DB03', N'Real Madrid', N'CLB2'),
('DB04', N'Barcelona', N'CLB2');
GO

------------------------------------------------------------
-- CẦU THỦ (11 NGƯỜI / ĐỘI)
------------------------------------------------------------

-- ⚽ Manchester City
INSERT INTO CauThu VALUES
('CT01', N'Ederson Moraes', N'Thủ môn', 'DB01'),
('CT02', N'Kyle Walker', N'Hậu vệ', 'DB01'),
('CT03', N'John Stones', N'Hậu vệ', 'DB01'),
('CT04', N'Rúben Dias', N'Hậu vệ', 'DB01'),
('CT05', N'Joško Gvardiol', N'Hậu vệ', 'DB01'),
('CT06', N'Rodri', N'Tiền vệ', 'DB01'),
('CT07', N'Kevin De Bruyne', N'Tiền vệ', 'DB01'),
('CT08', N'Bernardo Silva', N'Tiền vệ', 'DB01'),
('CT09', N'Phil Foden', N'Tiền đạo', 'DB01'),
('CT10', N'Jack Grealish', N'Tiền đạo', 'DB01'),
('CT11', N'Erling Haaland', N'Tiền đạo', 'DB01');

-- ⚽ Arsenal
INSERT INTO CauThu VALUES
('CT12', N'Aaron Ramsdale', N'Thủ môn', 'DB02'),
('CT13', N'Ben White', N'Hậu vệ', 'DB02'),
('CT14', N'William Saliba', N'Hậu vệ', 'DB02'),
('CT15', N'Gabriel Magalhães', N'Hậu vệ', 'DB02'),
('CT16', N'Takehiro Tomiyasu', N'Hậu vệ', 'DB02'),
('CT17', N'Declan Rice', N'Tiền vệ', 'DB02'),
('CT18', N'Martin Ødegaard', N'Tiền vệ', 'DB02'),
('CT19', N'Kai Havertz', N'Tiền vệ', 'DB02'),
('CT20', N'Bukayo Saka', N'Tiền đạo', 'DB02'),
('CT21', N'Gabriel Jesus', N'Tiền đạo', 'DB02'),
('CT22', N'Leandro Trossard', N'Tiền đạo', 'DB02');

-- ⚽ Real Madrid
INSERT INTO CauThu VALUES
('CT23', N'Thiabaut Courtois', N'Thủ môn', 'DB03'),
('CT24', N'Dani Carvajal', N'Hậu vệ', 'DB03'),
('CT25', N'Antonio Rüdiger', N'Hậu vệ', 'DB03'),
('CT26', N'David Alaba', N'Hậu vệ', 'DB03'),
('CT27', N'Ferland Mendy', N'Hậu vệ', 'DB03'),
('CT28', N'Aurélien Tchouaméni', N'Tiền vệ', 'DB03'),
('CT29', N'Luka Modrić', N'Tiền vệ', 'DB03'),
('CT30', N'Jude Bellingham', N'Tiền vệ', 'DB03'),
('CT31', N'Vinícius Júnior', N'Tiền đạo', 'DB03'),
('CT32', N'Rodrygo Goes', N'Tiền đạo', 'DB03'),
('CT33', N'Kylian Mbappé', N'Tiền đạo', 'DB03');

-- ⚽ Barcelona
INSERT INTO CauThu VALUES
('CT34', N'Marc-André ter Stegen', N'Thủ môn', 'DB04'),
('CT35', N'Jules Koundé', N'Hậu vệ', 'DB04'),
('CT36', N'Ronald Araújo', N'Hậu vệ', 'DB04'),
('CT37', N'Alejandro Balde', N'Hậu vệ', 'DB04'),
('CT38', N'Andreas Christensen', N'Hậu vệ', 'DB04'),
('CT39', N'Ilkay Gündogan', N'Tiền vệ', 'DB04'),
('CT40', N'Frenkie de Jong', N'Tiền vệ', 'DB04'),
('CT41', N'Pedri', N'Tiền vệ', 'DB04'),
('CT42', N'Gavi', N'Tiền vệ', 'DB04'),
('CT43', N'Lamine Yamal', N'Tiền đạo', 'DB04'),
('CT44', N'Robert Lewandowski', N'Tiền đạo', 'DB04');
GO

------------------------------------------------------------
-- TRẬN ĐẤU
------------------------------------------------------------
INSERT INTO TranDau VALUES
('TD01', 'DB01', 'DB02', N'Michael Oliver', N'SD1'),
('TD02', 'DB03', 'DB04', N'Antonio Mateu Lahoz', N'SD2'),
('TD03', 'DB01', 'DB03', N'Szymon Marciniak', N'SD1'),
('TD04', 'DB02', 'DB04', N'Daniele Orsato', N'SD2'),
('TD05', 'DB03', 'DB01', N'Clement Turpin', N'SD1'),
('TD06', 'DB04', 'DB02', N'Carlos del Cerro', N'SD2');
GO

------------------------------------------------------------
-- THAM GIA (vài cầu thủ tiêu biểu mỗi trận)
------------------------------------------------------------
INSERT INTO ThamGia VALUES
-- TD01
('TD01', 'CT11', 2), ('TD01', 'CT07', 1), ('TD01', 'CT20', 1), ('TD01', 'CT18', 0),
-- TD02
('TD02', 'CT33', 1), ('TD02', 'CT31', 1), ('TD02', 'CT44', 1), ('TD02', 'CT41', 0),
-- TD03
('TD03', 'CT11', 2), ('TD03', 'CT08', 1), ('TD03', 'CT31', 1), ('TD03', 'CT30', 1),
-- TD04
('TD04', 'CT21', 1), ('TD04', 'CT20', 0), ('TD04', 'CT44', 2), ('TD04', 'CT42', 0),
-- TD05
('TD05', 'CT33', 2), ('TD05', 'CT32', 0), ('TD05', 'CT11', 1), ('TD05', 'CT10', 0),
-- TD06
('TD06', 'CT43', 1), ('TD06', 'CT41', 1), ('TD06', 'CT20', 1), ('TD06', 'CT19', 0);
GO

------------------------------------------------------------
-- KIỂM TRA
------------------------------------------------------------
SELECT * FROM DoiBong;
SELECT * FROM CauThu ORDER BY MaCT;
SELECT * FROM TranDau;
SELECT * FROM ThamGia;
GO
