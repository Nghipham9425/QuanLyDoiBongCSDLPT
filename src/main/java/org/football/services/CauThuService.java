package org.football.services;
import org.football.dao.CauThuDAO;
import org.football.dao.DoiBongDAO;
import org.football.models.CauThu;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CauThuService {
    private CauThuDAO dao = new CauThuDAO();
    private DoiBongDAO doiBongDAO = new DoiBongDAO();

    // Thêm cầu thủ mới (CLB truyền từ Controller)
    public void insert(CauThu cauthu, String clb) throws SQLException {
        // Validate input
        validateCauThu(cauthu);
        
        // Check trùng mã
        if (dao.findById(cauthu.getMaCT()) != null) {
            throw new SQLException("❌ Mã cầu thủ đã tồn tại!");
        }
        
        dao.insert(cauthu, clb);
    }
    
    // Cập nhật cầu thủ
    public void update(CauThu cauthu, String oldCLB, String newCLB) throws SQLException {
        // Validate input
        validateCauThu(cauthu);
        
        // Check tồn tại
        if (dao.findById(cauthu.getMaCT()) == null) {
            throw new SQLException("❌ Cầu thủ không tồn tại!");
        }
        
        dao.update(cauthu, oldCLB, newCLB);
    }
    
    // Xóa cầu thủ
    public void delete(String maCT, String clb) throws SQLException {
        // Validate mã không rỗng
        if (maCT == null || maCT.trim().isEmpty()) {
            throw new SQLException("❌ Mã cầu thủ không được rỗng!");
        }
        
        // Check tồn tại
        if (dao.findById(maCT) == null) {
            throw new SQLException("❌ Cầu thủ không tồn tại!");
        }
        
        // TODO: Check khóa ngoại với ThamGia (nếu đã thi đấu thì không cho xóa)
        
        dao.delete(maCT, clb);
    }
    
    // Validate dữ liệu cầu thủ
    private void validateCauThu(CauThu cauthu) throws SQLException {
        if (cauthu == null) {
            throw new SQLException("❌ Dữ liệu cầu thủ không hợp lệ!");
        }
        
        // Check mã cầu thủ
        if (cauthu.getMaCT() == null || cauthu.getMaCT().trim().isEmpty()) {
            throw new SQLException("❌ Mã cầu thủ không được rỗng!");
        }
        if (!cauthu.getMaCT().matches("^CT\\d{2,}$")) {
            throw new SQLException("❌ Mã cầu thủ phải có định dạng CTxx (vd: CT01, CT02)!");
        }
        
        // Check họ tên
        if (cauthu.getHoTen() == null || cauthu.getHoTen().trim().isEmpty()) {
            throw new SQLException("❌ Họ tên không được rỗng!");
        }
        if (cauthu.getHoTen().length() < 3 || cauthu.getHoTen().length() > 50) {
            throw new SQLException("❌ Họ tên phải từ 3-50 ký tự!");
        }
        
        // Check vị trí
        if (cauthu.getViTri() == null || cauthu.getViTri().trim().isEmpty()) {
            throw new SQLException("❌ Vị trí không được rỗng!");
        }
        String[] validPositions = {"Thủ môn", "Hậu vệ", "Tiền vệ", "Tiền đạo"};
        boolean validPosition = false;
        for (String pos : validPositions) {
            if (pos.equals(cauthu.getViTri())) {
                validPosition = true;
                break;
            }
        }
        if (!validPosition) {
            throw new SQLException("❌ Vị trí phải là: Thủ môn, Hậu vệ, Tiền vệ, hoặc Tiền đạo!");
        }
        
        // Check mã đội
        if (cauthu.getMaDB() == null || cauthu.getMaDB().trim().isEmpty()) {
            throw new SQLException("❌ Mã đội bóng không được rỗng!");
        }
        
        // Check đội bóng có tồn tại không (FK constraint)
        if (doiBongDAO.findById(cauthu.getMaDB()) == null) {
            throw new SQLException("❌ Đội bóng '" + cauthu.getMaDB() + "' không tồn tại!");
        }
    }
    
    // Lấy tất cả (merge 2 DB)
    public List<CauThu> findAll() throws SQLException {
        return dao.findAllMerged();
    }
    
    // Lấy theo DB (cho filter)
    public List<CauThu> findByDB(int dbIndex) throws SQLException {
        Connection conn = dbIndex == 1 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        return dao.findAll(conn);
    }
    
    // Tìm kiếm theo tên
    public List<CauThu> searchByName(String hoTen) throws SQLException {
        return dao.searchByName(hoTen);
    }
    
    // Tìm theo mã
    public CauThu findById(String maCT) throws SQLException {
        return dao.findById(maCT);
    }
    
    // Tìm cầu thủ theo mã đội (for DiemSoDialog)
    public List<CauThu> findByMaDB(String maDB) throws SQLException {
        return dao.findByMaDB(maDB);
    }
}
