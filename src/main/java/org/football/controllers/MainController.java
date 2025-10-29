package org.football.controllers;

import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {
    
    @FXML private ScrollPane contentArea;
    @FXML private Label lblUserInfo;
    @FXML private HBox btnDashboard, btnDoiBong, btnCauThu, btnTranDau, btnQuery;
    
    private static final String ACTIVE_BG = "-fx-background-color: rgba(255,255,255,0.25);";
    private static final String TRANSPARENT_BG = "-fx-background-color: transparent;";
    private HBox currentActiveButton;
    
    @FXML
    public void initialize() {
        btnDashboard.setOnMouseClicked(e -> loadPage("Dashboard.fxml", btnDashboard));
        btnDoiBong.setOnMouseClicked(e -> loadPage("DoiBongManagement.fxml", btnDoiBong));
        btnCauThu.setOnMouseClicked(e -> loadPage("CauThuManagement.fxml", btnCauThu));
        btnTranDau.setOnMouseClicked(e -> loadPage("TranDauManagement.fxml", btnTranDau));
        btnQuery.setOnMouseClicked(e -> loadPage("QueryManagement.fxml", btnQuery));
        
        loadPage("Dashboard.fxml", btnDashboard);
    }
    
    @FXML
    private void handleLogout() {
        System.out.println("Logout clicked");
    }
    
    private void loadPage(String fxmlFile, HBox activeButton) {
        try {
            Parent page = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile));
            setContent(page);
            updateActiveButton(activeButton);
        } catch (IOException e) {
            showError("❌ Lỗi: Không thể tải trang " + fxmlFile);
            e.printStackTrace();
        }
    }
    
    private void updateActiveButton(HBox newButton) {
        if (currentActiveButton != null) {
            String style = currentActiveButton.getStyle();
            currentActiveButton.setStyle(style.replace(ACTIVE_BG, TRANSPARENT_BG));
        }
        if (newButton != null) {
            newButton.setStyle(newButton.getStyle() + " " + ACTIVE_BG);
        }
        currentActiveButton = newButton;
    }
    
    private void showComingSoon(String featureName, HBox activeButton) {
        VBox placeholder = createPlaceholder(featureName);
        setContent(placeholder);
        updateActiveButton(activeButton);
    }
    
    private VBox createPlaceholder(String featureName) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 60;");
        
        Label icon = new Label("🚧");
        icon.setStyle("-fx-font-size: 80px;");
        
        Label title = new Label("Tính năng " + featureName);
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        Label subtitle = new Label("Đang phát triển...");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
        
        box.getChildren().addAll(icon, title, subtitle);
        return box;
    }
    
    private void setContent(Parent content) {
        contentArea.setContent(content);
    }
    
    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
        setContent(errorLabel);
    }
    
    public void setUserInfo(String username) {
        lblUserInfo.setText(username);
    }
}
