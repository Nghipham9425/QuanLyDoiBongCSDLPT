# 📋 Hướng Dẫn Sử Dụng Hệ Thống Quản Lý Đội Bóng

## 🎯 Cập Nhật Mới

### ✅ Login Screen - Chọn Chi Nhánh

Giao diện login đã được cập nhật:

1. **Chọn chi nhánh (CLB) trước**

   - CLB1 - SD1 (Database 1)
   - CLB2 - SD2 (Database 2)

2. **Nhập tài khoản**

   - Username: `sa`
   - Password: (để trống)
   - HOẶC
   - Username: `admin`
   - Password: `admin`

3. **Kết quả**
   - Sau login, title bar hiển thị tài khoản + chi nhánh đã chọn
   - Ví dụ: `sa - CLB1 - SD1 (Database 1)`

---

## 🚀 Chạy Ứng Dụng

### Cách 1: **Chạy từ Terminal (Khuyến cáo)**

```powershell
cd c:\Users\nghip\Desktop\CSDLPT_Demo\QlDoiBong
mvn clean javafx:run
```

### Cách 2: **NetBeans**

Cấu hình Run Goal: `clean javafx:run`

### Cách 3: **Visual Studio Code**

Mở Terminal → Chạy lệnh trên

---

## 📊 Các Màn Hình Chính

### 1. 🏠 **Dashboard - Tổng Quan**

Hiển thị:

- 4 Card thống kê: Đội bóng, Cầu thủ, Trận đấu, Bàn thắng
- Phân bổ dữ liệu giữa 2 Database
- Top 3 Vua phá lưới

**Lưu ý:** Các số liệu được tính từ cả 2 database (DB1 + DB2)

---

### 2. 👥 **Quản Lý Đội Bóng**

**Chức năng CRUD:**

- ➕ **Thêm:** Chọn CLB → nhập Mã đội, Tên đội → Thêm
  - CLB1 → lưu vào DB1
  - CLB2 → lưu vào DB2
- ✏️ **Sửa:** Chọn dòng trong bảng → sửa thông tin → Sửa
- 🗑️ **Xóa:** Chọn dòng → Xóa
- 🔍 **Tìm kiếm:** Nhập tên đội → Tìm

**Filter xem dữ liệu:**

- ● **Tất cả:** Hiển thị từ cả 2 database
- ○ **CLB1 (DB1):** Chỉ hiển thị dữ liệu từ DB1
- ○ **CLB2 (DB2):** Chỉ hiển thị dữ liệu từ DB2

**⚠️ Quan trọng:**

- RadioButton filter **KHÔNG ảnh hưởng** việc Thêm/Sửa/Xóa
- Quyết định DB nào được lưu dựa vào **ComboBox CLB** được chọn

---

### 3. ⚽ **Quản Lý Cầu Thủ**

**Chức năng CRUD:**

- ➕ **Thêm:**
  - Mã cầu thủ (CT01, CT02...)
  - Họ tên
  - Vị trí (Tiền đạo, Hậu vệ, Thủ môn, Tiền vệ)
  - Chọn Đội bóng (tự động xác định CLB)
- ✏️ **Sửa:** Chọn cầu thủ → sửa → Sửa
- 🗑️ **Xóa:** Chọn cầu thủ → Xóa
- 🔍 **Tìm kiếm:** Nhập tên → Tìm

**Filter:**

- ● **Tất cả:** Hiển thị từ cả 2 database
- ○ **CLB1 (DB1):** Chỉ cầu thủ thuộc đội CLB1
- ○ **CLB2 (DB2):** Chỉ cầu thủ thuộc đội CLB2

---

### 4. 🏟️ **Quản Lý Trận Đấu & Nhập Điểm**

**Gồm 2 Tab:**

#### **Tab 1: Danh sách trận**

**Chức năng CRUD Trận Đấu:**

- ➕ **Thêm trận:**
  - Mã trận (TD01, TD02...)
  - Chọn Đội 1 (MaDB1)
  - Chọn Đội 2 (MaDB2)
  - Nhập Trọng tài
  - Chọn Sân đấu (SD1 → DB1, SD2 → DB2)
- ✏️ **Sửa:** Chọn trận → Sửa
- 🗑️ **Xóa:** Chọn trận → Xóa
- 📝 **Nhập Điểm:**
  1. Chọn trận trong bảng
  2. Click [📝 Điểm]
  3. Mở popup:
     - Tick ☑️ cầu thủ tham gia
     - Nhập số bàn thắng
     - Click [💾 Lưu]
  4. Tự động tạo bảng ThamGia

**Filter:**

- ● **Tất cả:** Hiển thị từ cả 2 sân
- ○ **SD1 (DB1):** Chỉ trận tại sân SD1
- ○ **SD2 (DB2):** Chỉ trận tại sân SD2

#### **Tab 2: Chi tiết trận**

Hiển thị READ-ONLY khi chọn 1 trận:

- Danh sách cầu thủ tham gia
- Số bàn thắng của từng cầu thủ
- Tỷ số cuối cùng
- Vua phá lưới trận này

---

### 5. 🔍 **Truy Vấn Phân Tán** _(Chưa tối ưu)_

Hiện tại là placeholder. Sẽ implement 10 câu query sau:

1. Tìm cầu thủ theo CLB
2. Đếm số trận của cầu thủ
3. Trận hòa theo sân
4. Vua phá lưới
5. Tìm trận theo cầu thủ & trọng tài
6. 2 cầu thủ cùng CLB
7. Cầu thủ ghi 0 bàn
8. Cầu thủ tích cực (≥3 trận)
9. Tổng bàn thắng của đội
10. Cầu thủ chưa thi đấu

---

## 🎨 Giao Diện & CSS

### Màu sắc chủ đạo:

- **Đỏ Coral (#EF5350):** Nút Primary, Header
- **Xanh Lá (#81C784):** Nút Success, Stats
- **Cam (#FFA726):** Nút Warning
- **Xanh Lơ (#26C6DA):** Nút Info, Refresh

### Các Component đã cải tiến:

- ✅ Buttons: Gradient, Shadow, Hover effects
- ✅ Tables: Header đỏ nhẹ, Row alternating colors
- ✅ TextFields: Rounded, Border focus đỏ
- ✅ Cards: White, Shadow, Hover translate up
- ✅ Tooltips: Dark background, whitespace

---

## 🔧 Cấu Hình Database

### Thông tin kết nối hiện tại:

Kiểm tra file: `src/main/java/org/football/utils/DatabaseConnection.java`

**Database 1:**

- Server: `localhost:1433` (hoặc `Winter`)
- Database: `QL_BongDa_CLB1_SD1`
- User: `sa`

**Database 2:**

- Server: `localhost:1434` (hoặc `sv1`)
- Database: `QL_BongDa_CLB2_SD2`
- User: `sa`

### Kiểm tra kết nối:

```sql
-- Kiểm tra DB1
SELECT * FROM DoiBong WHERE CLB = 'CLB1'

-- Kiểm tra DB2
SELECT * FROM DoiBong WHERE CLB = 'CLB2'
```

---

## 🐛 Troubleshooting

### ❌ Lỗi: "Connection refused"

**Nguyên nhân:** SQL Server không chạy  
**Giải pháp:**

```powershell
# Khởi động SQL Server
# Windows: Services → SQL Server (MSSQLSERVER)
# Docker: docker start mssql
```

### ❌ Lỗi: "Database not found"

**Nguyên nhân:** Database chưa tạo  
**Giải pháp:** Chạy SQL Script để tạo database + tables

### ❌ Giao diện trống sau login

**Nguyên nhân:** FXML không load được  
**Giải pháp:**

1. Kiểm tra file FXML tồn tại trong `src/main/resources/fxml/`
2. Compile lại: `mvn clean compile`

### ❌ NetBeans click Run không chạy

**Giải pháp:**

1. Chuột phải project → Properties
2. Mục "Run" → Set "Execute Goals" = `clean javafx:run`
3. OK

---

## 📝 Quy ước Đặt Tên

| Loại            | Quy ước              | Ví dụ                                |
| --------------- | -------------------- | ------------------------------------ |
| Package         | lowercase            | `org.football.controllers`           |
| Class           | PascalCase           | `CauThuDAO`, `QueryService`          |
| Method          | camelCase            | `findByHoTen()`, `handleLogin()`     |
| Variable        | camelCase            | `maCT`, `hoTen`, `selectedCLB`       |
| FXML ID         | camelCase + prefix   | `txtMaCT`, `btnThem`, `tableDoiBong` |
| Database Column | PascalCase không dấu | `MaCT`, `HoTen`, `MaDB`, `SanDau`    |

---

## 🎓 Kiến Trúc Dự Án

```
QlDoiBong/
├── src/main/java/org/football/
│   ├── App.java (Entry point)
│   ├── controllers/ (9 Controllers)
│   │   ├── LoginController.java ← Cập nhật: Chọn CLB
│   │   ├── MainController.java  ← Cập nhật: Hiển thị CLB
│   │   ├── DashboardController.java
│   │   ├── DoiBongController.java
│   │   ├── CauThuController.java
│   │   ├── TranDauController.java
│   │   ├── DiemSoDialog.java
│   │   ├── QueryController.java
│   │   └── ...
│   ├── dao/ (4 DAOs)
│   ├── services/ (4 Services)
│   ├── models/ (4 Models)
│   └── utils/ (DatabaseConnection)
├── src/main/resources/
│   ├── fxml/ (7 FXML files)
│   └── styles/ (CSS files)
├── pom.xml (Maven config)
└── README.md

```

---

## 📞 Liên Hệ & Support

Nếu gặp vấn đề:

1. Kiểm tra console output → tìm lỗi
2. Xem file log (nếu có)
3. Kiểm tra kết nối database
4. Tìm kiếm lỗi tương tự trên Google/StackOverflow

---

**Cập nhật lần cuối:** 2025-11-12  
**Phiên bản:** 1.0-SNAPSHOT  
**Java:** 21  
**JavaFX:** 21.0.1
