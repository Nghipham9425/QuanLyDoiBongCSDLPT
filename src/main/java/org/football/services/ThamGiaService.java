package org.football.services;

import org.football.dao.ThamGiaDAO;
import org.football.models.ThamGia;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class ThamGiaService {
    private ThamGiaDAO dao = new ThamGiaDAO();
    
    // Get participation records for a match
    public List<ThamGia> findByMaTD(String maTD, String sanDau) throws SQLException {
        Connection conn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        return dao.findByMaTD(maTD, conn);
    }
    
    // Save/Update participation (called from DiemSoDialog)
    // FIXED: Handle cross-CLB matches - save to both DBs based on player's team
    public void saveParticipation(String maTD, String sanDau, List<ThamGia> records) throws Exception {
        Connection conn1 = DatabaseConnection.getConnection1();
        Connection conn2 = DatabaseConnection.getConnection2();
        
        // Delete old records from BOTH databases
        dao.deleteByMaTD(maTD, conn1);
        dao.deleteByMaTD(maTD, conn2);
        
        // Separate records by player's CLB
        List<ThamGia> recordsForDB1 = new ArrayList<>();
        List<ThamGia> recordsForDB2 = new ArrayList<>();
        
        for (ThamGia tg : records) {
            // Check which DB has this player by trying to find in DB1 first
            String sqlCheck = "SELECT MaCT FROM CauThu WHERE MaCT = ?";
            boolean foundInDB1 = false;
            boolean foundInDB2 = false;
            
            try (java.sql.PreparedStatement stmt = conn1.prepareStatement(sqlCheck)) {
                stmt.setString(1, tg.getMaCT());
                java.sql.ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    foundInDB1 = true;
                }
            } catch (SQLException e) {
                // Ignore
            }
            
            if (!foundInDB1) {
                try (java.sql.PreparedStatement stmt = conn2.prepareStatement(sqlCheck)) {
                    stmt.setString(1, tg.getMaCT());
                    java.sql.ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        foundInDB2 = true;
                    }
                } catch (SQLException e) {
                    // Ignore
                }
            }
            
            if (foundInDB1) {
                recordsForDB1.add(tg);
            } else if (foundInDB2) {
                recordsForDB2.add(tg);
            } else {
                throw new Exception("Cầu thủ " + tg.getMaCT() + " không tồn tại trong hệ thống!");
            }
        }
        
        // Insert to DB1
        for (ThamGia tg : recordsForDB1) {
            if (!dao.insert(tg, conn1)) {
                throw new Exception("Lỗi lưu dữ liệu tham gia vào DB1!");
            }
        }
        
        // Insert to DB2
        for (ThamGia tg : recordsForDB2) {
            if (!dao.insert(tg, conn2)) {
                throw new Exception("Lỗi lưu dữ liệu tham gia vào DB2!");
            }
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
