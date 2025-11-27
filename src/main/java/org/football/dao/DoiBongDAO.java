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
            // ⚠️ Đổi CLB → Chuyển DoiBong + CauThu sang DB mới
            
            Connection oldConn = oldCLB.equals("CLB1") 
                ? DatabaseConnection.getConnection1() 
                : DatabaseConnection.getConnection2();
            
            Connection newConn = newCLB.equals("CLB1")
                ? DatabaseConnection.getConnection1()
                : DatabaseConnection.getConnection2();
            
            // BƯỚC 0: Kiểm tra có ThamGia tham chiếu đến cầu thủ của đội này ở DB cũ không
            // Nếu có → KHÔNG CHO CHUYỂN (vì sẽ tạo duplicate hoặc mất FK)
            String checkThamGia = "SELECT COUNT(*) FROM ThamGia WHERE MaCT IN (SELECT MaCT FROM CauThu WHERE MaDB = ?)";
            try (PreparedStatement psCheck = oldConn.prepareStatement(checkThamGia)) {
                psCheck.setString(1, doiBong.getMaDB());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Không thể chuyển đội bóng! Có " + rs.getInt(1) + 
                        " bản ghi ThamGia tham chiếu đến cầu thủ của đội này. " +
                        "Vui lòng xóa dữ liệu ThamGia trước khi chuyển.");
                }
            }
            
            // BƯỚC 1: Kiểm tra và xử lý DoiBong ở DB mới
            boolean doiBongExistsInNew = checkDoiBongExists(doiBong.getMaDB(), newConn);
            
            if (doiBongExistsInNew) {
                // Đã tồn tại → UPDATE
                String updateDoiBong = "UPDATE DoiBong SET TenDB = ?, CLB = ? WHERE MaDB = ?";
                try (PreparedStatement psUpdate = newConn.prepareStatement(updateDoiBong)) {
                    psUpdate.setString(1, doiBong.getTenDB());
                    psUpdate.setString(2, doiBong.getClb());
                    psUpdate.setString(3, doiBong.getMaDB());
                    psUpdate.executeUpdate();
                }
            } else {
                // Chưa tồn tại → INSERT
                String insertDoiBong = "INSERT INTO DoiBong (MaDB, TenDB, CLB) VALUES (?, ?, ?)";
                try (PreparedStatement psInsertDB = newConn.prepareStatement(insertDoiBong)) {
                    psInsertDB.setString(1, doiBong.getMaDB());
                    psInsertDB.setString(2, doiBong.getTenDB());
                    psInsertDB.setString(3, doiBong.getClb());
                    psInsertDB.executeUpdate();
                }
            }
            
            // BƯỚC 2: Copy cầu thủ sang DB mới
            String selectPlayers = "SELECT * FROM CauThu WHERE MaDB = ?";
            
            try (PreparedStatement psSelect = oldConn.prepareStatement(selectPlayers)) {
                psSelect.setString(1, doiBong.getMaDB());
                ResultSet rs = psSelect.executeQuery();
                
                while (rs.next()) {
                    String maCT = rs.getString("MaCT");
                    
                    if (!checkCauThuExists(maCT, newConn)) {
                        String insertPlayer = "INSERT INTO CauThu (MaCT, HoTen, ViTri, MaDB) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement psInsert = newConn.prepareStatement(insertPlayer)) {
                            psInsert.setString(1, maCT);
                            psInsert.setString(2, rs.getString("HoTen"));
                            psInsert.setString(3, rs.getString("ViTri"));
                            psInsert.setString(4, rs.getString("MaDB"));
                            psInsert.executeUpdate();
                        }
                    } else {
                        String updatePlayer = "UPDATE CauThu SET HoTen = ?, ViTri = ?, MaDB = ? WHERE MaCT = ?";
                        try (PreparedStatement psUpdate = newConn.prepareStatement(updatePlayer)) {
                            psUpdate.setString(1, rs.getString("HoTen"));
                            psUpdate.setString(2, rs.getString("ViTri"));
                            psUpdate.setString(3, rs.getString("MaDB"));
                            psUpdate.setString(4, maCT);
                            psUpdate.executeUpdate();
                        }
                    }
                }
            }
            
            // BƯỚC 3: Xóa cầu thủ ở DB cũ (đã kiểm tra không có ThamGia)
            String deletePlayers = "DELETE FROM CauThu WHERE MaDB = ?";
            try (PreparedStatement ps = oldConn.prepareStatement(deletePlayers)) {
                ps.setString(1, doiBong.getMaDB());
                ps.executeUpdate();
            }
            
            // BƯỚC 4: Xóa DoiBong ở DB cũ
            delete(doiBong.getMaDB(), oldCLB);
            
            return true;
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

    // Helper: Kiểm tra DoiBong tồn tại trong DB
    private boolean checkDoiBongExists(String maDB, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM DoiBong WHERE MaDB = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maDB);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    
    // Helper: Kiểm tra CauThu tồn tại trong DB
    private boolean checkCauThuExists(String maCT, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM CauThu WHERE MaCT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCT);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
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
