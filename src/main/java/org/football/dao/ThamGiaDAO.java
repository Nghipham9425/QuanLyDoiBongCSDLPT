package org.football.dao;

import org.football.models.ThamGia;
import org.football.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThamGiaDAO {
    
    // Get total goals for a team in a match
    public int getTotalGoalsByTeam(String maTD, String maDB, Connection conn) throws SQLException {
        String sql = "SELECT SUM(tg.SoTrai) AS TongBan " +
                     "FROM ThamGia tg " +
                     "JOIN CauThu ct ON tg.MaCT = ct.MaCT " +
                     "WHERE tg.MaTD = ? AND ct.MaDB = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maTD);
            stmt.setString(2, maDB);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TongBan");
            }
        }
        return 0;
    }
    
    // Get score string for a match "X-Y"
    public String getMatchScore(String maTD, String maDB1, String maDB2) throws SQLException {
        int scoreDB1 = 0;
        int scoreDB2 = 0;
        
        // Try DB1 first
        try (Connection conn1 = DatabaseConnection.getConnection1()) {
            scoreDB1 = getTotalGoalsByTeam(maTD, maDB1, conn1);
            scoreDB2 = getTotalGoalsByTeam(maTD, maDB2, conn1);
            
            if (scoreDB1 > 0 || scoreDB2 > 0) {
                return scoreDB1 + "-" + scoreDB2;
            }
        } catch (Exception e) {

        }
        
        // Try DB2
        try (Connection conn2 = DatabaseConnection.getConnection2()) {
            scoreDB1 = getTotalGoalsByTeam(maTD, maDB1, conn2);
            scoreDB2 = getTotalGoalsByTeam(maTD, maDB2, conn2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return scoreDB1 + "-" + scoreDB2;
    }
    
    // Get all participation records for a match
    public List<ThamGia> findByMaTD(String maTD, Connection conn) throws SQLException {
        List<ThamGia> list = new ArrayList<>();
        String sql = "SELECT * FROM ThamGia WHERE MaTD = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maTD);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                list.add(new ThamGia(
                    rs.getString("MaTD"),
                    rs.getString("MaCT"),
                    rs.getInt("SoTrai")
                ));
            }
        }
        return list;
    }
    
    // INSERT
    public boolean insert(ThamGia tg, Connection conn) throws SQLException {
        String sql = "INSERT INTO ThamGia (MaTD, MaCT, SoTrai) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tg.getMaTD());
            stmt.setString(2, tg.getMaCT());
            stmt.setInt(3, tg.getSoTrai());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // UPDATE
    public boolean update(ThamGia tg, Connection conn) throws SQLException {
        String sql = "UPDATE ThamGia SET SoTrai = ? WHERE MaTD = ? AND MaCT = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tg.getSoTrai());
            stmt.setString(2, tg.getMaTD());
            stmt.setString(3, tg.getMaCT());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // DELETE by MaTD 
    public boolean deleteByMaTD(String maTD, Connection conn) throws SQLException {
        String sql = "DELETE FROM ThamGia WHERE MaTD = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maTD);
            return stmt.executeUpdate() >= 0; // 0 is OK if no records exist
        }
    }
    
    // DELETE 
    public boolean delete(String maTD, String maCT, Connection conn) throws SQLException {
        String sql = "DELETE FROM ThamGia WHERE MaTD = ? AND MaCT = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maTD);
            stmt.setString(2, maCT);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // COUNT matches by player (for validation before delete)
    public int countByMaCT(String maCT, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS Total FROM ThamGia WHERE MaCT = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maCT);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Total");
            }
        }
        return 0;
    }
    
    // COUNT participations by match (for validation before delete match)
    public int countByMaTD(String maTD, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS Total FROM ThamGia WHERE MaTD = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maTD);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("Total");
            }
        }
        return 0;
    }
    
    // Get total goals from all matches (for dashboard)
    public int getTotalGoals(Connection conn) throws SQLException {
        String sql = "SELECT SUM(SoTrai) AS TotalGoals FROM ThamGia";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("TotalGoals");
            }
        }
        return 0;
    }
}
