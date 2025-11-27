package org.football.services;

import org.football.dao.ThamGiaDAO;
import org.football.models.ThamGia;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ThamGiaService {
    private ThamGiaDAO dao = new ThamGiaDAO();
    
    // Get participation records for a match
    public List<ThamGia> findByMaTD(String maTD, String sanDau) throws SQLException {
        Connection conn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        return dao.findByMaTD(maTD, conn);
    }
    
    
    public void saveParticipation(String maTD, String sanDau, List<ThamGia> records) throws Exception {
        // Target DB dựa trên SanDau của trận đấu
        Connection targetConn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        
        Connection otherConn = sanDau.equals("SD1")
            ? DatabaseConnection.getConnection2()
            : DatabaseConnection.getConnection1();
        
        // Delete old records from target DB only
        dao.deleteByMaTD(maTD, targetConn);
        
        // Insert all ThamGia to target DB (where TranDau is)
        for (ThamGia tg : records) {
            // Đảm bảo CauThu tồn tại trong target DB
            ensureCauThuExists(tg.getMaCT(), targetConn, otherConn);
            
            if (!dao.insert(tg, targetConn)) {
                throw new Exception("Lỗi lưu dữ liệu tham gia cho cầu thủ " + tg.getMaCT());
            }
        }
    }
    
    /**
     * Đảm bảo CauThu tồn tại trong DB đích
     * Nếu chưa có → sao chép từ DB nguồn
     */
    private void ensureCauThuExists(String maCT, Connection targetConn, Connection sourceConn) throws SQLException {
        // Kiểm tra CauThu đã tồn tại trong target chưa
        String checkSql = "SELECT COUNT(*) FROM CauThu WHERE MaCT = ?";
        try (PreparedStatement psCheck = targetConn.prepareStatement(checkSql)) {
            psCheck.setString(1, maCT);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Đã tồn tại
            }
        }
        
        // Chưa tồn tại → Lấy thông tin từ source và copy sang target
        String selectSql = "SELECT CT.*, DB.TenDB, DB.CLB FROM CauThu CT " +
                          "JOIN DoiBong DB ON CT.MaDB = DB.MaDB WHERE CT.MaCT = ?";
        try (PreparedStatement psSelect = sourceConn.prepareStatement(selectSql)) {
            psSelect.setString(1, maCT);
            ResultSet rs = psSelect.executeQuery();
            
            if (rs.next()) {
                String maDB = rs.getString("MaDB");
                
                // Đảm bảo DoiBong tồn tại trong target trước
                ensureDoiBongExists(maDB, rs.getString("TenDB"), rs.getString("CLB"), targetConn);
                
                // Copy CauThu sang target
                String insertSql = "INSERT INTO CauThu (MaCT, HoTen, ViTri, MaDB) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psInsert = targetConn.prepareStatement(insertSql)) {
                    psInsert.setString(1, maCT);
                    psInsert.setString(2, rs.getString("HoTen"));
                    psInsert.setString(3, rs.getString("ViTri"));
                    psInsert.setString(4, maDB);
                    psInsert.executeUpdate();
                }
            }
        }
    }
    
    /**
     * Đảm bảo DoiBong tồn tại trong DB đích
     */
    private void ensureDoiBongExists(String maDB, String tenDB, String clb, Connection targetConn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM DoiBong WHERE MaDB = ?";
        try (PreparedStatement psCheck = targetConn.prepareStatement(checkSql)) {
            psCheck.setString(1, maDB);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Đã tồn tại
            }
        }
        
        // Chưa tồn tại → Insert
        String insertSql = "INSERT INTO DoiBong (MaDB, TenDB, CLB) VALUES (?, ?, ?)";
        try (PreparedStatement psInsert = targetConn.prepareStatement(insertSql)) {
            psInsert.setString(1, maDB);
            psInsert.setString(2, tenDB);
            psInsert.setString(3, clb);
            psInsert.executeUpdate();
        }
    }
    
    // Delete all participation when deleting match
    public void deleteByMaTD(String maTD, String sanDau) throws SQLException {
        Connection conn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        dao.deleteByMaTD(maTD, conn);
    }
    
    // Count how many matches a player participated in (for validation before delete)
    public int countMatchesByPlayer(String maCT) throws SQLException {
        int count1 = dao.countByMaCT(maCT, DatabaseConnection.getConnection1());
        int count2 = dao.countByMaCT(maCT, DatabaseConnection.getConnection2());
        return count1 + count2;
    }
    
    // Count how many participations in a match (for validation before delete match)
    public int countByMaTD(String maTD) throws SQLException {
        // Try both databases (match could be in either SD1 or SD2)
        int count1 = dao.countByMaTD(maTD, DatabaseConnection.getConnection1());
        int count2 = dao.countByMaTD(maTD, DatabaseConnection.getConnection2());
        return count1 + count2;
    }
    
    // Get total goals from all matches (for dashboard)
    public int getTotalGoalsAll() throws SQLException {
        int goalsDB1 = dao.getTotalGoals(DatabaseConnection.getConnection1());
        int goalsDB2 = dao.getTotalGoals(DatabaseConnection.getConnection2());
        return goalsDB1 + goalsDB2;
    }
}
