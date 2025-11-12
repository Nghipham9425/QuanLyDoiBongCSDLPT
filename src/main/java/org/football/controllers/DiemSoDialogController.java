package org.football.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.football.models.CauThu;
import org.football.models.ThamGia;
import org.football.models.TranDau;
import org.football.services.CauThuService;
import org.football.services.ThamGiaService;
import org.football.utils.AlertUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller cho dialog nhập điểm trận đấu
 * Sử dụng FXML thay vì hard-code UI
 */
public class DiemSoDialogController {
    
    @FXML private Label lblMatchInfo;
    @FXML private Label lblStadiumRef;
    @FXML private Label lblTeam1;
    @FXML private Label lblTeam2;
    @FXML private Label lblTyso;
    
    @FXML private VBox vboxTeam1Players;
    @FXML private VBox vboxTeam2Players;
    
    @FXML private Button btnLuu;
    @FXML private Button btnDong;
    
    private TranDau tran;
    private Stage stage;
    private ThamGiaService thamGiaService;
    private CauThuService cauThuService;
    private Map<String, PlayerRow> playerRowMap = new HashMap<>();
    
    // Inner class để quản lý mỗi dòng cầu thủ
    class PlayerRow {
        CheckBox checkBox;
        CauThu cauThu;
        TextField txtSoBan;
        Label lblStatus;
        
        PlayerRow(CauThu ct) {
            this.cauThu = ct;
            
            // Checkbox với tên cầu thủ
            String displayName = ct.getMaCT() + " - " + ct.getHoTen() + " (" + ct.getViTri() + ")";
            this.checkBox = new CheckBox(displayName);
            this.checkBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #424242;");
            
            // TextField nhập số bàn
            this.txtSoBan = new TextField("0");
            this.txtSoBan.setPrefWidth(60);
            this.txtSoBan.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
            this.txtSoBan.setDisable(true);
            
            // Label trạng thái
            this.lblStatus = new Label("");
            this.lblStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #4caf50;");
            this.lblStatus.setPrefWidth(100);
            
            // Listener: Khi tick → enable TextField
            this.checkBox.selectedProperty().addListener((obs, old, newVal) -> {
                txtSoBan.setDisable(!newVal);
                if (!newVal) txtSoBan.setText("0");
                lblStatus.setText(newVal ? "🎯 Ra sân" : "");
                updateTyso();
            });
            
            // Listener: Khi thay đổi số bàn → cập nhật tỷ số
            this.txtSoBan.textProperty().addListener((obs, old, newVal) -> {
                updateTyso();
            });
        }
        
        int getSoBan() {
            try {
                return Integer.parseInt(txtSoBan.getText());
            } catch (Exception e) {
                return 0;
            }
        }
        
        HBox createRow() {
            HBox row = new HBox(10);
            row.setPrefHeight(35);
            row.setStyle("-fx-padding: 5; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
            
            HBox playerBox = new HBox(8);
            playerBox.setPrefWidth(250);
            playerBox.setStyle("-fx-alignment: CENTER_LEFT;");
            playerBox.getChildren().add(checkBox);
            
            HBox scoreBox = new HBox();
            scoreBox.setPrefWidth(80);
            scoreBox.getChildren().add(txtSoBan);
            
            row.getChildren().addAll(playerBox, scoreBox, lblStatus);
            return row;
        }
    }
    
    /**
     * Static method để mở dialog
     */
    public static void show(TranDau tran) {
        try {
            FXMLLoader loader = new FXMLLoader(
                DiemSoDialogController.class.getResource("/fxml/DiemSoDialog.fxml")
            );
            Parent root = loader.load();
            
            DiemSoDialogController controller = loader.getController();
            controller.tran = tran;
            controller.thamGiaService = new ThamGiaService();
            controller.cauThuService = new CauThuService();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Nhập điểm - Trận " + tran.getMaTD());
            stage.setScene(new Scene(root));
            
            controller.stage = stage;
            controller.initData();
            
            stage.showAndWait();
            
        } catch (Exception e) {
            AlertUtils.showError(" Lỗi mở dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load dữ liệu ban đầu
     */
    private void initData() {
        // Set thông tin header
        lblMatchInfo.setText(tran.getMaDB1() + " vs " + tran.getMaDB2());
        lblStadiumRef.setText("Sân: " + tran.getSanDau() + " | Trọng tài: " + tran.getTrongTai());
        
        // Load danh sách cầu thủ 2 đội
        try {
            List<CauThu> team1Players = cauThuService.findByMaDB(tran.getMaDB1());
            List<CauThu> team2Players = cauThuService.findByMaDB(tran.getMaDB2());
            
            lblTeam1.setText("⚽ Đội 1: " + tran.getMaDB1());
            lblTeam2.setText("⚽ Đội 2: " + tran.getMaDB2());
            
            // Tạo rows cho đội 1
            for (CauThu ct : team1Players) {
                PlayerRow row = new PlayerRow(ct);
                playerRowMap.put(ct.getMaCT(), row);
                vboxTeam1Players.getChildren().add(row.createRow());
            }
            
            // Tạo rows cho đội 2
            for (CauThu ct : team2Players) {
                PlayerRow row = new PlayerRow(ct);
                playerRowMap.put(ct.getMaCT(), row);
                vboxTeam2Players.getChildren().add(row.createRow());
            }
            
            loadExistingData();
            
        } catch (Exception e) {
            AlertUtils.showError(" Lỗi load dữ liệu: " + e.getMessage());
        }
    }
    

    private void loadExistingData() {
        try {
            List<ThamGia> existingData = thamGiaService.findByMaTD(tran.getMaTD(), tran.getSanDau());
            
            for (ThamGia tg : existingData) {
                PlayerRow row = playerRowMap.get(tg.getMaCT());
                if (row != null) {
                    row.checkBox.setSelected(true);
                    row.txtSoBan.setText(String.valueOf(tg.getSoTrai()));
                }
            }
            
            updateTyso();
        } catch (Exception e) {
            System.out.println(" Chưa có dữ liệu tham gia: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật tỷ số real-time
     */
    private void updateTyso() {
        try {
            int scoreTeam1 = 0;
            int scoreTeam2 = 0;
            
            List<CauThu> team1 = cauThuService.findByMaDB(tran.getMaDB1());
            List<CauThu> team2 = cauThuService.findByMaDB(tran.getMaDB2());
            
            // Tính tổng bàn thắng đội 1
            for (CauThu ct : team1) {
                PlayerRow row = playerRowMap.get(ct.getMaCT());
                if (row != null && row.checkBox.isSelected()) {
                    scoreTeam1 += row.getSoBan();
                }
            }
            
            // Tính tổng bàn thắng đội 2
            for (CauThu ct : team2) {
                PlayerRow row = playerRowMap.get(ct.getMaCT());
                if (row != null && row.checkBox.isSelected()) {
                    scoreTeam2 += row.getSoBan();
                }
            }
            
            lblTyso.setText(" Tỷ số: " + scoreTeam1 + " - " + scoreTeam2);
            
        } catch (Exception e) {
            lblTyso.setText(" Tỷ số: 0 - 0");
        }
    }
    
    /**
     * Handle nút Lưu
     */
    @FXML
    private void handleLuu() {
        try {
            List<ThamGia> records = new ArrayList<>();
            
            // Thu thập tất cả cầu thủ được tick
            for (Map.Entry<String, PlayerRow> entry : playerRowMap.entrySet()) {
                PlayerRow row = entry.getValue();
                if (row.checkBox.isSelected()) {
                    records.add(new ThamGia(
                        tran.getMaTD(),
                        row.cauThu.getMaCT(),
                        row.getSoBan()
                    ));
                }
            }
            
            if (records.isEmpty()) {
                AlertUtils.showError(" Chưa chọn cầu thủ nào tham gia!");
                return;
            }
            
            // Lưu vào database
            thamGiaService.saveParticipation(tran.getMaTD(), tran.getSanDau(), records);
            
            AlertUtils.showInfo(
                " Đã lưu kết quả trận đấu!\n" +
                lblTyso.getText() + "\n" +
                "Số cầu thủ tham gia: " + records.size()
            );
            
            stage.close();
            
        } catch (Exception e) {
            AlertUtils.showError(" Lỗi lưu dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Handle nút Đóng
     */
    @FXML
    private void handleDong() {
        stage.close();
    }
}
