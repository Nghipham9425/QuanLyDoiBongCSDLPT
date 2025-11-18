package org.football.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.football.models.CauThu;
import org.football.models.DoiBong;
import org.football.services.CauThuService;
import org.football.services.DoiBongService;
import org.football.utils.AlertUtils;

import java.util.List;

public class DoiBongController {

    @FXML private TextField txtMaDB;
    @FXML private TextField txtTenDB;
    @FXML private ComboBox<String> cbCLB;
    
    @FXML private Button btnThem;
    @FXML private Button btnSua;
    @FXML private Button btnXoa;
    @FXML private Button btnLamMoi;
    
    @FXML private RadioButton rbAll;
    @FXML private RadioButton rbCLB1;
    @FXML private RadioButton rbCLB2;
    @FXML private ToggleGroup filterGroup;
    
    @FXML private TextField txtTimKiem;
    
    @FXML private TableView<DoiBong> tableDoiBong;
    @FXML private TableColumn<DoiBong, String> colMaDB;
    @FXML private TableColumn<DoiBong, String> colTenDB;
    @FXML private TableColumn<DoiBong, String> colCLB;
    
    @FXML private Label lblTongSo;
    @FXML private Label lblDB1;
    @FXML private Label lblDB2;
    
    private DoiBongService service = new DoiBongService();
    private ObservableList<DoiBong> dataList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Lấy CLB từ LoginController
        String selectedCLB = LoginController.getSelectedCLB();
        boolean isClb1 = selectedCLB.contains("CLB1");
        
        // ComboBox CLB: chỉ hiển thị branch hiện tại
        String currentBranch = isClb1 ? "CLB1" : "CLB2";
        cbCLB.setItems(FXCollections.observableArrayList(currentBranch));
        cbCLB.getSelectionModel().selectFirst();
        
        // Toán tử 3 ngôi: hiển thị/ẩn RadioButton tương ứng
        rbAll.setVisible(true);
        rbAll.setManaged(true);
        rbCLB1.setVisible(isClb1);
        rbCLB1.setManaged(isClb1);
        rbCLB2.setVisible(!isClb1);
        rbCLB2.setManaged(!isClb1);
        
        // Mặc định chọn "Tất cả"
        rbAll.setSelected(true);
        
        setupTable();
        setupListeners();
        loadData();
    }
    
    private void setupTable() {
        colMaDB.setCellValueFactory(new PropertyValueFactory<>("maDB"));
        colTenDB.setCellValueFactory(new PropertyValueFactory<>("tenDB"));
        colCLB.setCellValueFactory(new PropertyValueFactory<>("clb"));
        tableDoiBong.setItems(dataList);
        
        // Double-click to edit
        tableDoiBong.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                DoiBong selected = tableDoiBong.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    txtMaDB.setText(selected.getMaDB());
                    txtTenDB.setText(selected.getTenDB());
                    cbCLB.setValue(selected.getClb());
                }
            }
        });
    }
    
    private void setupListeners() {
        filterGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val != null) handleTimKiem();
        });
        
        tableDoiBong.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            btnSua.setDisable(val == null);
            btnXoa.setDisable(val == null);
        });
    }

    @FXML
    public void handleThem() {
        try {
            if (txtMaDB.getText().trim().isEmpty() || txtTenDB.getText().trim().isEmpty()) {
                AlertUtils.showError(" Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            String clb = cbCLB.getValue();
            DoiBong doiBong = new DoiBong(
                txtMaDB.getText().trim(),
                txtTenDB.getText().trim(),
                clb
            );
            
            service.insert(doiBong, clb);
            AlertUtils.showInfo("Thêm đội bóng thành công!");
            
            clearForm();
            loadData();
            
        } catch (Exception e) {
            AlertUtils.showError(" " + e.getMessage());
        }
    }
    
    @FXML
    public void handleSua() {
        try {
            DoiBong selected = tableDoiBong.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError(" Vui lòng chọn đội cần sửa!");
                return;
            }
            
            if (txtMaDB.getText().trim().isEmpty() || txtTenDB.getText().trim().isEmpty()) {
                AlertUtils.showError(" Vui lòng nhập đầy đủ thông tin!");
                return;
            }
            
            String oldCLB = selected.getClb();
            String newCLB = cbCLB.getValue();
            
            if (!oldCLB.equals(newCLB)) {
                if (!AlertUtils.confirmMigration(oldCLB, newCLB)) {
                    return;
                }
            }
            
            DoiBong doiBong = new DoiBong(
                txtMaDB.getText().trim(),
                txtTenDB.getText().trim(),
                newCLB
            );
            
            service.update(doiBong, oldCLB, newCLB);
            AlertUtils.showInfo(" Cập nhật đội bóng thành công!");
            
            clearForm();
            loadData();
            
        } catch (Exception e) {
            AlertUtils.showError(" " + e.getMessage());
        }
    }
    
    @FXML
    public void handleXoa() {
        try {
            DoiBong selected = tableDoiBong.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError("Vui lòng chọn đội cần xóa!");
                return;
            }
            
            // Kiểm tra xem có cầu thủ nào thuộc đội này không
            CauThuService cauThuService = new CauThuService();
            List<CauThu> players = cauThuService.findByMaDB(selected.getMaDB());
            
            if (!players.isEmpty()) {
                AlertUtils.showError(" Không thể xóa đội bóng!\n\n" +
                    "Đội này có " + players.size() + " cầu thủ.\n" +
                    "Vui lòng xóa hoặc chuyển cầu thủ sang đội khác trước.");
                return;
            }
            
            if (!AlertUtils.confirmDelete("đội " + selected.getTenDB())) {
                return;
            }
            
            service.delete(selected.getMaDB(), selected.getClb());
            AlertUtils.showInfo(" Xóa đội bóng thành công!");
            
            clearForm();
            loadData();
            
        } catch (Exception e) {
            AlertUtils.showError(" " + e.getMessage());
        }
    }
    
    @FXML
    public void handleLamMoi() {
        clearForm();
        loadData();
    }

    @FXML
    public void handleTimKiem() {
        try {
            List<DoiBong> result;
            String keyword = txtTimKiem.getText().trim();
            
            if (!keyword.isEmpty()) {
                result = service.searchByName(keyword);
            } else {
                if (rbAll.isSelected()) {
                    result = service.findAll();
                } else if (rbCLB1.isSelected()) {
                    result = service.findByDB(1);
                } else {
                    result = service.findByDB(2);
                }
            }
            
            dataList.setAll(result);
            updateStats(result);
            
        } catch (Exception e) {
            AlertUtils.showError(" Lỗi tìm kiếm: " + e.getMessage());
        }
    }

    
    private void loadData() {
        try {
            List<DoiBong> data = service.findAll();
            dataList.setAll(data);
            updateStats(data);
        } catch (Exception e) {
            AlertUtils.showError(" Lỗi load dữ liệu: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtMaDB.clear();
        txtTenDB.clear();
        cbCLB.getSelectionModel().selectFirst();
        tableDoiBong.getSelectionModel().clearSelection();
    }
    
    private void updateStats(List<DoiBong> data) {
        int total = data.size();
        int db1Count = (int) data.stream().filter(db -> db.getClb().equals("CLB1")).count();
        int db2Count = total - db1Count;
        
        lblTongSo.setText(" Tổng: " + total + " đội");
        lblDB1.setText("DB1: " + db1Count + " đội");
        lblDB2.setText("DB2: " + db2Count + " đội");
    }
}
