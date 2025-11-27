package org.football.services;

import org.football.dao.DoiBongDAO;
import org.football.models.DoiBong;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DoiBongService {
    private DoiBongDAO dao = new DoiBongDAO();

    public void insert(DoiBong doiBong, String clb) throws Exception {
        // Validation: Dữ liệu không null
        if (doiBong == null) {
            throw new Exception("Dữ liệu đội bóng không hợp lệ!");
        }
        
        // Validation: Mã đội không rỗng và đúng format
        if (doiBong.getMaDB() == null || doiBong.getMaDB().trim().isEmpty()) {
            throw new Exception("Mã đội không được rỗng!");
        }
        if (!doiBong.getMaDB().matches("^DB\\d{2,}$")) {
            throw new Exception("Mã đội phải có định dạng DBxx (vd: DB01, DB02)!");
        }
        
        // Validation: Tên đội không rỗng và độ dài hợp lý
        if (doiBong.getTenDB() == null || doiBong.getTenDB().trim().isEmpty()) {
            throw new Exception("Tên đội không được rỗng!");
        }
        if (doiBong.getTenDB().length() < 3 || doiBong.getTenDB().length() > 100) {
            throw new Exception("Tên đội phải từ 3-100 ký tự!");
        }
        
        // Validation: CLB hợp lệ
        if (!clb.equals("CLB1") && !clb.equals("CLB2")) {
            throw new Exception("CLB phải là 'CLB1' hoặc 'CLB2'!");
        }
        
        // Validation: Check trùng MaDB trong cả 2 DB
        if (dao.findById(doiBong.getMaDB()) != null) {
            throw new Exception("Mã đội '" + doiBong.getMaDB() + "' đã tồn tại!");
        }
        
        if (!dao.insert(doiBong, clb)) {
            throw new Exception("Thêm đội bóng thất bại!");
        }
    }

    public void update(DoiBong doiBong, String oldCLB, String newCLB) throws Exception {
        // Validation: Dữ liệu không null
        if (doiBong == null) {
            throw new Exception("Dữ liệu đội bóng không hợp lệ!");
        }
        
        // Validation: Tên đội không rỗng
        if (doiBong.getTenDB() == null || doiBong.getTenDB().trim().isEmpty()) {
            throw new Exception("Tên đội không được rỗng!");
        }
        if (doiBong.getTenDB().length() < 3 || doiBong.getTenDB().length() > 100) {
            throw new Exception("Tên đội phải từ 3-100 ký tự!");
        }
        
        // Validation: Đội phải tồn tại
        if (dao.findById(doiBong.getMaDB()) == null) {
            throw new Exception("Đội bóng không tồn tại!");
        }
        
        if (!dao.update(doiBong, oldCLB, newCLB)) {
            throw new Exception("Cập nhật đội bóng thất bại!");
        }
    }

    public void delete(String maDB, String clb) throws Exception {
        // Validation: Đội phải tồn tại
        if (dao.findById(maDB) == null) {
            throw new Exception("Đội bóng không tồn tại!");
        }
        
        if (!dao.delete(maDB, clb)) {
            throw new Exception("Xóa đội bóng thất bại!");
        }
    }

    public DoiBong findById(String maDB) throws SQLException {
        return dao.findById(maDB);
    }
    
    public List<DoiBong> findAll() throws SQLException {
        return dao.findAllMerged();
    }
    
    public List<DoiBong> findByDB(int dbIndex) throws SQLException {
        Connection conn = (dbIndex == 1) 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        return dao.findAll(conn);
    }
    
    public List<DoiBong> searchByName(String tenDB) throws SQLException {
        return dao.searchByName(tenDB);
    }
    
    // Kiểm tra đội bóng có tồn tại không (dùng cho validation)
    public boolean checkExists(String maDB) throws SQLException {
        return dao.findById(maDB) != null;
    }
}
