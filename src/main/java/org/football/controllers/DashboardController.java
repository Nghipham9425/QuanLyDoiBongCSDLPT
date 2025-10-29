package org.football.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DashboardController {
    
    @FXML private Label lblDoiBong;
    @FXML private Label lblCauThu;
    @FXML private Label lblTranDau;
    @FXML private Label lblBanThang;
    
    @FXML private VBox lblDB1Stats;
    @FXML private VBox lblDB2Stats;
    
    @FXML
    public void initialize() {
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        
        lblDoiBong.setText("24");
        lblCauThu.setText("158");
        lblTranDau.setText("36");
        lblBanThang.setText("245");
        
    }
}
