# 🔧 Hướng Dẫn Phát Triển - Architecture & Implementation Guide

## 📐 Kiến Trúc 3-Layer Phân Tán

```
┌─────────────────────────────────────────────┐
│  PRESENTATION LAYER (controllers/*.java)    │
│  - LoginController                          │
│  - MainController                           │
│  - DashboardController                      │
│  - DoiBongController, CauThuController...   │
│  - DiemSoDialog (Popup nhập điểm)           │
└─────────────────────────────────────────────┘
                     ↓↓↓
┌─────────────────────────────────────────────┐
│  BUSINESS LOGIC LAYER (services/*.java)     │
│  - DoiBongService                           │
│  - CauThuService                            │
│  - TranDauService                           │
│  - ThamGiaService                           │
│  - QueryService (10 queries)                │
└─────────────────────────────────────────────┘
                     ↓↓↓
┌─────────────────────────────────────────────┐
│  DATA ACCESS LAYER (dao/*.java)             │
│  - DoiBongDAO ←→ DB1/DB2                    │
│  - CauThuDAO  ←→ DB1/DB2                    │
│  - TranDauDAO ←→ DB1/DB2                    │
│  - ThamGiaDAO ←→ DB1/DB2                    │
└─────────────────────────────────────────────┘
                     ↓↓↓
┌─────────────────────────────────────────────┐
│  DATABASE LAYER                             │
│  ┌──────────────────┐  ┌──────────────────┐ │
│  │ DB1 (CLB1/SD1)   │  │ DB2 (CLB2/SD2)   │ │
│  │ Port: 1433       │  │ Port: 1434       │ │
│  │ 4 tables x 2     │  │ 4 tables x 2     │ │
│  └──────────────────┘  └──────────────────┘ │
└─────────────────────────────────────────────┘
```

---

## 🔌 Xử Lý Phân Mảnh Dữ Liệu

### Pattern 1: **Query Đơn DB (Query 1 Side)**

**Khi nào dùng:** Dữ liệu toàn bộ nằm ở 1 DB

```java
// ❌ Sai: Hardcode connection
List<DoiBong> list = dao.findAll(conn1);

// ✅ Đúng: Xác định DB dựa vào CLB/SanDau
public List<DoiBong> findByClb(String clb) {
    Connection conn = clb.equals("CLB1") ? conn1 : conn2;
    return dao.findAll(conn);
}
```

**Các trường hợp:**

- DoiBong (phân mảnh theo CLB) → Query tương ứng DB
- CauThu (phân mảnh theo đội) → Xác định đội ở DB nào
- TranDau (phân mảnh theo SanDau) → Query SD1→DB1, SD2→DB2

---

### Pattern 2: **Query Cả 2 DB + Merge Kết Quả**

**Khi nào dùng:** Cần toàn bộ dữ liệu từ cả 2 mảnh

```java
// ✅ Chuẩn: Merge results
public List<CauThu> findAllCauThu() {
    List<CauThu> result = new ArrayList<>();

    // Query từ DB1
    List<CauThu> fromDB1 = dao.findAll(conn1);
    result.addAll(fromDB1);

    // Query từ DB2
    List<CauThu> fromDB2 = dao.findAll(conn2);
    result.addAll(fromDB2);

    return result;
}

// Hoặc dùng Stream
public List<CauThu> findAllCauThu() {
    return Stream.concat(
        dao.findAll(conn1).stream(),
        dao.findAll(conn2).stream()
    ).collect(Collectors.toList());
}
```

**Các trường hợp:**

- Câu 2: Đếm số trận của cầu thủ (cầu thủ có thể ở DB1 hoặc DB2)
- Câu 4: Vua phá lưới (cần tìm max từ cả 2 DB)
- Câu 7: Cầu thủ ghi 0 bàn (check cả 2 DB)

---

### Pattern 3: **JOIN Phức Tạp Qua 2 DB**

**Khi nào dùng:** Cần kết hợp dữ liệu từ 2 bảng ở 2 DB khác nhau

```java
// Câu 5: Tìm trận đấu theo cầu thủ & trọng tài
public List<TranDau> findByTrongTaiAndCauThu(String trongTai, String maCT) {
    List<TranDau> result = new ArrayList<>();

    // Bước 1: Tìm cầu thủ để xác định DB của cầu thủ
    CauThu ct = findCauThuByMaCT(maCT); // Sẽ tìm từ cả 2 DB

    // Bước 2: Xác định DB mà trận đấu có cầu thủ này tham gia
    Connection ctConn = ct.getClb().equals("CLB1") ? conn1 : conn2;

    // Bước 3: Tìm trận đấu có cầu thủ này trong bảng ThamGia
    List<String> matchIds = thamGiaDAO.findTranDauByMaCT(maCT, ctConn);

    // Bước 4: Lấy chi tiết trận từ bảng TranDau
    for (String maTD : matchIds) {
        TranDau tran = tranDauDAO.findByMaTD(maTD, ctConn);
        if (tran.getTrongTai().equals(trongTai)) {
            result.add(tran);
        }
    }

    return result;
}
```

---

## 🎯 Controller Implementation Pattern

### Pattern: CRUD Controller Chuẩn

```java
public class DoiBongController {

    @FXML private TextField txtMaDB, txtTenDB;
    @FXML private ComboBox<String> cbCLB;
    @FXML private TableView<DoiBong> tableDoiBong;
    @FXML private RadioButton rbAll, rbCLB1, rbCLB2;

    private DoiBongService service;
    private Connection conn1, conn2;

    @FXML
    public void initialize() {
        service = new DoiBongService();
        conn1 = DatabaseConnection.getConnection1();
        conn2 = DatabaseConnection.getConnection2();

        // Setup RadioButton listener
        rbAll.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) handleFilterChange();
        });
        rbCLB1.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) handleFilterChange();
        });
        rbCLB2.selectedProperty().addListener((obs, old, newVal) -> {
            if (newVal) handleFilterChange();
        });

        // Load initial data
        refreshTable();
    }

    // ✅ THÊM: Xác định DB dựa vào ComboBox CLB
    @FXML
    private void handleThem() {
        String maDB = txtMaDB.getText().trim();
        String tenDB = txtTenDB.getText().trim();
        String clb = cbCLB.getValue();

        // Validation
        if (maDB.isEmpty() || tenDB.isEmpty() || clb == null) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        // Kiểm tra trùng khóa từ CẢ 2 DB
        if (service.existsInDB1(maDB) || service.existsInDB2(maDB)) {
            showError("Mã đội bóng đã tồn tại!");
            return;
        }

        try {
            DoiBong doiBong = new DoiBong(maDB, tenDB, clb);

            // Xác định DB dựa vào ComboBox CLB
            Connection targetConn = clb.equals("CLB1") ? conn1 : conn2;

            // Insert vào DB tương ứng
            service.insert(doiBong, targetConn);

            showSuccess("Thêm đội bóng thành công!");

            // Auto switch filter về "Tất cả" để thấy record mới
            rbAll.setSelected(true);
            refreshTable();

        } catch (Exception e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    // ✅ XÓA: Xác định DB nơi record được lưu
    @FXML
    private void handleXoa() {
        DoiBong selected = tableDoiBong.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn đội bóng cần xóa!");
            return;
        }

        try {
            // Xác định DB nơi đội được lưu (dựa vào CLB)
            Connection targetConn = selected.getClb().equals("CLB1") ? conn1 : conn2;

            service.delete(selected.getMaDB(), targetConn);

            showSuccess("Xóa thành công!");
            refreshTable();

        } catch (Exception e) {
            showError("Lỗi xóa: " + e.getMessage());
        }
    }

    // ✅ FILTER XEM (RadioButton CHỈ ảnh hưởng VIEW)
    @FXML
    private void handleFilterChange() {
        List<DoiBong> data;

        if (rbAll.isSelected()) {
            // Merge cả 2 DB
            data = new ArrayList<>();
            data.addAll(service.findAll(conn1));
            data.addAll(service.findAll(conn2));
        } else if (rbCLB1.isSelected()) {
            // Chỉ DB1
            data = service.findAll(conn1);
        } else {
            // Chỉ DB2
            data = service.findAll(conn2);
        }

        tableDoiBong.getItems().setAll(data);
    }

    private void refreshTable() {
        if (rbAll.isSelected()) {
            handleFilterChange();
        } else if (rbCLB1.isSelected()) {
            handleFilterChange();
        } else {
            handleFilterChange();
        }
    }
}
```

---

## 🗄️ DAO Implementation Pattern

```java
public class DoiBongDAO {

    // ✅ Pattern: Method nhận Connection parameter
    public List<DoiBong> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM DoiBong";
        List<DoiBong> result = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }

        return result;
    }

    // ✅ Pattern: Query lọc theo điều kiện
    public List<DoiBong> findByClb(String clb, Connection conn) throws SQLException {
        String sql = "SELECT * FROM DoiBong WHERE CLB = ?";
        List<DoiBong> result = new ArrayList<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clb);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }

        return result;
    }

    // ✅ Pattern: Insert
    public void insert(DoiBong db, Connection conn) throws SQLException {
        String sql = "INSERT INTO DoiBong (MaDB, TenDB, CLB) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, db.getMaDB());
            pstmt.setString(2, db.getTenDB());
            pstmt.setString(3, db.getClb());

            pstmt.executeUpdate();
        }
    }

    // ✅ Pattern: Update
    public void update(DoiBong db, Connection conn) throws SQLException {
        String sql = "UPDATE DoiBong SET TenDB = ?, CLB = ? WHERE MaDB = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, db.getTenDB());
            pstmt.setString(2, db.getClb());
            pstmt.setString(3, db.getMaDB());

            pstmt.executeUpdate();
        }
    }

    // ✅ Pattern: Delete
    public void delete(String maDB, Connection conn) throws SQLException {
        String sql = "DELETE FROM DoiBong WHERE MaDB = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maDB);
            pstmt.executeUpdate();
        }
    }

    private DoiBong mapRow(ResultSet rs) throws SQLException {
        return new DoiBong(
            rs.getString("MaDB"),
            rs.getString("TenDB"),
            rs.getString("CLB")
        );
    }
}
```

---

## 📊 Service Layer Pattern

```java
public class DoiBongService {

    private DoiBongDAO dao = new DoiBongDAO();
    private Connection conn1 = DatabaseConnection.getConnection1();
    private Connection conn2 = DatabaseConnection.getConnection2();

    // ✅ Business logic: Kiểm tra toàn bộ 2 DB
    public boolean existsInBothDB(String maDB) {
        try {
            return dao.findByMaDB(maDB, conn1) != null ||
                   dao.findByMaDB(maDB, conn2) != null;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Business logic: Lấy toàn bộ dữ liệu
    public List<DoiBong> findAll() {
        List<DoiBong> result = new ArrayList<>();
        try {
            result.addAll(dao.findAll(conn1));
            result.addAll(dao.findAll(conn2));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ✅ Business logic: Thêm vào DB tương ứng
    public void insert(DoiBong db, Connection conn) {
        try {
            dao.insert(db, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi insert: " + e.getMessage());
        }
    }
}
```

---

## 🔐 Database Connection Utility

```java
public class DatabaseConnection {

    private static Connection conn1; // DB1: CLB1/SD1
    private static Connection conn2; // DB2: CLB2/SD2

    // ✅ Pattern: Lazy initialization với error handling
    public static Connection getConnection1() {
        if (conn1 == null) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                conn1 = DriverManager.getConnection(
                    "jdbc:sqlserver://localhost:1433;" +
                    "databaseName=QL_BongDa_CLB1_SD1;" +
                    "user=sa;password=;encrypt=true;trustServerCertificate=true"
                );

                System.out.println("✅ Connected to Database 1");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ JDBC Driver not found: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("❌ Connection failed (DB1): " + e.getMessage());
            }
        }

        return conn1;
    }

    public static Connection getConnection2() {
        if (conn2 == null) {
            try {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

                conn2 = DriverManager.getConnection(
                    "jdbc:sqlserver://localhost:1434;" +
                    "databaseName=QL_BongDa_CLB2_SD2;" +
                    "user=sa;password=;encrypt=true;trustServerCertificate=true"
                );

                System.out.println("✅ Connected to Database 2");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ JDBC Driver not found: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("❌ Connection failed (DB2): " + e.getMessage());
            }
        }

        return conn2;
    }

    // ✅ Pattern: Đóng connection
    public static void closeConnections() {
        try {
            if (conn1 != null && !conn1.isClosed()) {
                conn1.close();
                System.out.println("Database 1 closed");
            }
            if (conn2 != null && !conn2.isClosed()) {
                conn2.close();
                System.out.println("Database 2 closed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

---

## ✅ Checklist Khi Thêm Tính Năng Mới

- [ ] **Phân tích phân mảnh:** Tables nào cần query? Từ DB nào?
- [ ] **Xác định pattern:** 1 DB hay 2 DB? Merge hay không?
- [ ] **Implement DAO:** Thêm methods cần thiết
- [ ] **Implement Service:** Business logic + merge nếu cần
- [ ] **Implement Controller:** UI + xử lý user input
- [ ] **Test:** Kiểm tra cả 2 DB, edge cases
- [ ] **Error handling:** Try-catch + show user-friendly messages
- [ ] **Update documentation:** Comment code + update guide

---

## 🚀 Quick Reference

### Kiểm tra dữ liệu ở DB nào?

| Entity  | Phân mảnh theo | DB                   | SQL WHERE              |
| ------- | -------------- | -------------------- | ---------------------- |
| DoiBong | CLB            | DB1 nếu CLB='CLB1'   | `CLB = 'CLB1'`         |
| CauThu  | Đội (MaDB)     | Phụ thuộc đội        | `MaDB IN (DB01, DB02)` |
| TranDau | Sân (SanDau)   | DB1 nếu SanDau='SD1' | `SanDau = 'SD1'`       |
| ThamGia | Trận (MaTD)    | Phụ thuộc trận       | `MaTD IN (...)`        |

### Khi nào xác định DB?

1. **Khi Thêm/Sửa/Xóa:** Dựa vào ComboBox (CLB/SanDau)
2. **Khi Tìm kiếm:** Xác định từ ID hoặc điều kiện WHERE
3. **Khi Filter View:** RadioButton chỉ ảnh hưởng hiển thị, không ảnh hưởng CRUD

---

**Last updated:** 2025-11-12  
**For:** QlDoiBong Project v1.0-SNAPSHOT
