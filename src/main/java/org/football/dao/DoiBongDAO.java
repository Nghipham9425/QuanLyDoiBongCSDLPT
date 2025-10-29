package org.football.dao;
import org.football.models.DoiBong;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoiBongDAO {
    
    // Thêm đội bóng mới
    public boolean insert(DoiBong doibong) throws SQLException {
        Connection conn = doibong.getClb().equals("CLB1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();

        String sql = "INSERT INTO DoiBong (MaDB, TenDB, CLB) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doibong.getMaDB());
            ps.setString(2, doibong.getTenDB());
            ps.setString(3, doibong.getClb());
            return ps.executeUpdate() > 0;
        }
    }

    // Tìm đội bóng theo mã (tìm cả 2 DB)
    public DoiBong findById(String maDB) throws SQLException {
        String sql = "SELECT * FROM DoiBong WHERE MaDB = ?";
        
        // Thử DB1 trước
        DoiBong result = queryOne(DatabaseConnection.getConnection1(), sql, maDB);
        if (result != null) return result;
        
        // Không có thì thử DB2
        return queryOne(DatabaseConnection.getConnection2(), sql, maDB);
    }
    
    // Lấy tất cả đội bóng từ 1 DB
    public List<DoiBong> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM DoiBong";
        List<DoiBong> list = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }
    
    // Lấy tất cả từ cả 2 DB
    public List<DoiBong> findAllMerged() throws SQLException {
        List<DoiBong> result = new ArrayList<>();
        result.addAll(findAll(DatabaseConnection.getConnection1()));
        result.addAll(findAll(DatabaseConnection.getConnection2()));
        return result;
    }
    
    // Cập nhật đội bóng
    public boolean update(DoiBong doibong, String oldCLB, String newCLB) throws SQLException {
        if (!oldCLB.equals(newCLB)) {
            // Đổi CLB → xóa DB cũ, insert DB mới
            delete(doibong.getMaDB(), oldCLB);
            return insert(doibong);
        } else {
            // Cùng CLB → update bình thường
            Connection conn = oldCLB.equals("CLB1") 
                ? DatabaseConnection.getConnection1() 
                : DatabaseConnection.getConnection2();
            
            String sql = "UPDATE DoiBong SET TenDB = ?, CLB = ? WHERE MaDB = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, doibong.getTenDB());
                ps.setString(2, doibong.getClb());
                ps.setString(3, doibong.getMaDB());
                return ps.executeUpdate() > 0;
            }
        }
    }
    
    // Xóa đội bóng
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
    
    // Helper: query 1 record
    private DoiBong queryOne(Connection conn, String sql, String param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }
    
    // Helper: map ResultSet sang DoiBong
    private DoiBong mapResultSet(ResultSet rs) throws SQLException {
        return new DoiBong(
            rs.getString("MaDB"),
            rs.getString("TenDB"),
            rs.getString("CLB")
        );
    }
}
