package org.football.services;
import org.football.dao.DoiBongDAO;
import org.football.models.DoiBong;

import java.sql.SQLException;
import java.util.List;

public class DoiBongService {
    private DoiBongDAO dao = new DoiBongDAO();
    
    // Lấy tất cả đội bóng (merge từ 2 DB) - Dùng cho ComboBox
    public List<DoiBong> findAll() throws SQLException {
        return dao.findAllMerged();
    }
    
    // Thêm đội bóng mới
    public void insert(DoiBong doibong) throws SQLException {
        // Check trùng mã
        if (dao.findById(doibong.getMaDB()) != null) {
            throw new SQLException(" Mã đội bóng đã tồn tại!");
        }
        
        dao.insert(doibong);
    }
    
    // Tìm theo mã
    public DoiBong findById(String maDB) throws SQLException {
        return dao.findById(maDB);
    }
}
