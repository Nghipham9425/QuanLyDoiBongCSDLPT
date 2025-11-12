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
import org.football.services.ThamGiaService;
import org.football.utils.AlertUtils;

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
        
        // Lấy CLB từ LoginController
        String selectedCLB = LoginController.getSelectedCLB();
        boolean isClb1 = selectedCLB.contains("CLB1");
        
        // Load đội bóng chỉ từ branch hiện tại
        loadDoiBong(isClb1);
        
        // Toán tử 3 ngôi: hiển thị/ẩn RadioButton tương ứng
        rbAll.setVisible(true);
        rbAll.setManaged(true);
        rbCLB1.setVisible(isClb1);
        rbCLB1.setManaged(isClb1);
        rbCLB2.setVisible(!isClb1);
        rbCLB2.setManaged(!isClb1);
        
        // Mặc định chọn chi nhánh hiện tại
        if (isClb1) {
            rbCLB1.setSelected(true);
        } else {
            rbCLB2.setSelected(true);
        }
        
        setupTable();
        setupListeners();
        loadData();
    }
    
    // Load ComboBox đội bóng với DoiBong object - chỉ branch hiện tại
    private void loadDoiBong(boolean isClb1) {
        try {
            List<DoiBong> doiBongList;
            if (isClb1) {
                doiBongList = doiBongService.findByDB(1);  // DB1 = CLB1
            } else {
                doiBongList = doiBongService.findByDB(2);  // DB2 = CLB2
            }
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
            AlertUtils.showError("Lỗi load đội bóng: " + e.getMessage());
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
            AlertUtils.showError("Lỗi load dữ liệu: " + e.getMessage());
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
            
            AlertUtils.showInfo("✅ Thêm cầu thủ thành công!");
            clearFrom();
            loadData();
            
        } catch (Exception e) {
            AlertUtils.showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleSua() {
        try {
            CauThu selected = tableCauThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError("❌ Chọn cầu thủ cần sửa!");
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
            
            AlertUtils.showInfo("✅ Cập nhật thành công!");
            clearFrom();
            loadData();
            
        } catch (Exception e) {
            AlertUtils.showError(e.getMessage());
        }
    }
    
    @FXML
    private void handleXoa() {
        try {
            CauThu selected = tableCauThu.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertUtils.showError("❌ Chọn cầu thủ cần xóa!");
                return;
            }
            
            // Kiểm tra xem cầu thủ có tham gia trận đấu nào không
            ThamGiaService thamGiaService = new ThamGiaService();
            int matchCount = thamGiaService.countMatchesByPlayer(selected.getMaCT());
            
            if (matchCount > 0) {
                AlertUtils.showError("❌ Không thể xóa cầu thủ!\n\n" +
                    "Cầu thủ " + selected.getHoTen() + " đã tham gia " + matchCount + " trận đấu.\n" +
                    "Vui lòng xóa dữ liệu tham gia trận đấu trước.");
                return;
            }
            
            // Confirm trước khi xóa
            if (!AlertUtils.confirmDelete("cầu thủ " + selected.getHoTen())) {
                return;
            }
            
            String clb = selectedDoiBongForEdit.getClb();
            
            cauThuService.delete(selected.getMaCT(), clb);
            
            AlertUtils.showInfo("✅ Xóa thành công!");
            clearFrom();
            loadData();
            
        } catch (Exception e) {
            AlertUtils.showError(e.getMessage());
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
        loadData();
    }

    private void clearFrom() {
        txtMaCT.clear();
        txtHoTen.clear();
        cbViTri.setValue(null);
        cbDoiBong.setValue(null);
        selectedDoiBongForEdit = null;
    }
}
