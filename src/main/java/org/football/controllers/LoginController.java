package org.football.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;

public class LoginController {
    
    @FXML private ComboBox<String> cbCLB;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private ProgressIndicator progressLogin;
    
    // Lưu CLB đã chọn toàn cục
    private static String selectedCLB = "CLB1 - SD1 (Database 1)";
    
    @FXML
    public void initialize() {
        // Populate ComboBox items từ Java code
        cbCLB.getItems().addAll(
            "CLB1 - SD1 (Database 1)",
            "CLB2 - SD2 (Database 2)"
        );
        cbCLB.setValue("CLB1 - SD1 (Database 1)");
        txtPassword.setOnAction(e -> handleLogin());
    }
    
    public static String getSelectedCLB() {
        return selectedCLB;
    }
    
    @FXML
    private void handleLogin() {
        if (cbCLB.getValue() == null) {
            showError(" Vui lòng chọn chi nhánh!");
            return;
        }
        
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }
        
        setLoading(true);
        selectedCLB = cbCLB.getValue();
        
        new Thread(() -> {
            try {
                Connection conn1 = DatabaseConnection.getConnection1();
                Connection conn2 = DatabaseConnection.getConnection2();
                
                if (conn1 == null || conn2 == null) {
                    Platform.runLater(() -> {
                        showError(" Không thể kết nối đến cơ sở dữ liệu!");
                        setLoading(false);
                    });
                    return;
                }
                
                // Kiểm tra tài khoản - chấp nhận sa/123456 hoặc admin/admin
                boolean isValid = false;
                if ("sa".equals(username) && "123456".equals(password)) {
                    isValid = true;
                } else if ("admin".equals(username) && "admin".equals(password)) {
                    isValid = true;
                }
                
                if (isValid) {
                    Platform.runLater(() -> loadMainWindow(username));
                } else {
                    Platform.runLater(() -> {
                        showError(" Tên đăng nhập hoặc mật khẩu không đúng!");
                        setLoading(false);
                    });
                }
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError(" Lỗi kết nối: " + e.getMessage());
                    setLoading(false);
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
    
    private void setLoading(boolean loading) {
        btnLogin.setDisable(loading);
        btnLogin.setText(loading ? " Đang kết nối..." : "Đăng nhập");
        progressLogin.setVisible(loading);
        progressLogin.setManaged(loading);
        if (loading) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }
    }
    
    private void loadMainWindow(String username) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Main.fxml"));
            
            Stage stage = new Stage();
            stage.setTitle("Hệ thống Quản lý Đội bóng - " + selectedCLB);
            stage.setScene(new Scene(loader.load()));
            stage.setMaximized(true);
            
            MainController controller = loader.getController();
            controller.setUserInfo(username, selectedCLB);
            
            stage.show();
            ((Stage) btnLogin.getScene().getWindow()).close();
            
        } catch (Exception e) {
            showError(" Lỗi tải giao diện chính!");
            setLoading(false);
            e.printStackTrace();
        }
    }
}
