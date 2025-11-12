# 🚀 HƯỚNG DẪN QUICK START - HỆ THỐNG QUẢN LÝ ĐỘI BÓNG

## 📋 Tóm Tắt Cập Nhật (v1.0)

### ✅ Hoàn Thành

1. **Login Screen** ✔

   - Chọn Chi nhánh (CLB1/SD1 hoặc CLB2/SD2)
   - Tài khoản: `sa` / Password: `123456`
   - Hoặc: `admin` / `admin`

2. **Logout** ✔

   - Nút "Đăng xuất" ở header
   - Xác nhận → Quay lại Login

3. **Filter Logic** ✔

   - **Mặc định**: Hiển thị **chỉ data chi nhánh đã login**
   - **Nếu chọn "Xem tất cả"**: Merge dữ liệu từ cả 2 DB
   - **RadioButton** ở mỗi màn CRUD:
     - ● **Xem tất cả** = merge DB1 + DB2
     - ○ **CLB1 (DB1)** = chỉ DB1
     - ○ **CLB2 (DB2)** = chỉ DB2

4. **Các Màn Hình CRUD Đã Cải Tiến**
   - ✔ Đội bóng
   - ✔ Cầu thủ
   - ✔ Trận đấu + Nhập điểm
   - ✔ Hiển thị thống kê: "Tổng: X" | "DB1: Y" | "DB2: Z"

---

## 🎯 Cách Sử Dụng

### 1️⃣ Khởi Động

```powershell
cd c:\Users\nghip\Desktop\CSDLPT_Demo\QlDoiBong
mvn javafx:run
```

### 2️⃣ Đăng Nhập

- Chọn chi nhánh: **CLB1 - SD1 (Database 1)** hoặc **CLB2 - SD2 (Database 2)**
- Username: `sa`
- Password: `123456`
- Click **[Đăng nhập]**

### 3️⃣ Sử Dụng Các Màn Hình

#### 📊 Dashboard

- Hiển thị 4 thẻ thống kê
- Phân bổ dữ liệu giữa 2 DB
- Top 3 Vua phá lưới

#### 👥 Quản Lý Đội Bóng

```
Mặc định: Hiển thị CHỈ đội của chi nhánh hiện tại
Để xem tất cả: Chọn RadioButton "Xem tất cả"

CRUD:
- ➕ Thêm: Chọn CLB → nhập dữ liệu → Thêm
- ✏️ Sửa: Chọn dòng → sửa → Sửa
- 🗑️ Xóa: Chọn dòng → Xóa
- 🔍 Tìm: Nhập tên → Tìm
```

#### ⚽ Quản Lý Cầu Thủ

```
Tương tự Đội bóng
Filter mặc định: Cầu thủ của CLB đã login
```

#### 🏟️ Quản Lý Trận Đấu

```
Tab 1: Danh sách trận
- Thêm, Sửa, Xóa trận
- Nút [📝 Nhập điểm] → Popup nhập kết quả

Tab 2: Chi tiết trận (xem ThamGia)
- Click vào trận → Xem danh sách cầu thủ + điểm

Filter: Tất cả / SD1 (DB1) / SD2 (DB2)
```

#### 🔍 Truy Vấn (Chưa hoàn thiện)

- Placeholder, sẽ thêm 10 câu query sau

### 4️⃣ Đăng Xuất

```
Click nút [Đăng xuất] ở header
Xác nhận → Quay lại Login
```

---

## 🗄️ Dữ Liệu Mẫu

Tôi đã tạo file **`SQL_INSERT_DATA.sql`** chứa dữ liệu test:

- **DB1 (CLB1)**: 2 đội, 8 cầu thủ, 2 trận
- **DB2 (CLB2)**: 2 đội, 8 cầu thủ, 2 trận

**Cách import:**

1. Mở SQL Server Management Studio
2. Kết nối tới SQL Server
3. Mở file `SQL_INSERT_DATA.sql`
4. F5 hoặc Ctrl+E để Execute

---

## 🎨 Giao Diện & Màu Sắc

| Phần        | Màu          | Ý Nghĩa    |
| ----------- | ------------ | ---------- |
| Header      | 🔴 Đỏ Coral  | Chính      |
| Sidebar     | 🔵 Xanh Nước | Navigation |
| Nút Success | 🟢 Xanh Lá   | Thêm       |
| Nút Warning | 🟠 Cam       | Sửa        |
| Nút Danger  | 🔴 Đỏ        | Xóa        |
| Nút Info    | 🔵 Xanh Lơ   | Làm mới    |

---

## ⚙️ Cấu Hình Database

### Database 1 (CLB1/SD1)

- **Server**: `localhost:1433` (hoặc `Winter`)
- **Database**: `QL_BongDa_CLB1_SD1`
- **User**: `sa`
- **Fragmentation**:
  - DoiBong: CLB = 'CLB1'
  - CauThu: MaDB của đội CLB1
  - TranDau: SanDau = 'SD1'
  - ThamGia: MaTD từ TranDau tại SD1

### Database 2 (CLB2/SD2)

- **Server**: `localhost:1434` (hoặc `sv1`)
- **Database**: `QL_BongDa_CLB2_SD2`
- **User**: `sa`
- **Fragmentation**:
  - DoiBong: CLB = 'CLB2'
  - CauThu: MaDB của đội CLB2
  - TranDau: SanDau = 'SD2'
  - ThamGia: MaTD từ TranDau tại SD2

**File cấu hình:** `src/main/java/org/football/utils/DatabaseConnection.java`

---

## 🔧 Troubleshooting

### ❌ Lỗi: "Cannot find property 'CLB'"

- **Nguyên nhân**: DoiBong model thiếu getter/setter
- **Giải pháp**: Kiểm tra class DoiBong có `getClb()` method

### ❌ Lỗi: "Connection refused"

- **Nguyên nhân**: SQL Server chưa chạy
- **Giải pháp**:
  ```powershell
  # Windows
  services.msc → SQL Server (MSSQLSERVER) → Start
  # Docker
  docker start mssql
  ```

### ❌ Giao diện trống sau login

- **Nguyên nhân**: FXML không load
- **Giải pháp**:
  ```bash
  mvn clean compile
  mvn javafx:run
  ```

### ❌ Không thấy dữ liệu

- **Nguyên nhân**: Database chưa có dữ liệu
- **Giải pháp**: Chạy file `SQL_INSERT_DATA.sql`

---

## 📁 Cấu Trúc Thư Mục

```
QlDoiBong/
├── src/main/
│   ├── java/org/football/
│   │   ├── App.java                          (Entry point)
│   │   ├── controllers/                      (9 Controllers)
│   │   │   ├── LoginController.java          ✔ Đăng nhập
│   │   │   ├── MainController.java           ✔ Navigation + Logout
│   │   │   ├── DoiBongController.java        ✔ CRUD đội
│   │   │   ├── CauThuController.java         ✔ CRUD cầu thủ
│   │   │   ├── TranDauController.java        ✔ CRUD trận
│   │   │   ├── DashboardController.java      (Placeholder)
│   │   │   ├── QueryController.java          (Placeholder - 10 queries)
│   │   │   ├── DiemSoDialog.java             (Popup nhập điểm)
│   │   │   └── ...
│   │   ├── dao/                              (4 DAOs)
│   │   ├── services/                         (4 Services)
│   │   ├── models/                           (4 Models)
│   │   └── utils/DatabaseConnection.java     (Connection management)
│   └── resources/
│       ├── fxml/                             (7 FXML files)
│       └── styles/                           (CSS files)
├── pom.xml                                   (Maven config)
├── SQL_INSERT_DATA.sql                       (Test data)
├── HUONG_DAN_SU_DUNG.md                      (Hướng dẫn chi tiết)
├── ARCHITECTURE_GUIDE.md                     (Architecture & patterns)
└── README.md                                 (File này)
```

---

## 🎓 Pattern Quan Trọng

### Filter Logic

```java
// RadioButton filter (chỉ ảnh hưởng VIEW)
if (rbAll.isSelected()) {
    // Merge cả 2 DB
    result = service.findAll();
} else if (rbCLB1.isSelected()) {
    // Chỉ DB1
    result = service.findByDB(1);
} else {
    // Chỉ DB2
    result = service.findByDB(2);
}

// ComboBox CLB (xác định insert vào DB nào)
String clb = cbCLB.getValue();  // "CLB1" hoặc "CLB2"
Connection targetConn = clb.equals("CLB1") ? conn1 : conn2;
service.insert(entity, targetConn);
```

### Xác Định DB Tương Ứng

| Loại        | Xác định bằng  | DB                      |
| ----------- | -------------- | ----------------------- |
| **DoiBong** | `CLB`          | CLB1→DB1, CLB2→DB2      |
| **CauThu**  | `MaDB` của đội | Phụ thuộc đội ở DB nào  |
| **TranDau** | `SanDau`       | SD1→DB1, SD2→DB2        |
| **ThamGia** | `MaTD`         | Phụ thuộc trận ở DB nào |

---

## 📞 Support

Nếu gặp vấn đề:

1. Xem file `ARCHITECTURE_GUIDE.md` (chi tiết technical)
2. Xem file `HUONG_DAN_SU_DUNG.md` (hướng dẫn sử dụng)
3. Kiểm tra console output → tìm error message
4. Verify database connection

---

## 🎉 Tính Năng Sắp Tới

- [ ] 10 câu truy vấn phân tán (Query page)
- [ ] Thêm report/statistics page
- [ ] Export dữ liệu (CSV/PDF)
- [ ] Advanced search & filter
- [ ] User role & permissions

---

**Phiên bản:** 1.0  
**Ngôn ngữ:** Java 21  
**Framework:** JavaFX 21.0.1  
**Database:** SQL Server (Distributed)  
**Cập nhật lần cuối:** 2025-11-12

---

> **Chú ý**: Đây là phiên bản MVP (Minimum Viable Product). Nhiều tính năng đang trong giai đoạn phát triển.
> Mọi feedback, issue liên hệ để cải thiện!
