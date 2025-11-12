package org.football.dao;

import org.football.models.DoiBong;
import org.football.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoiBongDAO {

    public boolean insert(DoiBong doiBong, String clb) throws SQLException {
        Connection conn = clb.equals("CLB1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();

        String sql = "INSERT INTO DoiBong (MaDB, TenDB, CLB) VALUES (?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doiBong.getMaDB());
            ps.setString(2, doiBong.getTenDB());
            ps.setString(3, doiBong.getClb());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(DoiBong doiBong, String oldCLB, String newCLB) throws SQLException {
        if (!oldCLB.equals(newCLB)) {
            // Đổi CLB → Delete + Insert
            delete(doiBong.getMaDB(), oldCLB);
            return insert(doiBong, newCLB);
        }
        
        // Cùng CLB → Update bình thường
        Connection conn = oldCLB.equals("CLB1")
            ? DatabaseConnection.getConnection1()
            : DatabaseConnection.getConnection2();
        
        String sql = "UPDATE DoiBong SET TenDB = ?, CLB = ? WHERE MaDB = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doiBong.getTenDB());
            ps.setString(2, doiBong.getClb());
            ps.setString(3, doiBong.getMaDB());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(String maDB, String clb) throws SQLException {
        Connection conn = clb.equals("CLB1")
            ? DatabaseConnection.getConnection1()
            : DatabaseConnection.getConnection2();
        
        String sql = "DELETE FROM DoiBong WHERE MaDB = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDB);
            return ps.executeUpdate() > 0;
        }
    }

    public DoiBong findById(String maDB) throws SQLException {
        // Tìm DB1
        DoiBong result = findByIdInDB(maDB, DatabaseConnection.getConnection1());
        if (result != null) return result;
        
        // Tìm DB2
        return findByIdInDB(maDB, DatabaseConnection.getConnection2());
    }
    
    private DoiBong findByIdInDB(String maDB, Connection conn) throws SQLException {
        String sql = "SELECT * FROM DoiBong WHERE MaDB = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDB);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new DoiBong(
                        rs.getString("MaDB"),
                        rs.getString("TenDB"),
                        rs.getString("CLB")
                    );
                }
            }
        }
        return null;
    }

    public List<DoiBong> findAll(Connection conn) throws SQLException {
        List<DoiBong> list = new ArrayList<>();
        String sql = "SELECT * FROM DoiBong ORDER BY MaDB";
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(new DoiBong(
                    rs.getString("MaDB"),
                    rs.getString("TenDB"),
                    rs.getString("CLB")
                ));
            }
        }
        return list;
    }
    
    public List<DoiBong> findAllMerged() throws SQLException {
        List<DoiBong> merged = new ArrayList<>();
        merged.addAll(findAll(DatabaseConnection.getConnection1()));
        merged.addAll(findAll(DatabaseConnection.getConnection2()));
        return merged;
    }

    public List<DoiBong> searchByName(String tenDB) throws SQLException {
        List<DoiBong> result = new ArrayList<>();
        result.addAll(searchInDB(tenDB, DatabaseConnection.getConnection1()));
        result.addAll(searchInDB(tenDB, DatabaseConnection.getConnection2()));
        return result;
    }
    
    private List<DoiBong> searchInDB(String tenDB, Connection conn) throws SQLException {
        List<DoiBong> list = new ArrayList<>();
        String sql = "SELECT * FROM DoiBong WHERE TenDB LIKE ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + tenDB + "%");
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new DoiBong(
                        rs.getString("MaDB"),
                        rs.getString("TenDB"),
                        rs.getString("CLB")
                    ));
                }
            }
        }
        return list;
    }
}
