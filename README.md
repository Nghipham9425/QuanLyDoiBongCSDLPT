# 🚀 Hệ Thống Quản Lý Đội Bóng - JavaFX

## 📋 Mô tả
Ứng dụng quản lý đội bóng sử dụng JavaFX với cơ sở dữ liệu SQL Server phân tán.

## 🗂️ Cấu trúc dự án

```
QlDoiBong/
├── src/main/java/org/football/
│   ├── models/              # Entity classes
│   │   ├── DoiBong.java    # Đội bóng (MaDB, TenDB, CLB)
│   │   ├── CauThu.java     # Cầu thủ (MaCT, HoTen, ViTri, MaDB)
│   │   ├── TranDau.java    # Trận đấu (MaTD, MaDB1, MaDB2, TrongTai, SanDau)
│   │   └── ThamGia.java    # Tham gia (MaTD, MaCT, SoTrai)
│   │
│   ├── dao/                 # Data Access Objects (sẽ triển khai tiếp)
│   ├── services/            # Business Logic (sẽ triển khai tiếp)
│   ├── controllers/         # JavaFX Controllers (sẽ triển khai tiếp)
│   │
│   ├── utils/              
│   │   ├── DatabaseConnection.java    # Quản lý kết nối DB
│   │   └── FragmentManager.java       # Quản lý phân mảnh
│   │
│   ├── App.java            # Main JavaFX Application
│   └── Main.java           # Entry point
│
└── pom.xml                 # Maven dependencies

```

## 🗄️ Schema Database

### DoiBong (Đội bóng)
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| MaDB | VARCHAR | Mã đội bóng (PK) |
| TenDB | NVARCHAR | Tên đội bóng |
| CLB | VARCHAR | Câu lạc bộ (CLB1, CLB2) |

**Phân mảnh:** CLB1 → Instance 1, CLB2 → Instance 2

### CauThu (Cầu thủ)
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| MaCT | VARCHAR | Mã cầu thủ (PK) |
| HoTen | NVARCHAR | Họ tên |
| ViTri | NVARCHAR | Vị trí (Tiền đạo, Hậu vệ, ...) |
| MaDB | VARCHAR | Mã đội bóng (FK) |

**Phân mảnh:** Theo MaDB của đội bóng

### TranDau (Trận đấu)
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| MaTD | VARCHAR | Mã trận đấu (PK) |
| MaDB1 | VARCHAR | Đội 1 (FK) |
| MaDB2 | VARCHAR | Đội 2 (FK) |
| TrongTai | NVARCHAR | Trọng tài |
| SanDau | VARCHAR | Sân đấu (SD1, SD2) |

**Phân mảnh:** SD1 → Instance 1, SD2 → Instance 2

### ThamGia (Tham gia)
| Cột | Kiểu | Mô tả |
|-----|------|-------|
| MaTD | VARCHAR | Mã trận đấu (FK, PK) |
| MaCT | VARCHAR | Mã cầu thủ (FK, PK) |
| SoTrai | INT | Số bàn thắng |

**Phân mảnh:** Theo MaTD của trận đấu

## 🎯 10 Câu truy vấn phân tán

1. **Câu 1:** Tìm cầu thủ theo câu lạc bộ
2. **Câu 2:** Đếm số trận đấu của cầu thủ
3. **Câu 3:** Đếm trận hòa theo sân đấu
4. **Câu 4:** Tìm vua phá lưới
5. **Câu 5:** Lọc trận theo cầu thủ và trọng tài
6. **Câu 6:** Kiểm tra 2 cầu thủ cùng CLB
7. **Câu 7:** Cầu thủ tham gia nhưng 0 bàn
8. **Câu 8:** Cầu thủ tích cực (3+ trận)
9. **Câu 9:** Tổng bàn thắng của đội
10. **Câu 10:** Cầu thủ chưa tham gia trận nào

## ⚙️ Cấu hình

### Database Connection (DatabaseConnection.java)
```java
// Instance 1 (CLB1, SD1)
DB1_URL = "jdbc:sqlserver://localhost:1433;databaseName=QL_BongDa"
DB1_USER = "sa"
DB1_PASSWORD = "123"

// Instance 2 (CLB2, SD2)
DB2_URL = "jdbc:sqlserver://localhost:1434;databaseName=QL_BongDa"
DB2_USER = "sa"
DB2_PASSWORD = "123"
```

**Lưu ý:** Cập nhật password và port phù hợp với SQL Server của bạn!

## 🚀 Chạy ứng dụng

### Yêu cầu
- Java 21+
- Maven 3.6+
- SQL Server 2019+
- JavaFX SDK 21+

### Các bước chạy

1. **Cài đặt dependencies:**
```bash
mvn clean install
```

2. **Chạy ứng dụng:**
```bash
mvn javafx:run
```

Hoặc chạy từ Main class:
```bash
java -jar target/QlDoiBong-1.0-SNAPSHOT.jar
```

## 📦 Dependencies

```xml
<!-- JavaFX -->
- javafx-controls: 21.0.1
- javafx-fxml: 21.0.1

<!-- SQL Server -->
- mssql-jdbc: 12.4.2.jre11

<!-- Lombok (Optional) -->
- lombok: 1.18.30
```

## 🎨 Giao diện

- **Main Menu:** 10 buttons tương ứng 10 câu truy vấn
- **Màu sắc:** Modern, professional (Blue theme)
- **Responsive:** Tự động điều chỉnh kích thước

## 📝 Tiếp theo cần làm

- [ ] Implement DAO classes (DoiBongDAO, CauThuDAO, ...)
- [ ] Implement Service layer
- [ ] Tạo FXML cho từng màn hình query
- [ ] Implement Controllers cho từng query
- [ ] Xử lý logic phân mảnh trong queries
- [ ] Thêm validation và error handling
- [ ] Thêm export/import data
- [ ] Thêm reports và statistics

## 👨‍💻 Tác giả
- **Dự án:** Quản lý đội bóng - Database phân tán
- **Framework:** JavaFX + SQL Server
- **Architecture:** Clean Code, Layered Architecture

---
Made with ⚽ and ☕

