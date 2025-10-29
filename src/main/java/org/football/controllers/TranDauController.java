package org.football.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.football.models.TranDau;

public class TranDauController {
    
    @FXML private TextField txtMaTD, txtTrongTai;
    @FXML private ComboBox<String> cbMaDB1, cbMaDB2, cbSanDau;
    @FXML private Button btnThem, btnSua, btnXoa, btnLamMoi;
    
    @FXML private RadioButton rbAll, rbSD1, rbSD2;
    @FXML private ToggleGroup filterGroup;
    
    @FXML private TableView<TranDau> tableTranDau;
    @FXML private TableColumn<TranDau, String> colMaTD, colMaDB1, colMaDB2, colTyso, colTrongTai, colSanDau, colManh, colAction;
    
    @FXML private Label lblTongSo, lblDB1, lblDB2;
    
    @FXML
    public void initialize() {
        // Populate ComboBoxes
        cbSanDau.getItems().addAll("SD1", "SD2");
        cbMaDB1.getItems().addAll("DB01", "DB02", "DB03", "DB04"); // TODO: Load from database
        cbMaDB2.getItems().addAll("DB01", "DB02", "DB03", "DB04");
        
        setupTable();
        setupListeners();
        loadData();
    }
    
    private void setupTable() {
        colMaTD.setCellValueFactory(new PropertyValueFactory<>("maTD"));
        colMaDB1.setCellValueFactory(new PropertyValueFactory<>("maDB1"));
        colMaDB2.setCellValueFactory(new PropertyValueFactory<>("maDB2"));
        colTyso.setCellValueFactory(cellData -> {
            // TODO: Calculate from ThamGia table
            return new javafx.beans.property.SimpleStringProperty("0-0");
        });
        colTrongTai.setCellValueFactory(new PropertyValueFactory<>("trongTai"));
        colSanDau.setCellValueFactory(new PropertyValueFactory<>("sanDau"));
        colManh.setCellValueFactory(cellData -> {
            String sanDau = cellData.getValue().getSanDau();
            return new javafx.beans.property.SimpleStringProperty(
                "SD1".equals(sanDau) ? "DB1" : "DB2"
            );
        });
        
        // Action column with button
        colAction.setCellFactory(col -> new TableCell<TranDau, String>() {
            private final Button btnNhapDiem = new Button("📝 Nhập điểm");
            
            {
                btnNhapDiem.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                btnNhapDiem.setOnAction(e -> {
                    TranDau tran = getTableView().getItems().get(getIndex());
                    handleNhapDiem(tran);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnNhapDiem);
            }
        });
        
        tableTranDau.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showDetails(newVal)
        );
    }
    
    private void setupListeners() {
        filterGroup.selectedToggleProperty().addListener((obs, old, newVal) -> filterData());
    }
    
    private void showDetails(TranDau tran) {
        if (tran != null) {
            txtMaTD.setText(tran.getMaTD());
            cbMaDB1.setValue(tran.getMaDB1());
            cbMaDB2.setValue(tran.getMaDB2());
            txtTrongTai.setText(tran.getTrongTai());
            cbSanDau.setValue(tran.getSanDau());
        }
    }
    
    private void loadData() {
        System.out.println("⏳ Loading trận đấu from both databases...");
        // TODO: TranDauDAO.findAll() from DB1 (SD1) + DB2 (SD2)
        filterData();
    }
    
    private void filterData() {
        System.out.println("🔍 Filtering data...");
        // TODO: Filter based on rbAll/rbSD1/rbSD2
        updateStatistics();
    }
    
    private void updateStatistics() {
        lblTongSo.setText("📊 Tổng: 0 trận");
        lblDB1.setText("SD1: 0 trận");
        lblDB2.setText("SD2: 0 trận");
    }
    
    @FXML
    private void handleThem() {
        System.out.println("➕ Tạo trận đấu");
        // TODO: Validate → Insert vào DB theo SanDau
        System.out.println("💡 Sau khi tạo, click [📝 Nhập điểm] để nhập kết quả");
    }
    
    @FXML
    private void handleSua() {
        System.out.println("✏️ Sửa trận đấu");
        // TODO: Update hoặc move giữa DB nếu đổi SanDau
    }
    
    @FXML
    private void handleXoa() {
        System.out.println("🗑️ Xóa trận đấu");
        // TODO: Confirm → Delete ThamGia trước → Delete TranDau
    }
    
    private void handleNhapDiem(TranDau tran) {
        System.out.println("📝 Nhập điểm cho trận: " + tran.getMaTD());
        // TODO: Open DiemSoDialog
        // - Load cầu thủ của 2 đội
        // - CheckBox để chọn cầu thủ tham gia
        // - TextField nhập số bàn thắng
        // - Lưu vào bảng ThamGia
    }
    
    @FXML
    private void handleLamMoi() {
        System.out.println("🔄 Làm mới");
        txtMaTD.clear();
        cbMaDB1.setValue(null);
        cbMaDB2.setValue(null);
        txtTrongTai.clear();
        cbSanDau.setValue(null);
        tableTranDau.getSelectionModel().clearSelection();
        loadData();
    }
}
