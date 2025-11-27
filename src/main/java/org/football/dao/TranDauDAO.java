package org.football.dao;

import org.football.models.TranDau;
import org.football.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TranDauDAO {

    // INSERT
    public boolean insert(TranDau tranDau, String sanDau) throws SQLException {
        Connection targetConn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        
        Connection otherConn = sanDau.equals("SD1")
            ? DatabaseConnection.getConnection2()
            : DatabaseConnection.getConnection1();

        // ⚠️ Trước khi insert TranDau, đảm bảo DoiBong (MaDB1, MaDB2) tồn tại trong DB đích
        // Vì TranDau phân mảnh theo SanDau, nhưng DoiBong phân mảnh theo CLB
        ensureDoiBongExists(tranDau.getMaDB1(), targetConn, otherConn);
        ensureDoiBongExists(tranDau.getMaDB2(), targetConn, otherConn);
        
        return insertToConnection(tranDau, targetConn);
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
    
    /**
     * Đảm bảo DoiBong tồn tại trong DB đích (sao chép nếu cần)
     * Đây là giải pháp cho cross-partition FK: TranDau tham chiếu DoiBong từ DB khác
     */
    private void ensureDoiBongExists(String maDB, Connection targetConn, Connection sourceConn) throws SQLException {
        // Kiểm tra DoiBong đã tồn tại trong target chưa
        String checkSql = "SELECT COUNT(*) FROM DoiBong WHERE MaDB = ?";
        try (PreparedStatement psCheck = targetConn.prepareStatement(checkSql)) {
            psCheck.setString(1, maDB);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Đã tồn tại, không cần copy
            }
        }
        
        // Chưa tồn tại → Copy từ source DB
        String selectSql = "SELECT * FROM DoiBong WHERE MaDB = ?";
        try (PreparedStatement psSelect = sourceConn.prepareStatement(selectSql)) {
            psSelect.setString(1, maDB);
            ResultSet rs = psSelect.executeQuery();
            
            if (rs.next()) {
                String insertSql = "INSERT INTO DoiBong (MaDB, TenDB, CLB) VALUES (?, ?, ?)";
                try (PreparedStatement psInsert = targetConn.prepareStatement(insertSql)) {
                    psInsert.setString(1, rs.getString("MaDB"));
                    psInsert.setString(2, rs.getString("TenDB"));
                    psInsert.setString(3, rs.getString("CLB"));
                    psInsert.executeUpdate();
                }
            }
        }
    }

    // UPDATE
    public boolean update(TranDau tranDau, String oldSanDau, String newSanDau) throws SQLException {
        if (!oldSanDau.equals(newSanDau)) {
            // ⚠️ Đổi sân → Chuyển TranDau + ThamGia sang DB mới
            
            Connection oldConn = oldSanDau.equals("SD1") 
                ? DatabaseConnection.getConnection1() 
                : DatabaseConnection.getConnection2();
            
            Connection newConn = newSanDau.equals("SD1")
                ? DatabaseConnection.getConnection1()
                : DatabaseConnection.getConnection2();
            
            // BƯỚC 0: Đảm bảo DoiBong tồn tại trong DB mới
            ensureDoiBongExists(tranDau.getMaDB1(), newConn, oldConn);
            ensureDoiBongExists(tranDau.getMaDB2(), newConn, oldConn);
            
            // BƯỚC 1: Insert TranDau vào DB mới
            String insertTranDau = "INSERT INTO TranDau (MaTD, MaDB1, MaDB2, TrongTai, SanDau) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement psInsertTD = newConn.prepareStatement(insertTranDau)) {
                psInsertTD.setString(1, tranDau.getMaTD());
                psInsertTD.setString(2, tranDau.getMaDB1());
                psInsertTD.setString(3, tranDau.getMaDB2());
                psInsertTD.setString(4, tranDau.getTrongTai());
                psInsertTD.setString(5, tranDau.getSanDau());
                psInsertTD.executeUpdate();
            }
            
            // BƯỚC 2: Copy ThamGia sang DB mới
            // Cần đảm bảo CauThu tồn tại trong DB mới trước
            String selectThamGia = "SELECT TG.*, CT.HoTen, CT.ViTri, CT.MaDB FROM ThamGia TG " +
                                   "JOIN CauThu CT ON TG.MaCT = CT.MaCT WHERE TG.MaTD = ?";
            
            try (PreparedStatement psSelect = oldConn.prepareStatement(selectThamGia)) {
                psSelect.setString(1, tranDau.getMaTD());
                ResultSet rs = psSelect.executeQuery();
                
                while (rs.next()) {
                    String maCT = rs.getString("MaCT");
                    
                    // Đảm bảo CauThu tồn tại trong DB mới (và DoiBong của cầu thủ đó)
                    ensureCauThuExists(maCT, rs.getString("HoTen"), rs.getString("ViTri"), 
                                       rs.getString("MaDB"), newConn, oldConn);
                    
                    // Insert ThamGia
                    String insertThamGia = "INSERT INTO ThamGia (MaTD, MaCT, SoTrai) VALUES (?, ?, ?)";
                    try (PreparedStatement psInsert = newConn.prepareStatement(insertThamGia)) {
                        psInsert.setString(1, rs.getString("MaTD"));
                        psInsert.setString(2, maCT);
                        psInsert.setInt(3, rs.getInt("SoTrai"));
                        psInsert.executeUpdate();
                    }
                }
            }
            
            // BƯỚC 3: Xóa ThamGia ở DB cũ
            String deleteThamGia = "DELETE FROM ThamGia WHERE MaTD = ?";
            try (PreparedStatement ps = oldConn.prepareStatement(deleteThamGia)) {
                ps.setString(1, tranDau.getMaTD());
                ps.executeUpdate();
            }
            
            // BƯỚC 4: Delete trận ở DB cũ
            delete(tranDau.getMaTD(), oldSanDau);
            
            return true;
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
    
    /**
     * Đảm bảo CauThu tồn tại trong DB đích (sao chép nếu cần)
     * Cũng đảm bảo DoiBong của cầu thủ tồn tại (FK constraint)
     */
    private void ensureCauThuExists(String maCT, String hoTen, String viTri, String maDB, 
                                     Connection targetConn, Connection sourceConn) throws SQLException {
        // Kiểm tra CauThu đã tồn tại chưa
        String checkSql = "SELECT COUNT(*) FROM CauThu WHERE MaCT = ?";
        try (PreparedStatement psCheck = targetConn.prepareStatement(checkSql)) {
            psCheck.setString(1, maCT);
            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Đã tồn tại
            }
        }
        
        // Đảm bảo DoiBong của cầu thủ tồn tại trước (FK constraint)
        ensureDoiBongExists(maDB, targetConn, sourceConn);
        
        // Chưa tồn tại → Insert CauThu
        String insertSql = "INSERT INTO CauThu (MaCT, HoTen, ViTri, MaDB) VALUES (?, ?, ?, ?)";
        try (PreparedStatement psInsert = targetConn.prepareStatement(insertSql)) {
            psInsert.setString(1, maCT);
            psInsert.setString(2, hoTen);
            psInsert.setString(3, viTri);
            psInsert.setString(4, maDB);
            psInsert.executeUpdate();
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
