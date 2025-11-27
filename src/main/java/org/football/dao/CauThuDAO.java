package org.football.dao;
import org.football.models.CauThu;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CauThuDAO {

    // Thêm cầu thủ mới (CLB truyền từ UI)
    public boolean insert(CauThu cauthu, String clb) throws SQLException {
        // Chọn DB dựa vào CLB
        Connection conn = clb.equals("CLB1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();

        String sql = "INSERT INTO CauThu (MaCT, HoTen, ViTri, MaDB) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cauthu.getMaCT());
            ps.setString(2, cauthu.getHoTen());
            ps.setString(3, cauthu.getViTri());
            ps.setString(4, cauthu.getMaDB());
            return ps.executeUpdate() > 0;
        }
    }

    // Tìm cầu thủ theo mã (tìm cả 2 DB)
    public CauThu findById(String maCT) throws SQLException {
        String sql = "SELECT * FROM CauThu WHERE MaCT = ?";
        
        // Thử DB1 trước
        CauThu result = queryOne(DatabaseConnection.getConnection1(), sql, maCT);
        if (result != null) return result;
        
        // Không có thì thử DB2
        return queryOne(DatabaseConnection.getConnection2(), sql, maCT);
    }
    
    // Lấy tất cả cầu thủ từ 1 DB
    public List<CauThu> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM CauThu";
        List<CauThu> list = new ArrayList<>();
        
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }
    
    // Lấy tất cả từ cả 2 DB
    public List<CauThu> findAllMerged() throws SQLException {
        List<CauThu> result = new ArrayList<>();
        result.addAll(findAll(DatabaseConnection.getConnection1()));
        result.addAll(findAll(DatabaseConnection.getConnection2()));
        return result;
    }
    
    // Tìm theo tên (cả 2 DB)
    public List<CauThu> searchByName(String hoTen) throws SQLException {
        String sql = "SELECT * FROM CauThu WHERE HoTen LIKE ?";
        List<CauThu> result = new ArrayList<>();
        
        result.addAll(queryList(DatabaseConnection.getConnection1(), sql, "%" + hoTen + "%"));
        result.addAll(queryList(DatabaseConnection.getConnection2(), sql, "%" + hoTen + "%"));
        
        return result;
    }
    
    // Lấy cầu thủ theo đội
    public List<CauThu> findByMaDB(String maDB) throws SQLException {
        String sql = "SELECT * FROM CauThu WHERE MaDB = ?";
        List<CauThu> result = new ArrayList<>();
        
        // Tìm cả 2 DB vì không biết đội thuộc DB nào
        result.addAll(queryList(DatabaseConnection.getConnection1(), sql, maDB));
        result.addAll(queryList(DatabaseConnection.getConnection2(), sql, maDB));
        
        return result;
    }
    
    // Tìm cầu thủ theo CLB (cho query)
    public List<CauThu> findByCLB(String clb, Connection conn) throws SQLException {
        String sql = "SELECT CT.* " +
                    "FROM CauThu CT " +
                    "JOIN DoiBong DB ON CT.MaDB = DB.MaDB " +
                    "WHERE DB.CLB = ?";
        
        return queryList(conn, sql, clb);
    }
    
    // Cập nhật cầu thủ (có thể di chuyển giữa 2 mảnh)
    public boolean update(CauThu cauthu, String oldCLB, String newCLB) throws SQLException {
        if (!oldCLB.equals(newCLB)) {
            // Đổi CLB → copy cầu thủ sang DB mới
            Connection oldConn = oldCLB.equals("CLB1") 
                ? DatabaseConnection.getConnection1() 
                : DatabaseConnection.getConnection2();
            
            Connection newConn = newCLB.equals("CLB1")
                ? DatabaseConnection.getConnection1()
                : DatabaseConnection.getConnection2();
            
            // BƯỚC 0: Kiểm tra có ThamGia tham chiếu đến cầu thủ này ở DB cũ không
            // Nếu có → KHÔNG CHO CHUYỂN (vì sẽ tạo duplicate hoặc mất FK)
            String checkThamGia = "SELECT COUNT(*) FROM ThamGia WHERE MaCT = ?";
            try (PreparedStatement psCheck = oldConn.prepareStatement(checkThamGia)) {
                psCheck.setString(1, cauthu.getMaCT());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Không thể chuyển cầu thủ! Có " + rs.getInt(1) + 
                        " bản ghi ThamGia tham chiếu đến cầu thủ này. " +
                        "Vui lòng xóa dữ liệu ThamGia trước khi chuyển.");
                }
            }
            
            // BƯỚC 1: Kiểm tra và xử lý ở DB mới
            boolean cauThuExistsInNew = checkCauThuExists(cauthu.getMaCT(), newConn);
            
            if (cauThuExistsInNew) {
                // Đã tồn tại → UPDATE
                String updatePlayer = "UPDATE CauThu SET HoTen = ?, ViTri = ?, MaDB = ? WHERE MaCT = ?";
                try (PreparedStatement psUpdate = newConn.prepareStatement(updatePlayer)) {
                    psUpdate.setString(1, cauthu.getHoTen());
                    psUpdate.setString(2, cauthu.getViTri());
                    psUpdate.setString(3, cauthu.getMaDB());
                    psUpdate.setString(4, cauthu.getMaCT());
                    psUpdate.executeUpdate();
                }
            } else {
                // Chưa tồn tại → INSERT
                String insertPlayer = "INSERT INTO CauThu (MaCT, HoTen, ViTri, MaDB) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psInsert = newConn.prepareStatement(insertPlayer)) {
                    psInsert.setString(1, cauthu.getMaCT());
                    psInsert.setString(2, cauthu.getHoTen());
                    psInsert.setString(3, cauthu.getViTri());
                    psInsert.setString(4, cauthu.getMaDB());
                    psInsert.executeUpdate();
                }
            }
            
            // BƯỚC 2: Xóa cầu thủ ở DB cũ (đã kiểm tra không có ThamGia)
            delete(cauthu.getMaCT(), oldCLB);
            
            return true;
        } else {
            // Cùng CLB → update bình thường
            Connection conn = oldCLB.equals("CLB1") 
                ? DatabaseConnection.getConnection1() 
                : DatabaseConnection.getConnection2();
            
            String sql = "UPDATE CauThu SET HoTen = ?, ViTri = ?, MaDB = ? WHERE MaCT = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, cauthu.getHoTen());
                ps.setString(2, cauthu.getViTri());
                ps.setString(3, cauthu.getMaDB());
                ps.setString(4, cauthu.getMaCT());
                return ps.executeUpdate() > 0;
            }
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
    
    // Xóa cầu thủ
    public boolean delete(String maCT, String clb) throws SQLException {
        Connection conn = clb.equals("CLB1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        
        String sql = "DELETE FROM CauThu WHERE MaCT = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maCT);
            return ps.executeUpdate() > 0;
        }
    }
    
    // Helper: query 1 record
    private CauThu queryOne(Connection conn, String sql, String param) throws SQLException {
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
    
    // Helper: query nhiều records
    private List<CauThu> queryList(Connection conn, String sql, String param) throws SQLException {
        List<CauThu> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }
    
    // Helper: map ResultSet sang CauThu
    private CauThu mapResultSet(ResultSet rs) throws SQLException {
        return new CauThu(
            rs.getString("MaCT"),
            rs.getString("HoTen"),
            rs.getString("ViTri"),
            rs.getString("MaDB")
        );
    }
}


