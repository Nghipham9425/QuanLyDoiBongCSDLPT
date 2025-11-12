package org.football.dao;

import org.football.models.TranDau;
import org.football.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TranDauDAO {

    // INSERT
    public boolean insert(TranDau tranDau, String sanDau) throws SQLException {
        Connection conn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();

        return insertToConnection(tranDau, conn);
    }
    
    // INSERT to specific connection (for cross-CLB matches)
    public boolean insertToConnection(TranDau tranDau, Connection conn) throws SQLException {
        String sql = "INSERT INTO TranDau (MaTD, MaDB1, MaDB2, TrongTai, SanDau) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tranDau.getMaTD());
            ps.setString(2, tranDau.getMaDB1());
            ps.setString(3, tranDau.getMaDB2());
            ps.setString(4, tranDau.getTrongTai());
            ps.setString(5, tranDau.getSanDau());
            return ps.executeUpdate() > 0;
        }
    }

    // UPDATE
    public boolean update(TranDau tranDau, String oldSanDau, String newSanDau) throws SQLException {
        if (!oldSanDau.equals(newSanDau)) {
            // Đổi sân → Delete + Insert
            delete(tranDau.getMaTD(), oldSanDau);
            return insert(tranDau, newSanDau);
        }
        
        // Cùng sân → Update bình thường
        Connection conn = oldSanDau.equals("SD1")
            ? DatabaseConnection.getConnection1()
            : DatabaseConnection.getConnection2();
        
        String sql = "UPDATE TranDau SET MaDB1 = ?, MaDB2 = ?, TrongTai = ?, SanDau = ? WHERE MaTD = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tranDau.getMaDB1());
            ps.setString(2, tranDau.getMaDB2());
            ps.setString(3, tranDau.getTrongTai());
            ps.setString(4, tranDau.getSanDau());
            ps.setString(5, tranDau.getMaTD());
            return ps.executeUpdate() > 0;
        }
    }

    // DELETE
    public boolean delete(String maTD, String sanDau) throws SQLException {
        Connection conn = sanDau.equals("SD1")
            ? DatabaseConnection.getConnection1()
            : DatabaseConnection.getConnection2();
        
        String sql = "DELETE FROM TranDau WHERE MaTD = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTD);
            return ps.executeUpdate() > 0;
        }
    }

    // FIND BY ID
    public TranDau findById(String maTD) throws SQLException {
        // Tìm SD1
        TranDau result = findByIdInDB(maTD, DatabaseConnection.getConnection1());
        if (result != null) return result;
        
        // Tìm SD2
        return findByIdInDB(maTD, DatabaseConnection.getConnection2());
    }
    
    private TranDau findByIdInDB(String maTD, Connection conn) throws SQLException {
        String sql = "SELECT * FROM TranDau WHERE MaTD = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maTD);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TranDau(
                        rs.getString("MaTD"),
                        rs.getString("MaDB1"),
                        rs.getString("MaDB2"),
                        rs.getString("TrongTai"),
                        rs.getString("SanDau")
                    );
                }
            }
        }
        return null;
    }

    // FIND ALL
    public List<TranDau> findAll(Connection conn) throws SQLException {
        List<TranDau> list = new ArrayList<>();
        String sql = "SELECT * FROM TranDau ORDER BY MaTD";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new TranDau(
                    rs.getString("MaTD"),
                    rs.getString("MaDB1"),
                    rs.getString("MaDB2"),
                    rs.getString("TrongTai"),
                    rs.getString("SanDau")
                ));
            }
        }
        return list;
    }
    
    public List<TranDau> findAllMerged() throws SQLException {
        List<TranDau> merged = new ArrayList<>();
        merged.addAll(findAll(DatabaseConnection.getConnection1()));
        merged.addAll(findAll(DatabaseConnection.getConnection2()));
        return merged;
    }

    // SEARCH BY TRONG TAI
    public List<TranDau> searchByTrongTai(String trongTai) throws SQLException {
        List<TranDau> result = new ArrayList<>();
        result.addAll(searchInDB(trongTai, DatabaseConnection.getConnection1()));
        result.addAll(searchInDB(trongTai, DatabaseConnection.getConnection2()));
        return result;
    }
    
    private List<TranDau> searchInDB(String trongTai, Connection conn) throws SQLException {
        List<TranDau> list = new ArrayList<>();
        String sql = "SELECT * FROM TranDau WHERE TrongTai LIKE ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + trongTai + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new TranDau(
                        rs.getString("MaTD"),
                        rs.getString("MaDB1"),
                        rs.getString("MaDB2"),
                        rs.getString("TrongTai"),
                        rs.getString("SanDau")
                    ));
                }
            }
        }
        return list;
    }
}
