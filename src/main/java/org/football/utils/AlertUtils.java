package org.football.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class để xử lý tất cả Alert dialogs
 * Giảm code duplicate trong controllers
 */
public class AlertUtils {
    
    /**
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     */
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Hiển thị thông báo thành công
     * @param message Nội dung thông báo
     */
    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Hiển thị cảnh báo
     * @param message Nội dung cảnh báo
     */
    public static void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Cảnh báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Hiển thị dialog xác nhận
     * @param title Tiêu đề
     * @param header Header text
     * @param content Nội dung
     * @return true nếu user click OK, false nếu cancel
     */
    public static boolean showConfirm(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Hiển thị dialog xác nhận xóa
     * @param itemName Tên item cần xóa (VD: "Trận đấu TD01")
     * @return true nếu xác nhận xóa
     */
    public static boolean confirmDelete(String itemName) {
        return showConfirm(
            "Xác nhận xóa",
            "Xóa " + itemName,
            "Bạn có chắc muốn xóa? Hành động này không thể hoàn tác!"
        );
    }
    
    /**
     * Hiển thị dialog xác nhận di chuyển dữ liệu giữa 2 DB
     * @param oldDB DB cũ (VD: "SD1")
     * @param newDB DB mới (VD: "SD2")
     * @return true nếu xác nhận di chuyển
     */
    public static boolean confirmMigration(String oldDB, String newDB) {
        return showConfirm(
            "Xác nhận di chuyển",
            "Thay đổi phân mảnh dữ liệu",
            "Bạn đang thay đổi từ " + oldDB + " sang " + newDB + ".\n" +
            "Dữ liệu sẽ được di chuyển sang mảnh DB khác.\n" +
            "Tiếp tục?"
        );
    }
}
