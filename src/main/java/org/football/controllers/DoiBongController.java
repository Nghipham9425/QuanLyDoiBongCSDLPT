package org.football.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.football.models.DoiBong;

public class DoiBongController {
    
    @FXML private TextField txtMaDB, txtTenDB, txtTimKiem;
    @FXML private ComboBox<String> cbCLB;
    @FXML private Button btnThem, btnSua, btnXoa, btnLamMoi;
    
    @FXML private RadioButton rbAll, rbCLB1, rbCLB2;
    @FXML private ToggleGroup filterGroup;
    
    @FXML private TableView<DoiBong> tableDoiBong;
    @FXML private TableColumn<DoiBong, String> colMaDB, colTenDB, colCLB, colManh;
    @FXML private TableColumn<DoiBong, Integer> colSoCauThu;
    
    @FXML private Label lblTongSo, lblDB1, lblDB2;
    
    @FXML
    public void initialize() {
        cbCLB.getItems().addAll("CLB1", "CLB2");
        setupTable();
        setupListeners();
        loadData();
    }
    
    private void setupTable() {
        colMaDB.setCellValueFactory(new PropertyValueFactory<>("maDB"));
        colTenDB.setCellValueFactory(new PropertyValueFactory<>("tenDB"));
        colCLB.setCellValueFactory(new PropertyValueFactory<>("clb"));
        colManh.setCellValueFactory(cellData -> {
            String clb = cellData.getValue().getClb();
            return new javafx.beans.property.SimpleStringProperty(
                "CLB1".equals(clb) ? "DB1" : "DB2"
            );
        });
        colSoCauThu.setCellValueFactory(new PropertyValueFactory<>("soCauThu"));
        
        tableDoiBong.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showDetails(newVal)
        );
    }
    
    private void setupListeners() {
        filterGroup.selectedToggleProperty().addListener((obs, old, newVal) -> filterData());
        txtTimKiem.textProperty().addListener((obs, old, newVal) -> filterData());
    }
    
    private void loadData() {
        // TODO: Load từ cả 2 DB
        System.out.println("⏳ Loading data from both databases...");
    }
    
    private void filterData() {
        // TODO: Filter theo RadioButton và search text
        System.out.println("🔍 Filtering data...");
    }
    
    private void showDetails(DoiBong doiBong) {
        if (doiBong != null) {
            txtMaDB.setText(doiBong.getMaDB());
            txtTenDB.setText(doiBong.getTenDB());
            cbCLB.setValue(doiBong.getClb());
        }
    }
    
    @FXML
    private void handleThem() {
        System.out.println("➕ Thêm đội bóng mới");
        // TODO: Validate → Insert vào DB theo CLB
    }
    
    @FXML
    private void handleSua() {
        System.out.println("✏️ Sửa đội bóng");
        // TODO: Validate → Update hoặc move giữa 2 DB nếu đổi CLB
    }
    
    @FXML
    private void handleXoa() {
        System.out.println("🗑️ Xóa đội bóng");
        // TODO: Confirm → Delete từ DB tương ứng
    }
    
    @FXML
    private void handleTimKiem() {
        System.out.println("🔍 Tìm kiếm: " + txtTimKiem.getText());
        filterData();
    }
    
    @FXML
    private void handleLamMoi() {
        System.out.println("🔄 Làm mới");
        txtMaDB.clear();
        txtTenDB.clear();
        cbCLB.setValue(null);
        txtTimKiem.clear();
        tableDoiBong.getSelectionModel().clearSelection();
        loadData();
    }
}
