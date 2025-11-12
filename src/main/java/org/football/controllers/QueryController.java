package org.football.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.football.models.CauThu;
import org.football.models.TranDau;
import org.football.services.QueryService;
import org.football.utils.AlertUtils;

import java.util.List;
import java.util.Map;

public class QueryController {
    
    // Query 1 - Tìm cầu thủ theo CLB
    @FXML private TextField txtCLB_Q1;
    @FXML private TableView<CauThu> tableQ1;
    @FXML private TableColumn<CauThu, String> colMaCT_Q1;
    @FXML private TableColumn<CauThu, String> colHoTen_Q1;
    @FXML private TableColumn<CauThu, String> colViTri_Q1;
    @FXML private TableColumn<CauThu, String> colMaDB_Q1;
    
    // Query 2 - Số trận của cầu thủ
    @FXML private TextField txtHoTen_Q2;
    @FXML private Label lblResult_Q2;
    @FXML private Label lblNumber_Q2;
    
    // Query 3 - Trận hòa theo sân
    @FXML private TextField txtSanDau_Q3;
    @FXML private Label lblResult_Q3;
    @FXML private Label lblNumber_Q3;
    
    // Query 4 - Vua phá lưới
    @FXML private TableView<Map<String, Object>> tableQ4;
    @FXML private TableColumn<Map<String, Object>, String> colMaCT_Q4;
    @FXML private TableColumn<Map<String, Object>, String> colHoTen_Q4;
    @FXML private TableColumn<Map<String, Object>, String> colViTri_Q4;
    @FXML private TableColumn<Map<String, Object>, Integer> colTongBan_Q4;
    
    // Query 5 - Trận có cầu thủ X và trọng tài Y
    @FXML private TextField txtHoTen_Q5;
    @FXML private TextField txtTrongTai_Q5;
    @FXML private TableView<TranDau> tableQ5;
    @FXML private TableColumn<TranDau, String> colMaTD_Q5;
    @FXML private TableColumn<TranDau, String> colMaDB1_Q5;
    @FXML private TableColumn<TranDau, String> colMaDB2_Q5;
    @FXML private TableColumn<TranDau, String> colTrongTai_Q5;
    @FXML private TableColumn<TranDau, String> colSanDau_Q5;
    
    // Query 6 - 2 cầu thủ cùng CLB
    @FXML private TextField txtHoTen1_Q6;
    @FXML private TextField txtHoTen2_Q6;
    @FXML private Label lblResult_Q6;
    
    // Query 7 - Cầu thủ ghi 0 bàn
    @FXML private TableView<Map<String, Object>> tableQ7;
    @FXML private TableColumn<Map<String, Object>, String> colMaCT_Q7;
    @FXML private TableColumn<Map<String, Object>, String> colHoTen_Q7;
    @FXML private TableColumn<Map<String, Object>, String> colViTri_Q7;
    @FXML private TableColumn<Map<String, Object>, Integer> colSoTran_Q7;
    
    // Query 8 - Cầu thủ >= 3 trận
    @FXML private TableView<Map<String, Object>> tableQ8;
    @FXML private TableColumn<Map<String, Object>, String> colMaCT_Q8;
    @FXML private TableColumn<Map<String, Object>, String> colHoTen_Q8;
    @FXML private TableColumn<Map<String, Object>, String> colViTri_Q8;
    @FXML private TableColumn<Map<String, Object>, Integer> colSoTran_Q8;
    
    // Query 9 - Tổng bàn của đội
    @FXML private TextField txtMaDB_Q9;
    @FXML private Label lblResult_Q9;
    @FXML private Label lblNumber_Q9;
    
    // Query 10 - Cầu thủ chưa đá
    @FXML private TableView<CauThu> tableQ10;
    @FXML private TableColumn<CauThu, String> colMaCT_Q10;
    @FXML private TableColumn<CauThu, String> colHoTen_Q10;
    @FXML private TableColumn<CauThu, String> colViTri_Q10;
    @FXML private TableColumn<CauThu, String> colMaDB_Q10;
    
    private QueryService queryService = new QueryService();
    
    @FXML
    public void initialize() {
        initTableQ1();
        initTableQ4();
        initTableQ5();
        initTableQ7();
        initTableQ8();
        initTableQ10();
        
        System.out.println("✅ QueryController initialized");
    }
    
    // === INITIALIZE TABLE COLUMNS ===
    
    private void initTableQ1() {
        colMaCT_Q1.setCellValueFactory(new PropertyValueFactory<>("maCT"));
        colHoTen_Q1.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colViTri_Q1.setCellValueFactory(new PropertyValueFactory<>("viTri"));
        colMaDB_Q1.setCellValueFactory(new PropertyValueFactory<>("maDB"));
    }
    
    private void initTableQ4() {
        colMaCT_Q4.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("maCT")));
        colHoTen_Q4.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("hoTen")));
        colViTri_Q4.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("viTri")));
        colTongBan_Q4.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>((Integer) cellData.getValue().get("tongBan")));
    }
    
    private void initTableQ5() {
        colMaTD_Q5.setCellValueFactory(new PropertyValueFactory<>("maTD"));
        colMaDB1_Q5.setCellValueFactory(new PropertyValueFactory<>("maDB1"));
        colMaDB2_Q5.setCellValueFactory(new PropertyValueFactory<>("maDB2"));
        colTrongTai_Q5.setCellValueFactory(new PropertyValueFactory<>("trongTai"));
        colSanDau_Q5.setCellValueFactory(new PropertyValueFactory<>("sanDau"));
    }
    
    private void initTableQ7() {
        colMaCT_Q7.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("maCT")));
        colHoTen_Q7.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("hoTen")));
        colViTri_Q7.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("viTri")));
        colSoTran_Q7.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>((Integer) cellData.getValue().get("soTran")));
    }
    
    private void initTableQ8() {
        colMaCT_Q8.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("maCT")));
        colHoTen_Q8.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("hoTen")));
        colViTri_Q8.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty((String) cellData.getValue().get("viTri")));
        colSoTran_Q8.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>((Integer) cellData.getValue().get("soTran")));
    }
    
    private void initTableQ10() {
        colMaCT_Q10.setCellValueFactory(new PropertyValueFactory<>("maCT"));
        colHoTen_Q10.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colViTri_Q10.setCellValueFactory(new PropertyValueFactory<>("viTri"));
        colMaDB_Q10.setCellValueFactory(new PropertyValueFactory<>("maDB"));
    }
    
    // === QUERY HANDLERS ===
    
    @FXML
    private void handleQuery1() {
        try {
            String clb = txtCLB_Q1.getText().trim();
            if (clb.isEmpty()) {
                AlertUtils.showWarning("Vui lòng nhập CLB (CLB1 hoặc CLB2)");
                return;
            }
            
            List<CauThu> result = queryService.query1_FindByCLB(clb);
            tableQ1.setItems(FXCollections.observableArrayList(result));
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Không có cầu thủ nào thuộc " + clb);
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tìm cầu thủ: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery2() {
        try {
            String hoTen = txtHoTen_Q2.getText().trim();
            if (hoTen.isEmpty()) {
                AlertUtils.showWarning("Vui lòng nhập họ tên cầu thủ");
                return;
            }
            
            int count = queryService.query2_CountMatches(hoTen);
            lblNumber_Q2.setText(String.valueOf(count));
            lblResult_Q2.setText("trận đấu");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi đếm số trận: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery3() {
        try {
            String sanDau = txtSanDau_Q3.getText().trim();
            if (sanDau.isEmpty()) {
                AlertUtils.showWarning("Vui lòng nhập sân đấu (SD1 hoặc SD2)");
                return;
            }
            
            int count = queryService.query3_CountDrawMatches(sanDau);
            lblNumber_Q3.setText(String.valueOf(count));
            lblResult_Q3.setText("trận hòa");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi đếm trận hòa: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery4() {
        try {
            List<Map<String, Object>> result = queryService.query4_TopScorers();
            tableQ4.setItems(FXCollections.observableArrayList(result));
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Chưa có thông tin bàn thắng");
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tìm vua phá lưới: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery5() {
        try {
            String hoTen = txtHoTen_Q5.getText().trim();
            String trongTai = txtTrongTai_Q5.getText().trim();
            
            if (hoTen.isEmpty() || trongTai.isEmpty()) {
                AlertUtils.showWarning("Vui lòng nhập cả họ tên cầu thủ và trọng tài");
                return;
            }
            
            List<TranDau> result = queryService.query5_FindMatchesByPlayerAndRef(hoTen, trongTai);
            tableQ5.setItems(FXCollections.observableArrayList(result));
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Không có trận đấu nào phù hợp");
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tìm trận đấu: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery6() {
        try {
            String hoTen1 = txtHoTen1_Q6.getText().trim();
            String hoTen2 = txtHoTen2_Q6.getText().trim();
            
            if (hoTen1.isEmpty() || hoTen2.isEmpty()) {
                AlertUtils.showWarning("Vui lòng nhập họ tên cả 2 cầu thủ");
                return;
            }
            
            boolean sameCLB = queryService.query6_SameCLB(hoTen1, hoTen2);
            
            if (sameCLB) {
                lblResult_Q6.setText("✅ Hai cầu thủ cùng CLB");
                lblResult_Q6.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-font-size: 16px;");
            } else {
                lblResult_Q6.setText("❌ Hai cầu thủ khác CLB");
                lblResult_Q6.setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-font-size: 16px;");
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi so sánh CLB: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery7() {
        try {
            List<Map<String, Object>> result = queryService.query7_PlayersWithZeroGoals();
            tableQ7.setItems(FXCollections.observableArrayList(result));
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Không có cầu thủ nào ghi 0 bàn");
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tìm cầu thủ: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery8() {
        try {
            List<Map<String, Object>> result = queryService.query8_ActivePlayers();
            tableQ8.setItems(FXCollections.observableArrayList(result));
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Không có cầu thủ nào tham gia >= 3 trận");
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tìm cầu thủ: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery9() {
        try {
            String maDB = txtMaDB_Q9.getText().trim();
            if (maDB.isEmpty()) {
                AlertUtils.showWarning("Vui lòng nhập mã đội bóng");
                return;
            }
            
            int totalGoals = queryService.query9_TeamTotalGoals(maDB);
            lblNumber_Q9.setText(String.valueOf(totalGoals));
            lblResult_Q9.setText("bàn thắng");
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tính tổng bàn: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleQuery10() {
        try {
            List<CauThu> result = queryService.query10_PlayersNeverPlayed();
            tableQ10.setItems(FXCollections.observableArrayList(result));
            
            if (result.isEmpty()) {
                AlertUtils.showInfo("Tất cả cầu thủ đều đã thi đấu");
            }
        } catch (Exception e) {
            AlertUtils.showError("Lỗi khi tìm cầu thủ: " + e.getMessage());
        }
    }
}

