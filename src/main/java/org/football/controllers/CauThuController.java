package org.football.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import org.football.models.CauThu;
import org.football.models.DoiBong;
import org.football.services.CauThuService;
import org.football.services.DoiBongService;

import java.sql.SQLException;
import java.util.List;

public class CauThuController {
    private CauThuService cauThuService = new CauThuService();
    private DoiBongService doiBongService = new DoiBongService();

    @FXML private TextField txtMaCT, txtHoTen, txtTimKiem;
    @FXML private ComboBox<String> cbViTri;
    @FXML private ComboBox<DoiBong> cbDoiBong; 
    @FXML private Button btnThem, btnSua, btnXoa, btnLamMoi;
    
    @FXML private RadioButton rbAll, rbCLB1, rbCLB2;
    @FXML private ToggleGroup filterGroup;
    
    @FXML private TableView<CauThu> tableCauThu;
    @FXML private TableColumn<CauThu, String> colMaCT, colHoTen, colViTri, colMaDB;
    
    @FXML private Label lblTongSo, lblDB1, lblDB2;
    
    private DoiBong selectedDoiBongForEdit = null; 
    
    @FXML
    public void initialize() {
        // Vị trí
        cbViTri.getItems().addAll("Thủ môn", "Hậu vệ", "Tiền vệ", "Tiền đạo");
        
        // Load đội bóng từ cả 2 DB
        loadDoiBong();
        
        setupTable();
        setupListeners();
        loadData();
    }
    
    // Load ComboBox đội bóng với DoiBong object
    private void loadDoiBong() {
        try {
            List<DoiBong> doiBongList = doiBongService.findAll();
            cbDoiBong.setItems(FXCollections.observableArrayList(doiBongList));
            
            cbDoiBong.setConverter(new StringConverter<DoiBong>() {
                @Override
                public String toString(DoiBong db) {
                    if (db == null) return "";
                    return db.getMaDB() + " - " + db.getTenDB() + " (" + db.getClb() + ")";
                }
                
                @Override
                public DoiBong fromString(String string) {
                    return null;
                }
            });
            
        } catch (SQLException e) {
            showAlert("Error", " Lỗi load đội bóng: " + e.getMessage());
        }
    }
    
    private void setupTable() {
        colMaCT.setCellValueFactory(new PropertyValueFactory<>("maCT"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colViTri.setCellValueFactory(new PropertyValueFactory<>("viTri"));
        colMaDB.setCellValueFactory(new PropertyValueFactory<>("maDB"));
        
        tableCauThu.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> showDetails(newVal)
        );
    }
    
    private void setupListeners() {
        filterGroup.selectedToggleProperty().addListener((obs, old, newVal) -> filterData());
        txtTimKiem.textProperty().addListener((obs, old, newVal) -> filterData());
    }
    
    private void showDetails(CauThu cauThu) {
        if (cauThu != null) {
            txtMaCT.setText(cauThu.getMaCT());
            txtHoTen.setText(cauThu.getHoTen());
            cbViTri.setValue(cauThu.getViTri());
            
            // Tìm DoiBong trong ComboBox theo MaDB
            for (DoiBong db : cbDoiBong.getItems()) {
                if (db.getMaDB().equals(cauThu.getMaDB())) {
                    cbDoiBong.setValue(db);
                    selectedDoiBongForEdit = db; 
                    break;
                }
            }
        }
    }
    
    private void loadData() {
        filterData();
    }
    
    private void filterData() {
        try {
            List<CauThu> result;
            
            if (rbAll.isSelected()) {
                result = cauThuService.findAll();
            } else if (rbCLB1.isSelected()) {
                result = cauThuService.findByDB(1);
            } else {
                result = cauThuService.findByDB(2);
            }
            
            // Filter theo tên nếu có search
            if (!txtTimKiem.getText().isEmpty()) {
                String keyword = txtTimKiem.getText().toLowerCase();
                result = result.stream()
                    .filter(ct -> ct.getHoTen().toLowerCase().contains(keyword))
                    .toList();
            }
            
            tableCauThu.getItems().setAll(result);
            updateStatistics();
            
        } catch (Exception e) {
            showAlert("Error", " Lỗi load dữ liệu: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        try {
            int total = cauThuService.findAll().size();
            int db1 = cauThuService.findByDB(1).size();
            int db2 = cauThuService.findByDB(2).size();
            
            lblTongSo.setText("Tổng: " + total + " cầu thủ");
            lblDB1.setText("DB1: " + db1 + " cầu thủ");
            lblDB2.setText("DB2: " + db2 + " cầu thủ");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleThem() {
        try {
            DoiBong selectedDoiBong = cbDoiBong.getValue();
            
            CauThu ct = new CauThu(
                txtMaCT.getText(),
                txtHoTen.getText(),
                cbViTri.getValue(),
                selectedDoiBong.getMaDB()
            );
            
            // Service sẽ validate hết
            cauThuService.insert(ct, selectedDoiBong.getClb());
            
            showAlert("Success", " Thêm cầu thủ thành công!");
            clearFrom();
            rbAll.setSelected(true);
            loadData();
            
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleSua() {
        try {
            CauThu selected = tableCauThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", "Chọn cầu thủ cần sửa!");
                return;
            }
            
            DoiBong newDoiBong = cbDoiBong.getValue();
            
            CauThu updated = new CauThu(
                selected.getMaCT(),
                txtHoTen.getText(),
                cbViTri.getValue(),
                newDoiBong.getMaDB()
            );
            
            String oldCLB = selectedDoiBongForEdit.getClb();
            String newCLB = newDoiBong.getClb();
            
            // Service sẽ validate hết
            cauThuService.update(updated, oldCLB, newCLB);
            
            showAlert("Success", " Cập nhật thành công!");
            clearFrom();
            loadData();
            
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleXoa() {
        try {
            CauThu selected = tableCauThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Error", " Chọn cầu thủ cần xóa!");
                return;
            }
            
            // Confirm trước khi xóa
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText("Xác nhận xóa cầu thủ?");
            confirm.setContentText("Bạn có chắc muốn xóa: " + selected.getHoTen() + "?");
            
            if (confirm.showAndWait().get() != ButtonType.OK) {
                return;
            }
            
            String clb = selectedDoiBongForEdit.getClb();
            
            cauThuService.delete(selected.getMaCT(), clb);
            
            showAlert("Success", " Xóa thành công!");
            clearFrom();
            loadData();
            
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleTimKiem() {
        filterData();
    }
    
    @FXML
    private void handleLamMoi() {
        txtMaCT.clear();
        txtHoTen.clear();
        cbViTri.setValue(null);
        cbDoiBong.setValue(null);
        txtTimKiem.clear();
        tableCauThu.getSelectionModel().clearSelection();
        selectedDoiBongForEdit = null;
        rbAll.setSelected(true);
        loadData();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(
            title.equals("Success") ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFrom() {
        txtMaCT.clear();
        txtHoTen.clear();
        cbViTri.setValue(null);
        cbDoiBong.setValue(null);
        selectedDoiBongForEdit = null;
    }
}
