package org.football.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.football.services.DoiBongService;
import org.football.services.CauThuService;
import org.football.services.TranDauService;
import org.football.services.ThamGiaService;
import org.football.services.QueryService;

import java.util.List;
import java.util.Map;

public class DashboardController {
    
    @FXML private Label lblDoiBong;
    @FXML private Label lblCauThu;
    @FXML private Label lblTranDau;
    @FXML private Label lblBanThang;
    
    @FXML private Label lblDB1Teams;
    @FXML private Label lblDB1Players;
    @FXML private Label lblDB1Matches;
    
    @FXML private Label lblDB2Teams;
    @FXML private Label lblDB2Players;
    @FXML private Label lblDB2Matches;
    
    @FXML private VBox topScorerContainer;
    
    private DoiBongService doiBongService = new DoiBongService();
    private CauThuService cauThuService = new CauThuService();
    private TranDauService tranDauService = new TranDauService();
    private ThamGiaService thamGiaService = new ThamGiaService();
    private QueryService queryService = new QueryService();
    
    @FXML
    public void initialize() {
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        try {
            // Tổng số đội bóng (merge cả 2 DB)
            int totalTeams = doiBongService.findAll().size();
            lblDoiBong.setText(String.valueOf(totalTeams));
            
            // Tổng số cầu thủ
            int totalPlayers = cauThuService.findAll().size();
            lblCauThu.setText(String.valueOf(totalPlayers));
            
            // Tổng số trận đấu
            int totalMatches = tranDauService.findAll().size();
            lblTranDau.setText(String.valueOf(totalMatches));
            
            // Tổng bàn thắng (tính từ ThamGia)
            int totalGoals = thamGiaService.getTotalGoalsAll();
            lblBanThang.setText(String.valueOf(totalGoals));
            
            // Thống kê từng DB
            loadDB1Stats();
            loadDB2Stats();
            
            // Load top scorers
            loadTopScorers();
            
            System.out.println("✅ Dashboard loaded: " + totalTeams + " teams, " + totalPlayers + " players, " + totalMatches + " matches, " + totalGoals + " goals");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values
            lblDoiBong.setText("0");
            lblCauThu.setText("0");
            lblTranDau.setText("0");
            lblBanThang.setText("0");
        }
    }
    
    private void loadDB1Stats() {
        try {
            int teamsDB1 = doiBongService.findByDB(1).size();
            int playersDB1 = cauThuService.findByDB(1).size();
            int matchesDB1 = tranDauService.findByDB(1).size();
            
            if (lblDB1Teams != null) lblDB1Teams.setText(teamsDB1 + " đội");
            if (lblDB1Players != null) lblDB1Players.setText(playersDB1 + " cầu thủ");
            if (lblDB1Matches != null) lblDB1Matches.setText(matchesDB1 + " trận (SD1)");
        } catch (Exception e) {
            System.err.println("Error loading DB1 stats: " + e.getMessage());
        }
    }
    
    private void loadDB2Stats() {
        try {
            int teamsDB2 = doiBongService.findByDB(2).size();
            int playersDB2 = cauThuService.findByDB(2).size();
            int matchesDB2 = tranDauService.findByDB(2).size();
            
            if (lblDB2Teams != null) lblDB2Teams.setText(teamsDB2 + " đội");
            if (lblDB2Players != null) lblDB2Players.setText(playersDB2 + " cầu thủ");
            if (lblDB2Matches != null) lblDB2Matches.setText(matchesDB2 + " trận (SD2)");
        } catch (Exception e) {
            System.err.println("Error loading DB2 stats: " + e.getMessage());
        }
    }
    
    private void loadTopScorers() {
        try {
            if (topScorerContainer == null) return;
            
            // Clear existing content
            topScorerContainer.getChildren().clear();
            
            // Get top 3 scorers
            List<Map<String, Object>> topScorers = queryService.query4_TopScorers();
            
            if (topScorers.isEmpty()) {
                Label noData = new Label("Chưa có dữ liệu bàn thắng");
                noData.setStyle("-fx-text-fill: #999; -fx-font-size: 13px;");
                topScorerContainer.getChildren().add(noData);
                return;
            }
            
            // Show top 3 only
            int limit = Math.min(3, topScorers.size());
            String[] medals = {"🥇", "🥈", "🥉"};
            String[] colors = {"#ffd700", "#c0c0c0", "#cd7f32"};
            
            for (int i = 0; i < limit; i++) {
                Map<String, Object> scorer = topScorers.get(i);
                String name = (String) scorer.get("hoTen");
                int goals = (Integer) scorer.get("tongBan");
                
                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);
                
                // Medal icon
                Label medal = new Label(medals[i]);
                medal.setStyle("-fx-font-size: 20px;");
                
                // Player name
                Label lblName = new Label(name);
                lblName.setStyle("-fx-font-size: 14px; -fx-font-weight: 500; -fx-text-fill: #333;");
                
                // Spacer
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                
                // Goals
                Label lblGoals = new Label(goals + " bàn");
                lblGoals.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + colors[i] + ";");
                
                row.getChildren().addAll(medal, lblName, spacer, lblGoals);
                topScorerContainer.getChildren().add(row);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading top scorers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
