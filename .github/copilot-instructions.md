# Hướng dẫn AI Agent - Hệ Thống Quản Lý Đội Bóng (Distributed Database)

## 🎯 Tổng quan kiến trúc

Đây là ứng dụng **JavaFX Desktop** quản lý đội bóng với **cơ sở dữ liệu phân tán (Distributed Database)** trên SQL Server. Dự án thuộc đồ án môn **Cơ Sở Dữ Liệu Phân Tán**.

### Kiến trúc 3-layer phân tán:
```
JavaFX UI (controllers/) 
    ↓
Business Logic (services/)
    ↓
Data Access Layer (dao/) → [DB1: CLB1_SD1] + [DB2: CLB2_SD2]
```

## 🗄️ Chiến lược phân mảnh dữ liệu (QUAN TRỌNG!)

Hệ thống sử dụng **2 SQL Server instances** với phân mảnh ngang (horizontal fragmentation):

### Database 1: `QL_BongDa_CLB1_SD1` (Server: Winter hoặc localhost:1433)
- **DoiBong:** `WHERE CLB = 'CLB1'`
- **CauThu:** Phân mảnh theo `MaDB` của đội thuộc CLB1
- **TranDau:** `WHERE SanDau = 'SD1'`
- **ThamGia:** Phân mảnh theo `MaTD` của trận đấu tại SD1

### Database 2: `QL_BongDa_CLB2_SD2` (Server: sv1 hoặc localhost:1434)
- **DoiBong:** `WHERE CLB = 'CLB2'`
- **CauThu:** Phân mảnh theo `MaDB` của đội thuộc CLB2
- **TranDau:** `WHERE SanDau = 'SD2'`
- **ThamGia:** Phân mảnh theo `MaTD` của trận đấu tại SD2

**LƯU Ý:** 
- CLB1 có thể có nhiều đội (ví dụ: DB01, DB02) - đại diện cho giải đấu
- CLB2 tương tự (ví dụ: DB03, DB04)
- Mỗi trận đấu chỉ diễn ra tại 1 sân (SD1 hoặc SD2)

## 📋 Schema Models (package `models/`)

4 entity classes tương ứng với global schema:
- `DoiBong`: (MaDB*, TenDB, CLB) - *khóa chính
- `CauThu`: (MaCT*, HoTen, ViTri, MaDB#) - #khóa ngoại
- `TranDau`: (MaTD*, MaDB1#, MaDB2#, TrongTai, SanDau)
- `ThamGia`: (MaTD*, MaCT*, SoTrai) - *khóa chính kép

**Convention:** 
- Sử dụng **camelCase** cho tên biến Java
- Tên cột DB viết **không dấu, PascalCase** (MaDB, HoTen, SoTrai)
- Không dùng Lombok (code POJO thủ công)

## 🔌 Kết nối Database phân tán

File `utils/DatabaseConnection.java` quản lý **2 connections đồng thời**:

```java
// Pattern cần implement:
public class DatabaseConnection {
    private static Connection conn1; // CLB1_SD1
    private static Connection conn2; // CLB2_SD2
    
    public static Connection getConnection1() { ... }
    public static Connection getConnection2() { ... }
}
```

**Quan trọng:** Mọi query phân tán phải:
1. Xác định dữ liệu nằm ở DB nào (dựa vào CLB hoặc SanDau)
2. Kết nối đúng instance
3. Merge kết quả nếu cần query cả 2 DB

## 🎯 10 câu truy vấn bắt buộc (services/)

Tất cả service methods phải xử lý **distributed queries**:

### Câu 1-2: Query trên 1 hoặc 2 DB
- **Câu 1:** Tìm cầu thủ theo CLB → Query DB tương ứng với CLB
- **Câu 2:** Đếm trận của cầu thủ → JOIN CauThu + ThamGia (có thể cần 2 DB)

### Câu 3-4: Aggregation phân tán
- **Câu 3:** Trận hòa theo sân → Query DB theo SanDau, tính SUM(SoTrai) cho mỗi đội
- **Câu 4:** Vua phá lưới → UNION kết quả từ cả 2 DB, tìm MAX(SoTrai)

### Câu 5-10: JOIN phức tạp
- **Câu 5-6:** JOIN nhiều bảng qua 2 DB
- **Câu 7-8:** Filter với điều kiện (SoTrai = 0, COUNT >= 3)
- **Câu 9:** Tổng bàn thắng đội → JOIN TranDau + ThamGia + CauThu
- **Câu 10:** LEFT JOIN để tìm cầu thủ chưa tham gia

**Pattern xử lý:**
```java
// Ví dụ Câu 4 - Vua phá lưới
public List<CauThu> findTopScorer() {
    List<CauThu> fromDB1 = queryDB1();
    List<CauThu> fromDB2 = queryDB2();
    return mergeAndFindMax(fromDB1, fromDB2);
}
```

## 🎨 Giao diện JavaFX - Sidebar Navigation (controllers/ + resources/fxml/)

### Kiến trúc UI - Multi-Page với Sidebar:
Giao diện dạng **Dashboard** với sidebar điều hướng giữa các trang:

```
┌─────────────────────────────────────────────────────────────────────┐
│  🏆 HỆ THỐNG QUẢN LÝ ĐỘI BÓNG - PHÂN TÁN                            │
├──────────┬──────────────────────────────────────────────────────────┤
│ SIDEBAR  │  CONTENT AREA (Dynamic Pages)                            │
│          │                                                          │
│ 🏠 Tổng   │  ┌────────────────────────────────────────────────┐    │
│    quan  │  │  [Page content thay đổi]                       │    │
│          │  │                                                │    │
│ 👥 Đội    │  │  - Login                                       │    │
│    bóng  │  │  - CRUD Đội bóng                               │    │
│          │  │  - CRUD Cầu thủ                                │    │
│ ⚽ Cầu    │  │  - CRUD Trận đấu                               │    │
│    thủ   │  │  - CRUD Tham gia                               │    │
│          │  │  - 10 Queries                                  │    │
│ 🏟️ Trận   │  │                                                │    │
│    đấu   │  └────────────────────────────────────────────────┘    │
│          │                                                          │
│ 📊 Thống │                                                          │
│    kê    │                                                          │
│          │                                                          │
│ 🔍 Truy  │                                                          │
│    vấn   │                                                          │
└──────────┴──────────────────────────────────────────────────────────┘
```

### 📋 Mockup chi tiết từng màn hình:

---

#### **1. CauThuManagement.fxml** - CRUD + Filter mảnh

```
┌────────────────────────────────────────────────────────────────┐
│  ⚽ QUẢN LÝ CẦU THỦ                                             │
├────────────────────────────────────────────────────────────────┤
│  THÊM MỚI CẦU THỦ                                             │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Mã CT: [________]  CLB: [CLB1 ▼]  Đội: [DB01 ▼]         │ │
│  │ Họ tên: [___________________]  Vị trí: [Tiền đạo ▼]     │ │
│  │ [➕ Thêm] [✏️ Sửa] [🗑️ Xóa] [🔍 Tìm kiếm]               │ │
│  └──────────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────┤
│  🗄️ HIỂN THỊ:  ● Tất cả (Merge)  ○ Chỉ DB1 (CLB1)  ○ Chỉ DB2 │  ← NEW!
├────────────────────────────────────────────────────────────────┤
│  DANH SÁCH CẦU THỦ                                             │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ ☑ │ MaCT │ Họ Tên    │ Vị trí   │ Đội  │ CLB  │ Mảnh  │ │
│  ├──────────────────────────────────────────────────────────┤ │
│  │ ☐ │ CT01 │ Nguyễn A  │ Tiền đạo │ DB01 │ CLB1 │ DB1   │ │
│  │ ☐ │ CT02 │ Trần B    │ Hậu vệ   │ DB02 │ CLB1 │ DB1   │ │
│  │ ☐ │ CT03 │ Lê C      │ Thủ môn  │ DB03 │ CLB2 │ DB2   │ │
│  │ ☐ │ CT04 │ Phạm D    │ Tiền vệ  │ DB04 │ CLB2 │ DB2   │ │
│  └──────────────────────────────────────────────────────────┘ │
│  📊 Tổng: 4 cầu thủ (2 từ CLB1/DB1, 2 từ CLB2/DB2)            │
└────────────────────────────────────────────────────────────────┘
```

**Xử lý filter mảnh:**
```java
@FXML private RadioButton rbAll, rbDB1, rbDB2;

@FXML void handleFilterChange() {
    List<CauThu> result;
    
    if (rbAll.isSelected()) {
        // Merge cả 2 mảnh
        List<CauThu> fromDB1 = cauThuDAO.findAll(conn1);
        List<CauThu> fromDB2 = cauThuDAO.findAll(conn2);
        result = new ArrayList<>();
        result.addAll(fromDB1);
        result.addAll(fromDB2);
    } else if (rbDB1.isSelected()) {
        // Chỉ từ DB1
        result = cauThuDAO.findAll(conn1);
    } else {
        // Chỉ từ DB2
        result = cauThuDAO.findAll(conn2);
    }
    
    tableView.getItems().setAll(result);
}
```

---

#### **2. QueryManagement.fxml** - 10 câu query trong TabPane

```
┌────────────────────────────────────────────────────────────────┐
│  🔍 TRUY VẤN PHÂN TÁN                                          │
├────────────────────────────────────────────────────────────────┤
│  [Q1: CT theo CLB] [Q2: Số trận] [Q3: Trận hòa] ... [Q10]    │
├────────────────────────────────────────────────────────────────┤
│  ┌─ Câu 1: Tìm cầu thủ theo CLB ──────────────────────────┐  │
│  │                                                          │  │
│  │  Nhập CLB: [CLB1    ]  [🔍 Tìm cầu thủ]                 │  │
│  │                                                          │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │ MaCT │ Họ Tên      │ Vị trí    │ Đội   │          │ │  │
│  │  ├────────────────────────────────────────────────────┤ │  │
│  │  │ CT01 │ Nguyễn A    │ Tiền đạo  │ DB01  │          │ │  │
│  │  │ CT02 │ Trần B      │ Hậu vệ    │ DB02  │          │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  │  📊 Kết quả: 2 cầu thủ (từ mảnh DB1)                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────────┘
```

**Bảng mapping Input/Output cho 10 câu:**

| Câu | Input Fields | Button | Output | Mảnh DB |
|-----|-------------|--------|--------|---------|
| **Q1** | `TextField txtCLB` | 🔍 Tìm cầu thủ | TableView: MaCT, HoTen, ViTri, MaDB | DB1 hoặc DB2 |
| **Q2** | `TextField txtHoTen` | 📊 Đếm trận | Label: "Số trận: X" | DB1 + DB2 merge |
| **Q3** | `TextField txtSanDau` | 🏟️ Tính trận hòa | Label: "Số trận hòa: X" | DB1 hoặc DB2 |
| **Q4** | (Không) | 👑 Vua phá lưới | TableView: MaCT, HoTen, TongBanThang | DB1 + DB2 merge |
| **Q5** | `TextField txtHoTen`, `TextField txtTrongTai` | 🔍 Tìm trận | TableView: MaTD, MaDB1, MaDB2, SanDau | DB1 + DB2 |
| **Q6** | `TextField txtHoTen1`, `TextField txtHoTen2` | 🔄 So sánh CLB | Label: "✅ Cùng CLB" / "❌ Khác CLB" | DB1 + DB2 |
| **Q7** | (Không) | 0️⃣ CT ghi 0 bàn | TableView: MaCT, HoTen | DB1 + DB2 merge |
| **Q8** | (Không) | ⭐ CT ≥3 trận | TableView: MaCT, HoTen, SoTran | DB1 + DB2 merge |
| **Q9** | `TextField txtMaDB` | 🎯 Tổng bàn đội | Label: "Tổng bàn thắng: X" | DB1 hoặc DB2 |
| **Q10** | (Không) | 💤 CT chưa đá | TableView: MaCT, HoTen, MaDB | DB1 + DB2 merge |

---

#### **3. DoiBongManagement.fxml** - CRUD Đội bóng + Filter

```
┌────────────────────────────────────────────────────────────────┐
│  👥 QUẢN LÝ ĐỘI BÓNG                                           │
├────────────────────────────────────────────────────────────────┤
│  Mã đội: [____]  Tên đội: [______________]  CLB: [CLB1 ▼]    │
│  [➕ Thêm] [✏️ Sửa] [🗑️ Xóa] [🔍 Tìm]                         │
├────────────────────────────────────────────────────────────────┤
│  🗄️ Hiển thị:  ● Tất cả  ○ CLB1 (DB1)  ○ CLB2 (DB2)          │  ← NEW!
├────────────────────────────────────────────────────────────────┤
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ MaDB │ Tên Đội         │ CLB  │ Mảnh │ Số CT │          │ │
│  ├──────────────────────────────────────────────────────────┤ │
│  │ DB01 │ Barcelona A     │ CLB1 │ DB1  │ 15    │          │ │
│  │ DB02 │ Barcelona B     │ CLB1 │ DB1  │ 12    │          │ │
│  │ DB03 │ Real Madrid A   │ CLB2 │ DB2  │ 18    │          │ │
│  └──────────────────────────────────────────────────────────┘ │
│  📊 Tổng: 3 đội (2 từ DB1, 1 từ DB2)                          │
└────────────────────────────────────────────────────────────────┘
```

---

#### **4. TranDauManagement.fxml** - CRUD Trận đấu + Nhập điểm

**🎯 LUỒNG XỬ LÝ ĐÚNG:**
1. **Tạo trận đấu** → Chỉ lưu bảng `TranDau` (MaTD, MaDB1, MaDB2, TrongTai, SanDau)
2. **Click [📝 Điểm]** → Popup nhập điểm
3. **Chọn cầu thủ tham gia + nhập số bàn** → Tự động lưu vào bảng `ThamGia`
4. **Kết quả:** Dữ liệu được tạo theo thứ tự logic: TranDau trước → ThamGia sau

```
┌────────────────────────────────────────────────────────────────┐
│  🏟️ QUẢN LÝ TRẬN ĐẤU & ĐIỂM SỐ                                │
├────────────────────────────────────────────────────────────────┤
│  TẠO TRẬN ĐẤU MỚI                                              │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Mã trận: [____]  Đội 1: [DB01 ▼]  Đội 2: [DB02 ▼]      │ │
│  │ Trọng tài: [__________]  Sân đấu: [SD1 ▼]              │ │
│  │ [➕ Thêm] [✏️ Sửa] [🗑️ Xóa]                              │ │
│  │                                                          │ │
│  │ ⚠️ CHÚ Ý: Chỉ tạo khung trận đấu, chưa có điểm số!      │ │
│  │          Click [📝 Điểm] sau khi tạo để nhập kết quả    │ │
│  └──────────────────────────────────────────────────────────┘ │
├────────────────────────────────────────────────────────────────┤
│  🗄️ Hiển thị:  ● Tất cả  ○ SD1 (DB1)  ○ SD2 (DB2)            │
├────────────────────────────────────────────────────────────────┤
│  DANH SÁCH TRẬN ĐẤU                                            │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ MaTD │ Đội 1 vs Đội 2  │ Trọng tài │ Sân  │ [Action]  │ │
│  ├──────────────────────────────────────────────────────────┤ │
│  │ TD01 │ DB01 vs DB02    │ Nguyễn X  │ SD1  │ [📝 Điểm] │ │  ← Bước 2
│  │ TD02 │ DB03 vs DB04    │ Trần Y    │ SD2  │ [📝 Điểm] │ │
│  └──────────────────────────────────────────────────────────┘ │
│  📊 Tổng: 2 trận (1 tại SD1/DB1, 1 tại SD2/DB2)              │
└────────────────────────────────────────────────────────────────┘

**Bước 2: Click [📝 Điểm] → Mở popup nhập điểm (Tự động tạo bảng ThamGia):**

┌─────────────────────────────────────────────┐
│  📝 NHẬP ĐIỂM - Trần TD01 (Bước 3)          │
│  DB01 (Barcelona A) vs DB02 (Barcelona B)   │
│  Sân: SD1 | Trọng tài: Nguyễn X             │
├─────────────────────────────────────────────┤
│  💡 Hướng dẫn: Tick cầu thủ tham gia, nhập  │
│     số bàn thắng. Hệ thống tự động lưu vào  │
│     bảng ThamGia khi click [💾 Lưu]         │
├─────────────────────────────────────────────┤
│  ĐỘI DB01 (Barcelona A):                   │
│  ┌───────────────────────────────────────┐ │
│  │ ☑ Cầu thủ       │ Số bàn │          │ │
│  ├───────────────────────────────────────┤ │
│  │ ☑ CT01 (Nguyễn A) │ [2]  │  🎯 Ra sân│ │
│  │ ☑ CT02 (Trần B)   │ [0]  │  🎯 Ra sân│ │
│  │ ☐ CT05 (Hoàng E)  │ [_]  │  � Dự bị│ │
│  └───────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│  ĐỘI DB02 (Barcelona B):                   │
│  ┌───────────────────────────────────────┐ │
│  │ ☑ Cầu thủ       │ Số bàn │          │ │
│  ├───────────────────────────────────────┤ │
│  │ ☑ CT03 (Lê C)     │ [1]  │  🎯 Ra sân│ │
│  │ ☑ CT04 (Phạm D)   │ [0]  │  🎯 Ra sân│ │
│  │ ☐ CT06 (Minh F)   │ [_]  │  � Dự bị│ │
│  └───────────────────────────────────────┘ │
├─────────────────────────────────────────────┤
│  📊 Tỷ số tạm tính: DB01 [2] - [1] DB02    │
│  🎯 Cầu thủ tham gia: 4/6 cầu thủ          │
│                                             │
│  [💾 Lưu tất cả vào bảng ThamGia] [❌ Đóng] │
│       ↑                                     │
│  INSERT INTO ThamGia (MaTD, MaCT, SoTrai)   │
│  với các cầu thủ được tick ☑                │
└─────────────────────────────────────────────┘
```

**💡 LUỒNG XỬ LÝ ĐÚNG - TỪ TRẬN ĐẤU ĐẾN BẢNG THAM GIA:**

```java
// === BƯỚC 1: Tạo trận đấu (TranDauController.java) ===
@FXML void handleThem() {
    // Chỉ lưu thông tin trận đấu, CHƯA có điểm số
    TranDau tran = new TranDau(
        txtMaTD.getText(),
        cbMaDB1.getValue(),
        cbMaDB2.getValue(),
        txtTrongTai.getText(),
        cbSanDau.getValue()
    );
    
    String sanDau = cbSanDau.getValue();
    Connection conn = sanDau.equals("SD1") ? conn1 : conn2;
    
    // Insert vào bảng TranDau
    tranDauDAO.insert(tran, conn);
    
    showInfo("✅ Tạo trận đấu thành công!\n" +
             "💡 Bây giờ click [📝 Điểm] để nhập kết quả trận đấu");
    
    refreshTable();
}

// === BƯỚC 2: Click nút [📝 Điểm] → Mở popup ===
@FXML void handleNhapDiem() {
    TranDau tran = tableView.getSelectionModel().getSelectedItem();
    
    if (tran == null) {
        showError("❌ Chọn trận đấu cần nhập điểm!");
        return;
    }
    
    // Xác định DB dựa vào SanDau
    Connection conn = tran.getSanDau().equals("SD1") ? conn1 : conn2;
    
    // Load TOÀN BỘ cầu thủ của 2 đội (chưa filter)
    List<CauThu> doiNha = cauThuService.findByMaDB(tran.getMaDB1());
    List<CauThu> doiKhach = cauThuService.findByMaDB(tran.getMaDB2());
    
    // Mở dialog - User sẽ chọn ai tham gia + nhập bàn thắng
    DiemSoDialog dialog = new DiemSoDialog(tran, doiNha, doiKhach, conn);
    dialog.showAndWait();
    
    // Refresh sau khi đóng (bảng ThamGia đã tự động được tạo)
    refreshTable();
}

// === BƯỚC 3: Trong DiemSoDialog - Tự động tạo bảng ThamGia ===
public class DiemSoDialog extends Stage {
    private TranDau tran;
    private Connection conn;
    private List<PlayerRow> playerRows = new ArrayList<>();
    
    // Class để quản lý mỗi dòng cầu thủ
    class PlayerRow {
        CheckBox checkBox;    // Tick = tham gia
        CauThu cauThu;
        TextField txtSoBan;   // Số bàn thắng
        
        PlayerRow(CauThu ct) {
            this.cauThu = ct;
            this.checkBox = new CheckBox(ct.getHoTen());
            this.txtSoBan = new TextField("0");
            this.txtSoBan.setDisable(true);  // Disable mặc định
            
            // Khi tick → enable TextField
            checkBox.selectedProperty().addListener((obs, old, newVal) -> {
                txtSoBan.setDisable(!newVal);
                if (!newVal) txtSoBan.setText("0");  // Uncheck = reset về 0
                updateTyso();
            });
            
            txtSoBan.textProperty().addListener((obs, old, newVal) -> {
                updateTyso();  // Cập nhật tỷ số real-time
            });
        }
    }
    
    public DiemSoDialog(TranDau tran, List<CauThu> doi1, List<CauThu> doi2, Connection conn) {
        this.tran = tran;
        this.conn = conn;
        
        // Tạo UI với CheckBox + TextField cho mỗi cầu thủ
        VBox root = new VBox(15);
        
        // Đội 1
        root.getChildren().add(new Label("ĐỘI " + tran.getMaDB1()));
        for (CauThu ct : doi1) {
            PlayerRow row = new PlayerRow(ct);
            playerRows.add(row);
            
            HBox hbox = new HBox(10, row.checkBox, row.txtSoBan);
            root.getChildren().add(hbox);
        }
        
        // Đội 2
        root.getChildren().add(new Label("ĐỘI " + tran.getMaDB2()));
        for (CauThu ct : doi2) {
            PlayerRow row = new PlayerRow(ct);
            playerRows.add(row);
            
            HBox hbox = new HBox(10, row.checkBox, row.txtSoBan);
            root.getChildren().add(hbox);
        }
        
        // Nút lưu
        Button btnLuu = new Button("💾 Lưu tất cả vào bảng ThamGia");
        btnLuu.setOnAction(e -> luuTatCa());
        
        root.getChildren().add(btnLuu);
        setScene(new Scene(root, 500, 600));
    }
    
    // === BƯỚC 4: Lưu vào bảng ThamGia ===
    void luuTatCa() {
        try {
            ThamGiaDAO dao = new ThamGiaDAO();
            
            // Xóa dữ liệu cũ của trận này (nếu có)
            dao.deleteByMaTD(tran.getMaTD(), conn);
            
            // Chỉ lưu các cầu thủ được TICK ☑
            for (PlayerRow row : playerRows) {
                if (row.checkBox.isSelected()) {  // ← CHỈ lưu khi tick
                    int soBan = Integer.parseInt(row.txtSoBan.getText());
                    
                    ThamGia tg = new ThamGia(
                        tran.getMaTD(),
                        row.cauThu.getMaCT(),
                        soBan
                    );
                    
                    // INSERT vào bảng ThamGia
                    dao.insert(tg, conn);
                }
            }
            
            showInfo("✅ Đã lưu kết quả trận đấu vào bảng ThamGia!");
            close();
            
        } catch (Exception ex) {
            showError("❌ Lỗi lưu dữ liệu: " + ex.getMessage());
        }
    }
    
    void updateTyso() {
        // Tính tỷ số real-time từ các TextField
        // (Code tính tổng bàn thắng của mỗi đội)
    }
}
```

**🎯 KẾT QUẢ:**
- Bảng `TranDau`: TD01, DB01, DB02, Nguyễn X, SD1
- Bảng `ThamGia` (tự động được tạo):
  - (TD01, CT01, 2)  ← CT01 được tick, ghi 2 bàn
  - (TD01, CT02, 0)  ← CT02 được tick, không ghi bàn
  - (TD01, CT03, 1)  ← CT03 được tick, ghi 1 bàn
  - (TD01, CT04, 0)  ← CT04 được tick, không ghi bàn
  - (CT05, CT06 KHÔNG có trong bảng vì không được tick)

**✅ LỢI ÍCH LUỒNG NÀY:**
1. **Logic tự nhiên:** Tạo trận → Nhập kết quả (theo đúng thứ tự thực tế)
2. **Tự động:** User không cần thao tác với bảng ThamGia trực tiếp
3. **Linh hoạt:** Có thể sửa điểm số sau (click lại [📝 Điểm])
4. **Dữ liệu sạch:** Chỉ lưu cầu thủ thực sự tham gia (tick ☑)
```

---

#### **5. Xem Chi Tiết Trận Đấu (ThamGia) - KHÔNG CẦN MÀN HÌNH RIÊNG**

**💡 ThamGia được hiển thị TRONG màn hình TranDauManagement:**

**Cách 1: Tab bên trong TranDau (đề xuất)**
```
┌────────────────────────────────────────────────────────────────┐
│  🏟️ QUẢN LÝ TRẬN ĐẤU                                          │
├────────────────────────────────────────────────────────────────┤
│  [📋 Danh sách trận] [👤 Chi tiết trận đã chọn]              │  ← TabPane
├────────────────────────────────────────────────────────────────┤
│  📋 TAB: DANH SÁCH TRẬN ĐẤU                                    │
│  (CRUD như mockup trên - có nút [📝 Điểm])                    │
└────────────────────────────────────────────────────────────────┘

Khi click vào 1 trận → Chuyển sang tab "Chi tiết":

┌────────────────────────────────────────────────────────────────┐
│  🏟️ QUẢN LÝ TRẬN ĐẤU                                          │
├────────────────────────────────────────────────────────────────┤
│  [� Danh sách trận] [👤 Chi tiết trận TD01] ← Active        │
├────────────────────────────────────────────────────────────────┤
│  👤 CHI TIẾT TRẬN ĐẤU TD01                                     │
│  DB01 (Barcelona A) vs DB02 (Barcelona B)                      │
│  Trọng tài: Nguyễn X | Sân: SD1 | [📝 Sửa điểm]              │
├────────────────────────────────────────────────────────────────┤
│  🎯 CẦU THỦ THAM GIA & ĐIỂM SỐ                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ Đội  │ Cầu thủ         │ Vị trí   │ Số bàn │ 🔥 Hiệu suất│ │
│  ├──────────────────────────────────────────────────────────┤ │
│  │ DB01 │ CT01 (Nguyễn A) │ Tiền đạo │   2    │ ⭐⭐      │ │
│  │ DB01 │ CT02 (Trần B)   │ Hậu vệ   │   0    │ ⭐        │ │
│  │ DB02 │ CT03 (Lê C)     │ Tiền đạo │   1    │ ⭐⭐      │ │
│  │ DB02 │ CT04 (Phạm D)   │ Thủ môn  │   0    │ ⭐        │ │
│  └──────────────────────────────────────────────────────────┘ │
│  📊 Tỷ số cuối: DB01 [2] - [1] DB02                           │
│  🎯 Tổng cầu thủ ra sân: 4 cầu thủ                            │
│  🔥 Vua phá lưới trận này: CT01 (2 bàn)                       │
│                                                                │
│  [⬅️ Quay lại danh sách]  [📝 Sửa điểm số]                    │
└────────────────────────────────────────────────────────────────┘
```

**Cách 2: Expandable Row (nâng cao)**
- Mỗi dòng trận đấu có nút [▶️ Chi tiết]
- Click → Mở rộng row hiển thị bảng ThamGia bên dưới
- Giống như accordion/collapse pattern

**Cách 3: Popup chỉ xem (đơn giản nhất)**
- Thêm nút [👁️ Xem] bên cạnh [📝 Điểm]
- Click → Popup READ-ONLY hiển thị bảng ThamGia

---

---

#### **6. Dashboard.fxml** - Tổng quan

```
┌────────────────────────────────────────────────────────────────┐
│  🏠 DASHBOARD                                                  │
├────────────────────────────────────────────────────────────────┤
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐         │
│  │ 👥 Đội  │  │ ⚽ Cầu   │  │ 🏟️ Trận │  │ 🎯 Bàn  │         │
│  │         │  │   thủ   │  │   đấu   │  │  thắng  │         │
│  │   24    │  │   158   │  │   36    │  │   245   │         │
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘         │
├────────────────────────────────────────────────────────────────┤
│  📊 Thống kê theo mảnh:                                        │
│  ┌───────────────────┬───────────────────┐                    │
│  │ CLB1 (DB1)        │ CLB2 (DB2)        │                    │
│  ├───────────────────┼───────────────────┤                    │
│  │ 12 đội            │ 12 đội            │                    │
│  │ 79 cầu thủ        │ 79 cầu thủ        │                    │
│  │ 18 trận (SD1)     │ 18 trận (SD2)     │                    │
│  └───────────────────┴───────────────────┘                    │
├────────────────────────────────────────────────────────────────┤
│  🏆 TOP SCORER                                                 │
│  1. Nguyễn A (CT01) - 15 bàn                                   │
│  2. Trần B (CT02) - 12 bàn                                     │
│  3. Lê C (CT03) - 10 bàn                                       │
└────────────────────────────────────────────────────────────────┘
```

### Cấu trúc Pages (FXML Files):

```
Sidebar Navigation:
┌─────────────────────┐
│ 🏠 Tổng quan        │  ← Dashboard.fxml
├─────────────────────┤
│ 📋 QUẢN LÝ          │
│   👥 Đội bóng       │  ← DoiBongManagement.fxml
│   ⚽ Cầu thủ        │  ← CauThuManagement.fxml
│   🏟️ Trận đấu       │  ← TranDauManagement.fxml 
│                     │     • CRUD trận đấu
│                     │     • [📝 Điểm] → Nhập điểm (tạo ThamGia)
│                     │     • Tab/Popup hiển thị chi tiết (xem ThamGia)
├─────────────────────┤
│ 🔍 Truy vấn         │  ← QueryManagement.fxml
├─────────────────────┤
│ 🚪 Đăng xuất        │
└─────────────────────┘

**LƯU Ý:** ThamGia KHÔNG có màn hình riêng, được tích hợp hoàn toàn vào TranDau
```

#### **1. Main.fxml** - Container chính
- **MainController.java**: Quản lý sidebar navigation
- Load các page con vào `contentArea` (AnchorPane/BorderPane)

#### **2. Login.fxml** - Màn hình đăng nhập
- **LoginController.java**: Xác thực user, kết nối DB
- Input: Username, Password
- Kiểm tra quyền truy cập mảnh DB1/DB2

#### **3. Dashboard.fxml** - Trang tổng quan
- **DashboardController.java**
- Cards: Tổng đội bóng, cầu thủ, trận đấu, bàn thắng
- Biểu đồ: Top scorer, trận đấu gần đây
- So sánh 2 mảnh (CLB1 vs CLB2)

#### **4. CRUD Pages** - Tự động routing + Filter mảnh:

**Pattern chung cho tất cả CRUD:**
- **RadioButton group:** ● Tất cả ○ Mảnh DB1 ○ Mảnh DB2 (CHỈ để XEM, không ảnh hưởng CRUD)
- **Auto-routing khi thêm/sửa:** Dựa vào **ComboBox CLB/SanDau** (KHÔNG phải RadioButton)
- **Filter khi hiển thị:** User chọn xem từ mảnh nào
- **Validation:** Check trùng khóa qua cả 2 DB
- **Status bar:** Hiển thị số lượng từ mỗi mảnh

**a) DoiBongManagement.fxml**
- **DoiBongController.java**
- Filter hiển thị: ● Tất cả ○ CLB1 (DB1) ○ CLB2 (DB2)
- Auto-routing insert: User chọn `ComboBox CLB` → CLB1 = insert DB1, CLB2 = insert DB2
- **LƯU Ý:** RadioButton CHỈ để xem, ComboBox CLB quyết định insert vào DB nào

**b) CauThuManagement.fxml**
- **CauThuController.java**
- Filter hiển thị: ● Tất cả ○ CLB1 (DB1) ○ CLB2 (DB2)
- Auto-routing insert: Dựa vào `ComboBox CLB` của đội → tìm CLB của đội đó → insert vào DB tương ứng

**c) TranDauManagement.fxml** - Quản lý trận đấu + Nhập/Xem điểm (ThamGia tích hợp)
- **TranDauController.java**
- **Layout:** TabPane với 2 tabs:
  - **Tab 1: Danh sách trận** - CRUD TranDau
  - **Tab 2: Chi tiết trận** - Hiển thị bảng ThamGia (tự động load khi chọn trận)
- **Chức năng Tab 1 (Danh sách trận):**
  - CRUD trận đấu (Thêm, Xóa, Sửa) → CHỈ lưu bảng TranDau
  - Button [📝 Nhập điểm] → Popup nhập điểm → **TỰ ĐỘNG tạo bảng ThamGia**
  - Click vào 1 trận → Tự động chuyển sang Tab 2
- **Chức năng Tab 2 (Chi tiết trận):**
  - **Hiển thị READ-ONLY:** TableView với cột (Đội, Cầu thủ, Vị trí, Số bàn)
  - Load từ bảng `ThamGia` WHERE MaTD = trận đang chọn
  - Hiển thị tỷ số, top scorer của trận đó
  - Button [📝 Sửa điểm] → Mở lại popup nhập điểm
  - Button [⬅️ Quay lại] → Chuyển về Tab 1
- Filter: ● Tất cả ○ SD1 (DB1) ○ SD2 (DB2)
- Auto-routing: SD1 → insert DB1, SD2 → insert DB2
- **💡 LUỒNG ĐÚNG:**
  1. **Tab 1:** Tạo trận đấu → Lưu vào `TranDau` (chưa có điểm)
  2. **Tab 1:** Click [📝 Điểm] → Popup nhập điểm
  3. **Popup:** Tick ☑ cầu thủ tham gia + nhập số bàn → Lưu vào `ThamGia`
  4. **Tab 2:** Click vào trận → Tự động hiển thị bảng ThamGia của trận đó
  5. **Kết quả:** ThamGia KHÔNG cần CRUD riêng, hoàn toàn tích hợp trong TranDau

#### **5. Query Pages** (10 câu truy vấn phân tán):

**Option 1: Tất cả trong 1 màn hình (đề xuất)**
- **QueryManagement.fxml** - 1 màn hình duy nhất
- **QueryController.java**
- Layout: TabPane hoặc Accordion với 10 tabs/sections
- Mỗi tab chứa input fields + button + TableView riêng
- Tiết kiệm code, dễ maintain

**Option 2: Nhóm theo độ phức tạp (3 màn hình)**
- **QueryBasic.fxml** (Câu 1-3: Query đơn giản)
  - Tab 1: Tìm cầu thủ theo CLB
  - Tab 2: Số trận của cầu thủ
  - Tab 3: Trận hòa theo sân
  
- **QueryAdvanced.fxml** (Câu 4-7: Aggregation)
  - Tab 1: Vua phá lưới
  - Tab 2: Trận theo CT & trọng tài
  - Tab 3: 2 CT cùng CLB
  - Tab 4: CT tham gia 0 bàn
  
- **QueryStatistics.fxml** (Câu 8-10: Thống kê)
  - Tab 1: CT tích cực (≥3 trận)
  - Tab 2: Tổng bàn của đội
  - Tab 3: CT chưa thi đấu

**Layout example (Option 1 - TabPane):**
```xml
<TabPane fx:id="queryTabs">
    <!-- Câu 1 -->
    <Tab text="Q1: CT theo CLB">
        <VBox spacing="10" padding="20">
            <HBox spacing="10">
                <Label text="Nhập CLB:"/>
                <TextField fx:id="txtCLB_Q1"/>
                <Button text="Tìm" onAction="#handleQuery1"/>
            </HBox>
            <TableView fx:id="tableQ1"/>
        </VBox>
    </Tab>
    
    <!-- Câu 2 -->
    <Tab text="Q2: Số trận CT">
        <VBox spacing="10" padding="20">
            <HBox spacing="10">
                <Label text="Họ tên:"/>
                <TextField fx:id="txtHoTen_Q2"/>
                <Button text="Tìm" onAction="#handleQuery2"/>
            </HBox>
            <Label fx:id="lblResult_Q2" styleClass="result-label"/>
        </VBox>
    </Tab>
    
    <!-- ... Câu 3-10 tương tự -->
</TabPane>
```



### Controller Pattern:

```java
// MainController.java - Quản lý navigation
public class MainController {
    @FXML private VBox sidebar;
    @FXML private AnchorPane contentArea;
    
    @FXML void showDashboard() { loadPage("Dashboard.fxml"); }
    @FXML void showDoiBong() { loadPage("DoiBongManagement.fxml"); }
    @FXML void showCauThu() { loadPage("CauThuManagement.fxml"); }
    @FXML void showQueries() { loadPage("QueryManagement.fxml"); }
    // ...
    
    private void loadPage(String fxmlFile) {
        Parent page = FXMLLoader.load(...);
        contentArea.getChildren().setAll(page);
    }
}

// QueryController.java - 10 queries trong 1 controller
public class QueryController {
    // Query 1 components
    @FXML private TextField txtCLB_Q1;
    @FXML private TableView<CauThu> tableQ1;
    
    // Query 2 components
    @FXML private TextField txtHoTen_Q2;
    @FXML private Label lblResult_Q2;
    
    // ... Q3-Q10 components
    
    private QueryService queryService = new QueryService();
    
    @FXML void handleQuery1() {
        String clb = txtCLB_Q1.getText();
        List<CauThu> result = queryService.query1_FindByClb(clb);
        tableQ1.getItems().setAll(result);
    }
    
    @FXML void handleQuery2() {
        String hoTen = txtHoTen_Q2.getText();
        int count = queryService.query2_CountMatches(hoTen);
        lblResult_Q2.setText("Số trận: " + count);
    }
    
    // ... Q3-Q10 methods
}

// DoiBongController.java - CRUD Example
public class DoiBongController {
    @FXML private TextField txtMaDB, txtTenDB;
    @FXML private ComboBox<String> cbCLB;
    @FXML private TableView<DoiBong> tableDoiBong;
    
    private DoiBongService service = new DoiBongService();
    
    @FXML void handleThem() {
        // 1. Validate input
        // 2. Kiểm tra MaDB trùng qua 2 DB
        if (service.existsInBothDB(txtMaDB.getText())) {
            showError("Mã đội bóng đã tồn tại!");
            return;
        }
        // 3. Insert vào DB đúng theo CLB
        service.insert(...);
        refreshTable();
    }
    
    @FXML void handleXoa() { /* Xóa từ DB tương ứng */ }
    @FXML void handleSua() { /* Update DB */ }
    @FXML void handleTimKiem() { /* Query from both DB */ }
}
```

### CSS Styling (resources/styles.css):

```css
/* Sidebar */
.sidebar {
    -fx-background-color: #1976D2;
    -fx-pref-width: 200px;
}

.sidebar-button {
    -fx-background-color: transparent;
    -fx-text-fill: white;
    -fx-font-size: 14px;
    -fx-padding: 15px;
    -fx-cursor: hand;
}

.sidebar-button:hover {
    -fx-background-color: #1565C0;
}

.sidebar-button:selected {
    -fx-background-color: #0D47A1;
    -fx-border-color: #FFC107;
    -fx-border-width: 0 0 0 4px;
}

/* Content Area */
.content-area {
    -fx-background-color: #F5F5F5;
    -fx-padding: 20px;
}

/* Buttons */
.btn-primary {
    -fx-background-color: #2196F3;
    -fx-text-fill: white;
    -fx-background-radius: 5px;
    -fx-cursor: hand;
}

.btn-success { -fx-background-color: #4CAF50; }
.btn-danger { -fx-background-color: #F44336; }
.btn-warning { -fx-background-color: #FF9800; }

/* TextFields */
.text-field {
    -fx-background-radius: 5px;
    -fx-border-radius: 5px;
    -fx-border-color: #BDBDBD;
}

.text-field:focused {
    -fx-border-color: #2196F3;
    -fx-border-width: 2px;
}

/* TableView */
.table-view {
    -fx-background-color: white;
    -fx-border-radius: 5px;
}

.table-view .column-header {
    -fx-background-color: #E3F2FD;
    -fx-font-weight: bold;
}
```

### Quy tắc xử lý phân mảnh trong Controllers:

**QUAN TRỌNG:** RadioButton filter CHỈ ảnh hưởng VIEW, KHÔNG ảnh hưởng CRUD!

1. **Thêm (INSERT):**
   - Xác định mảnh dựa vào **ComboBox CLB/SanDau** (KHÔNG phải RadioButton)
   - Kiểm tra khóa chính trùng qua **cả 2 DB** trước khi insert
   - Insert vào đúng DB instance
   - (Optional) Tự động chuyển RadioButton về "Tất cả" sau insert để thấy record mới
   
   ```java
   @FXML void handleThem() {
       String clb = cbCLB.getValue(); // ComboBox, KHÔNG phải RadioButton
       
       // Xác định DB dựa vào CLB
       Connection targetConn = clb.equals("CLB1") ? conn1 : conn2;
       
       // Insert
       doiBongDAO.insert(doiBong, targetConn);
       
       // Auto switch về "Tất cả" để thấy record mới
       rbAll.setSelected(true);
       handleFilterChange(); // Refresh table
   }
   ```

2. **Xóa (DELETE):**
   - Tìm record ở DB nào (dựa vào CLB/SanDau của record)
   - Xóa khóa ngoại liên quan (cascade)
   - Xóa từ đúng DB instance

3. **Sửa (UPDATE):**
   - Nếu thay đổi CLB/SanDau → **Di chuyển dữ liệu** giữa 2 mảnh
   - Pattern: SELECT → DELETE (DB cũ) → INSERT (DB mới)

4. **Tìm kiếm (SELECT):**
   - Nếu biết mảnh → Query 1 DB
   - Nếu không → Query cả 2 DB và UNION kết quả
   
5. **Filter VIEW (RadioButton):**
   - CHỈ ảnh hưởng đến TableView hiển thị
   - KHÔNG ảnh hưởng đến CRUD operations
   
   ```java
   @FXML void handleFilterChange() {
       // RadioButton CHỈ để lọc VIEW
       if (rbAll.isSelected()) {
           // Merge cả 2 DB
           List<DoiBong> fromDB1 = doiBongDAO.findAll(conn1);
           List<DoiBong> fromDB2 = doiBongDAO.findAll(conn2);
           // Merge và hiển thị
       } else if (rbDB1.isSelected()) {
           // Chỉ hiển thị DB1
       } else {
           // Chỉ hiển thị DB2
       }
   }
   ```

## 🔧 Build & Run

### Maven commands (PowerShell):
```powershell
mvn clean install          # Compile và download dependencies
mvn javafx:run            # Chạy ứng dụng
mvn clean package         # Tạo JAR file
```

### Database setup cần kiểm tra:
1. SQL Server instances đang chạy (port 1433 và 1434)
2. Database `QL_BongDa_CLB1_SD1` và `QL_BongDa_CLB2_SD2` đã tạo
3. 4 tables (DoiBong, CauThu, TranDau, ThamGia) đã có schema
4. Credentials trong `DatabaseConnection.java` đúng

### Debugging:
- Bật SQL logging để xem queries thực tế
- Test từng DB riêng trước khi merge
- Dùng `System.out.println()` để trace data flow

## 📝 Workflow phát triển tính năng mới

Khi implement 1 câu query (ví dụ Câu 5):

1. **Phân tích phân mảnh:**
   - Tables nào cần? → CauThu (phân theo CLB), TranDau (phân theo SanDau)
   - Cần query DB nào? → Có thể cả 2
   
2. **Tạo DAO methods:**
   ```java
   // CauThuDAO.java
   List<CauThu> findByHoTen(String hoTen, Connection conn)
   
   // TranDauDAO.java
   List<TranDau> findByTrongTaiAndCauThu(String trongTai, String maCT, Connection conn)
   ```

3. **Tạo Service logic:**
   ```java
   // QueryService.java
   public List<TranDau> query5(String hoTen, String trongTai) {
       // 1. Tìm cầu thủ từ cả 2 DB
       // 2. JOIN với trận đấu
       // 3. Filter theo trọng tài
       // 4. Merge results
   }
   ```

4. **Tạo Controller + FXML:**
   - Input: 2 TextField (hoTen, trongTai)
   - Output: TableView với columns (MaTD, MaDB1, MaDB2, TrongTai, SanDau)
   - Button: Gọi service và hiển thị kết quả

5. **Test với data thực:**
   - Insert test data vào cả 2 DB
   - Verify kết quả đúng với yêu cầu đề bài

## ⚠️ Lỗi thường gặp

1. **"No suitable driver"**: Thiếu `mssql-jdbc` trong dependencies
2. **"Login failed"**: Sai credentials hoặc SQL Server không cho phép remote
3. **Empty result**: Query sai DB instance (check CLB/SanDau)
4. **NullPointerException**: Không check null khi JOIN bị rỗng
5. **FXML LoadException**: Sai path hoặc fx:id không match với controller

## 🎓 Quy tắc đặt tên

- **Package:** lowercase (models, dao, services)
- **Class:** PascalCase (CauThuDAO, QueryService)
- **Method:** camelCase (findByHoTen, getTopScorer)
- **Variable:** camelCase (maCT, hoTen)
- **FXML id:** camelCase với prefix (txtCLB, tableResult, btnSearch)

---

**Mục tiêu cuối:** Ứng dụng desktop hoàn chỉnh với 10 màn hình query, xử lý distributed database, UI đẹp và UX mượt mà.
