package org.football.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.football.dao.ThamGiaDAO;
import org.football.models.DoiBong;
import org.football.models.TranDau;
import org.football.services.DoiBongService;
import org.football.services.TranDauService;
import org.football.utils.AlertUtils;

import java.util.List;

public class TranDauController {
    // FXML Components
    @FXML private TextField txtMaTD;
    @FXML private ComboBox<DoiBong> cbMaDB1;
    @FXML private ComboBox<DoiBong> cbMaDB2;
    @FXML private TextField txtTrongTai;
    @FXML private ComboBox<String> cbSanDau;
    @FXML private TextField txtTimKiem;
    
    @FXML private TableView<TranDau> tableTranDau;
    @FXML private TableColumn<TranDau, String> colMaTD;
    @FXML private TableColumn<TranDau, String> colMaDB1;
    @FXML private TableColumn<TranDau, String> colMaDB2;
    @FXML private TableColumn<TranDau, String> colTyso;
    @FXML private TableColumn<TranDau, String> colTrongTai;
    @FXML private TableColumn<TranDau, String> colSanDau;
    @FXML private TableColumn<TranDau, Void> colAction;
    
    @FXML private RadioButton rbAll;
    @FXML private RadioButton rbSD1;
    @FXML private RadioButton rbSD2;
    
    @FXML private Label lblTotal;
    @FXML private Label lblSD1;
    @FXML private Label lblSD2;
    
    // Services
    private TranDauService service;
    private DoiBongService doiBongService;
    private ThamGiaDAO thamGiaDAO;
    private ObservableList<TranDau> dataList;
    private String currentSanDau = null; // Track selected match's stadium

    @FXML
    public void initialize() {
        System.out.println("🏟️ TranDauController initialized");
        
        service = new TranDauService();
        doiBongService = new DoiBongService();
        thamGiaDAO = new ThamGiaDAO();
        dataList = FXCollections.observableArrayList();
        
        // Lấy CLB từ LoginController
        String selectedCLB = LoginController.getSelectedCLB();
        boolean isClb1 = selectedCLB.contains("CLB1");
        
        // Hiển thị tất cả RadioButton
        rbAll.setVisible(true);
        rbAll.setManaged(true);
        rbSD1.setVisible(true);
        rbSD1.setManaged(true);
        rbSD2.setVisible(true);
        rbSD2.setManaged(true);
        
        // Mặc định chọn "Tất cả"
        rbAll.setSelected(true);
        
        // Setup table columns
        colMaTD.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaTD()));
        colMaDB1.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaDB1()));
        colMaDB2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaDB2()));
        
        // Tỷ số column - calculate from ThamGia table
        colTyso.setCellValueFactory(data -> {
            try {
                TranDau tran = data.getValue();
                String score = thamGiaDAO.getMatchScore(tran.getMaTD(), tran.getMaDB1(), tran.getMaDB2());
                return new SimpleStringProperty(score);
            } catch (Exception e) {
                return new SimpleStringProperty("0-0");
            }
        });
        
        colTrongTai.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrongTai()));
        colSanDau.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSanDau()));
        
        // Action column with [📝 Nhập điểm] button
        colAction.setCellFactory(col -> new TableCell<TranDau, Void>() {
            private final Button btnNhapDiem = new Button("📝 Nhập điểm");
            
            {
                btnNhapDiem.setStyle(
                    "-fx-background-color: #4caf50; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 11px; " +
                    "-fx-padding: 6 12; " +
                    "-fx-background-radius: 5; " +
                    "-fx-cursor: hand;"
                );
                
                btnNhapDiem.setOnMouseEntered(e -> 
                    btnNhapDiem.setStyle(
                        "-fx-background-color: #45a049; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
                    )
                );
                
                btnNhapDiem.setOnMouseExited(e -> 
                    btnNhapDiem.setStyle(
                        "-fx-background-color: #4caf50; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 11px; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand;"
                    )
                );
                
                btnNhapDiem.setOnAction(e -> {
                    TranDau tran = getTableView().getItems().get(getIndex());
                    handleNhapDiem(tran);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnNhapDiem);
            }
        });
        
        // Setup ComboBox SanDau (SD1/SD2)
        cbSanDau.setItems(FXCollections.observableArrayList("SD1", "SD2"));
        
        // Load teams for ComboBoxes từ branch hiện tại
        loadDoiBong(isClb1);
        
        // Setup RadioButton group
        ToggleGroup group = new ToggleGroup();
        rbAll.setToggleGroup(group);
        rbSD1.setToggleGroup(group);
        rbSD2.setToggleGroup(group);
        
        // Filter listener
        rbAll.selectedProperty().addListener((obs, old, newVal) -> { if (newVal) loadData(0); });
        rbSD1.selectedProperty().addListener((obs, old, newVal) -> { if (newVal) loadData(1); });
        rbSD2.selectedProperty().addListener((obs, old, newVal) -> { if (newVal) loadData(2); });
        
        // Double-click to open score dialog (nhập điểm)
        tableTranDau.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableTranDau.getSelectionModel().getSelectedItem() != null) {
                TranDau selected = tableTranDau.getSelectionModel().getSelectedItem();
                handleNhapDiem(selected);
            }
        });
        
        // Selection listener to track current stadium
        tableTranDau.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                currentSanDau = newVal.getSanDau();
                fillFormWithSelectedRow();
            }
        });
        
        // Load data theo branch đã chọn (KHÔNG load all)
        if (isClb1) {
            loadData(1); // Load SD1
        } else {
            loadData(2); // Load SD2
        }
    }
    
    // Load teams into ComboBoxes with custom display - filter theo branch
    private void loadDoiBong(boolean isClb1) {
        try {
            List<DoiBong> allTeams;
            if (isClb1) {
                allTeams = doiBongService.findByDB(1);  // DB1 = CLB1
            } else {
                allTeams = doiBongService.findByDB(2);  // DB2 = CLB2
            }
            
            // Setup StringConverter to display "MaDB - TenDB"
            StringConverter<DoiBong> converter = new StringConverter<DoiBong>() {
                @Override
                public String toString(DoiBong db) {
                    return db == null ? "" : db.getMaDB() + " - " + db.getTenDB();
                }
                
                @Override
                public DoiBong fromString(String string) {
                    return null; // Not needed for display-only
                }
            };
            
            cbMaDB1.setItems(FXCollections.observableArrayList(allTeams));
            cbMaDB1.setConverter(converter);
            
            cbMaDB2.setItems(FXCollections.observableArrayList(allTeams));
            cbMaDB2.setConverter(converter);
            
            System.out.println("✅ Loaded " + allTeams.size() + " teams into ComboBoxes");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi load đội bóng: " + e.getMessage());
        }
    }
    
    // Load data with filter (0 = all, 1 = SD1, 2 = SD2)
    private void loadData(int filter) {
        try {
            System.out.println("🔄 Loading trận đấu from both databases...");
            List<TranDau> result;
            
            if (filter == 0) {
                result = service.findAll();
            } else {
                result = service.findByDB(filter);
            }
            
            dataList.setAll(result);
            tableTranDau.setItems(dataList);
            updateStats();
            System.out.println("✅ Filtering data...");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi load dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Update statistics labels
    private void updateStats() {
        try {
            List<TranDau> all = service.findAll();
            List<TranDau> sd1 = service.findByDB(1);
            List<TranDau> sd2 = service.findByDB(2);
            
            lblTotal.setText("Tổng: " + all.size());
            lblSD1.setText("SD1: " + sd1.size());
            lblSD2.setText("SD2: " + sd2.size());
        } catch (Exception e) {
            lblTotal.setText("Tổng: 0");
            lblSD1.setText("SD1: 0");
            lblSD2.setText("SD2: 0");
        }
    }
    
    // Fill form with selected row
    private void fillFormWithSelectedRow() {
        TranDau selected = tableTranDau.getSelectionModel().getSelectedItem();
        if (selected != null) {
            txtMaTD.setText(selected.getMaTD());
            
            // Find and select teams in ComboBoxes
            for (DoiBong db : cbMaDB1.getItems()) {
                if (db.getMaDB().equals(selected.getMaDB1())) {
                    cbMaDB1.setValue(db);
                    break;
                }
            }
            
            for (DoiBong db : cbMaDB2.getItems()) {
                if (db.getMaDB().equals(selected.getMaDB2())) {
                    cbMaDB2.setValue(db);
                    break;
                }
            }
            
            txtTrongTai.setText(selected.getTrongTai());
            cbSanDau.setValue(selected.getSanDau());
            currentSanDau = selected.getSanDau(); // Update tracked stadium
        }
    }
    
    // Clear form
    private void clearForm() {
        txtMaTD.clear();
        cbMaDB1.setValue(null);
        cbMaDB2.setValue(null);
        txtTrongTai.clear();
        cbSanDau.setValue(null);
        tableTranDau.getSelectionModel().clearSelection();
        currentSanDau = null;
    }
    
    // Validate form inputs
    private boolean validateForm() {
        if (txtMaTD.getText().trim().isEmpty()) {
            AlertUtils.showError("❌ Mã trận đấu không được để trống!");
            return false;
        }
        if (cbMaDB1.getValue() == null) {
            AlertUtils.showError("❌ Chưa chọn đội 1!");
            return false;
        }
        if (cbMaDB2.getValue() == null) {
            AlertUtils.showError("❌ Chưa chọn đội 2!");
            return false;
        }
        if (txtTrongTai.getText().trim().isEmpty()) {
            AlertUtils.showError("❌ Trọng tài không được để trống!");
            return false;
        }
        if (cbSanDau.getValue() == null) {
            AlertUtils.showError("❌ Chưa chọn sân đấu!");
            return false;
        }
        return true;
    }
    
    // Handle INSERT
    @FXML
    private void handleThem() {
        if (!validateForm()) return;
        
        try {
            String sanDau = cbSanDau.getValue();
            TranDau tran = new TranDau(
                txtMaTD.getText().trim(),
                cbMaDB1.getValue().getMaDB(),
                cbMaDB2.getValue().getMaDB(),
                txtTrongTai.getText().trim(),
                sanDau
            );
            
            service.insert(tran, sanDau);
            AlertUtils.showInfo("✅ Thêm trận đấu thành công!");
            clearForm();
            
            // Reload data
            loadData(0);
        } catch (Exception e) {
            AlertUtils.showError("❌ " + e.getMessage());
        }
    }
    
    // Handle UPDATE
    @FXML
    private void handleSua() {
        if (tableTranDau.getSelectionModel().getSelectedItem() == null) {
            AlertUtils.showError("❌ Chưa chọn trận đấu cần sửa!");
            return;
        }
        
        if (!validateForm()) return;
        
        try {
            String newSanDau = cbSanDau.getValue();
            
            // Check if stadium changed (requires migration confirmation)
            if (currentSanDau != null && !currentSanDau.equals(newSanDau)) {
                if (!AlertUtils.confirmMigration(currentSanDau, newSanDau)) {
                    return;
                }
            }
            
            TranDau tran = new TranDau(
                txtMaTD.getText().trim(),
                cbMaDB1.getValue().getMaDB(),
                cbMaDB2.getValue().getMaDB(),
                txtTrongTai.getText().trim(),
                newSanDau
            );
            
            service.update(tran, currentSanDau, newSanDau);
            AlertUtils.showInfo("✅ Cập nhật trận đấu thành công!");
            clearForm();
            
            // Refresh and keep current filter
            if (rbAll.isSelected()) loadData(0);
            else if (rbSD1.isSelected()) loadData(1);
            else loadData(2);
            
        } catch (Exception e) {
            AlertUtils.showError("❌ " + e.getMessage());
        }
    }
    
    // Handle DELETE
    @FXML
    private void handleXoa() {
        TranDau selected = tableTranDau.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("❌ Chưa chọn trận đấu cần xóa!");
            return;
        }
        
        // Confirm trước khi xóa
        if (!AlertUtils.confirmDelete("trận đấu " + selected.getMaTD())) {
            return;
        }
        
        try {
            service.delete(selected.getMaTD(), selected.getSanDau());
            AlertUtils.showInfo("✅ Xóa trận đấu thành công!");
            clearForm();
            
            // Refresh current view
            if (rbAll.isSelected()) loadData(0);
            else if (rbSD1.isSelected()) loadData(1);
            else loadData(2);
        } catch (Exception e) {
            AlertUtils.showError("❌ " + e.getMessage());
        }
    }
    
    // Handle SEARCH
    @FXML
    private void handleTimKiem() {
        String keyword = txtTimKiem.getText().trim();
        
        if (keyword.isEmpty()) {
            // If empty, reload current filter
            if (rbAll.isSelected()) loadData(0);
            else if (rbSD1.isSelected()) loadData(1);
            else loadData(2);
            return;
        }
        
        try {
            List<TranDau> result = service.searchByTrongTai(keyword);
            dataList.setAll(result);
            tableTranDau.setItems(dataList);
            updateStats();
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Không tìm thấy trận đấu với trọng tài: " + keyword);
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi tìm kiếm: " + e.getMessage());
        }
    }
    
    // Handle REFRESH
    @FXML
    private void handleLamMoi() {
        clearForm();
        txtTimKiem.clear();
        loadData(0);
    }
    
    // Handle nhập điểm (open popup to edit ThamGia)
    @FXML
    private void handleNhapDiem(TranDau tran) {
        if (tran == null) {
            AlertUtils.showError("❌ Chưa chọn trận đấu!");
            return;
        }
        
        try {
            // Open DiemSoDialog với FXML
            DiemSoDialogController.show(tran);
            
            // Refresh table to update scores
            if (rbAll.isSelected()) loadData(0);
            else if (rbSD1.isSelected()) loadData(1);
            else loadData(2);
            
        } catch (Exception e) {
            AlertUtils.showError(" Lỗi mở dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
