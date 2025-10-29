package org.football.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class QueryController {
    
    // Query 1
    @FXML private TextField txtCLB_Q1;
    @FXML private TableView tableQ1;
    
    // Query 2
    @FXML private TextField txtHoTen_Q2;
    @FXML private Label lblResult_Q2;
    
    // Query 3
    @FXML private TextField txtSanDau_Q3;
    @FXML private Label lblResult_Q3;
    
    // Query 4
    @FXML private TableView tableQ4;
    
    // Query 5
    @FXML private TextField txtHoTen_Q5, txtTrongTai_Q5;
    @FXML private TableView tableQ5;
    
    // Query 6
    @FXML private TextField txtHoTen1_Q6, txtHoTen2_Q6;
    @FXML private Label lblResult_Q6;
    
    // Query 7
    @FXML private TableView tableQ7;
    
    // Query 8
    @FXML private TableView tableQ8;
    
    // Query 9
    @FXML private TextField txtMaDB_Q9;
    @FXML private Label lblResult_Q9;
    
    // Query 10
    @FXML private TableView tableQ10;
    
    @FXML
    public void initialize() {
        System.out.println("✅ QueryController initialized");
    }
    
    @FXML
    private void handleQuery1() {
        String clb = txtCLB_Q1.getText();
        System.out.println("Q1: Tìm cầu thủ theo CLB = " + clb);
        // TODO: Query DB theo CLB → hiển thị vào tableQ1
    }
    
    @FXML
    private void handleQuery2() {
        String hoTen = txtHoTen_Q2.getText();
        System.out.println("Q2: Đếm trận của cầu thủ " + hoTen);
        // TODO: JOIN CauThu + ThamGia → COUNT
        lblResult_Q2.setText("Số trận: 0");
    }
    
    @FXML
    private void handleQuery3() {
        String sanDau = txtSanDau_Q3.getText();
        System.out.println("Q3: Trận hòa tại sân " + sanDau);
        // TODO: Query TranDau + ThamGia → WHERE SUM(SoTrai) đội 1 = đội 2
        lblResult_Q3.setText("Số trận hòa: 0");
    }
    
    @FXML
    private void handleQuery4() {
        System.out.println("Q4: Vua phá lưới");
        // TODO: UNION 2 DB → GROUP BY MaCT → MAX(SUM(SoTrai))
    }
    
    @FXML
    private void handleQuery5() {
        String hoTen = txtHoTen_Q5.getText();
        String trongTai = txtTrongTai_Q5.getText();
        System.out.println("Q5: Trận có CT=" + hoTen + " và TT=" + trongTai);
        // TODO: JOIN CauThu + ThamGia + TranDau → WHERE
    }
    
    @FXML
    private void handleQuery6() {
        String hoTen1 = txtHoTen1_Q6.getText();
        String hoTen2 = txtHoTen2_Q6.getText();
        System.out.println("Q6: So sánh CLB của " + hoTen1 + " và " + hoTen2);
        // TODO: Find CLB của 2 CT → compare
        lblResult_Q6.setText("✅ Cùng CLB / ❌ Khác CLB");
    }
    
    @FXML
    private void handleQuery7() {
        System.out.println("Q7: CT ghi 0 bàn");
        // TODO: JOIN CauThu + ThamGia WHERE SoTrai = 0
    }
    
    @FXML
    private void handleQuery8() {
        System.out.println("Q8: CT tham gia ≥3 trận");
        // TODO: JOIN CauThu + ThamGia → GROUP BY → HAVING COUNT >= 3
    }
    
    @FXML
    private void handleQuery9() {
        String maDB = txtMaDB_Q9.getText();
        System.out.println("Q9: Tổng bàn của đội " + maDB);
        // TODO: JOIN TranDau + ThamGia + CauThu WHERE MaDB = ...
        lblResult_Q9.setText("Tổng bàn thắng: 0");
    }
    
    @FXML
    private void handleQuery10() {
        System.out.println("Q10: CT chưa thi đấu");
        // TODO: LEFT JOIN CauThu with ThamGia WHERE MaTD IS NULL
    }
}
