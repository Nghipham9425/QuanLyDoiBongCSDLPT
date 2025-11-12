package org.football.services;

import org.football.dao.TranDauDAO;
import org.football.dao.DoiBongDAO;
import org.football.models.TranDau;
import org.football.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TranDauService {
    private TranDauDAO dao = new TranDauDAO();
    private ThamGiaService thamGiaService = new ThamGiaService();
    private DoiBongDAO doiBongDAO = new DoiBongDAO();

    // INSERT
    public void insert(TranDau tranDau, String sanDau) throws Exception {
        // Validation: Dữ liệu không null
        if (tranDau == null) {
            throw new Exception("Dữ liệu trận đấu không hợp lệ!");
        }
        
        // Validation: Mã trận đấu không rỗng và đúng format
        if (tranDau.getMaTD() == null || tranDau.getMaTD().trim().isEmpty()) {
            throw new Exception("Mã trận đấu không được rỗng!");
        }
        if (!tranDau.getMaTD().matches("^TD\\d{2,}$")) {
            throw new Exception("Mã trận đấu phải có định dạng TDxx (vd: TD01, TD02)!");
        }
        
        // Validation: Check trùng MaTD trong cả 2 DB
        if (dao.findById(tranDau.getMaTD()) != null) {
            throw new Exception("Mã trận đấu '" + tranDau.getMaTD() + "' đã tồn tại!");
        }
        
        // Validation: Trọng tài không rỗng
        if (tranDau.getTrongTai() == null || tranDau.getTrongTai().trim().isEmpty()) {
            throw new Exception("Trọng tài không được rỗng!");
        }
        if (tranDau.getTrongTai().length() < 3 || tranDau.getTrongTai().length() > 50) {
            throw new Exception("Tên trọng tài phải từ 3-50 ký tự!");
        }
        
        // Validation: Sân đấu hợp lệ
        if (!sanDau.equals("SD1") && !sanDau.equals("SD2")) {
            throw new Exception("Sân đấu phải là 'SD1' hoặc 'SD2'!");
        }
        
        // Validation: Mã đội không rỗng
        if (tranDau.getMaDB1() == null || tranDau.getMaDB1().trim().isEmpty()) {
            throw new Exception("Mã đội 1 không được rỗng!");
        }
        if (tranDau.getMaDB2() == null || tranDau.getMaDB2().trim().isEmpty()) {
            throw new Exception("Mã đội 2 không được rỗng!");
        }
        
        // Validation: 2 đội không trùng nhau
        if (tranDau.getMaDB1().equals(tranDau.getMaDB2())) {
            throw new Exception("2 đội không được trùng nhau!");
        }
        
        // Validation: Check đội bóng có tồn tại không (FK constraint)
        if (doiBongDAO.findById(tranDau.getMaDB1()) == null) {
            throw new Exception("Đội '" + tranDau.getMaDB1() + "' không tồn tại!");
        }
        if (doiBongDAO.findById(tranDau.getMaDB2()) == null) {
            throw new Exception("Đội '" + tranDau.getMaDB2() + "' không tồn tại!");
        }
        
        // Check if cross-CLB match (need to insert to both DBs)
        org.football.models.DoiBong doi1 = doiBongDAO.findById(tranDau.getMaDB1());
        org.football.models.DoiBong doi2 = doiBongDAO.findById(tranDau.getMaDB2());
        
        boolean isCrossCLB = !doi1.getClb().equals(doi2.getClb());
        
        if (isCrossCLB) {
            // Cross-CLB match: Insert to BOTH databases
            Connection conn1 = DatabaseConnection.getConnection1();
            Connection conn2 = DatabaseConnection.getConnection2();
            
            if (!dao.insertToConnection(tranDau, conn1)) {
                throw new Exception("Thêm trận đấu vào DB1 thất bại!");
            }
            if (!dao.insertToConnection(tranDau, conn2)) {
                throw new Exception("Thêm trận đấu vào DB2 thất bại!");
            }
        } else {
            // Same CLB: Insert to correct DB based on stadium
            if (!dao.insert(tranDau, sanDau)) {
                throw new Exception("Thêm trận đấu thất bại!");
            }
        }
    }

    // UPDATE
    public void update(TranDau tranDau, String oldSanDau, String newSanDau) throws Exception {
        // Validation: Dữ liệu không null
        if (tranDau == null) {
            throw new Exception("Dữ liệu trận đấu không hợp lệ!");
        }
        
        // Validation: Trận phải tồn tại
        if (dao.findById(tranDau.getMaTD()) == null) {
            throw new Exception("Trận đấu không tồn tại!");
        }
        
        // Validation: Trọng tài không rỗng
        if (tranDau.getTrongTai() == null || tranDau.getTrongTai().trim().isEmpty()) {
            throw new Exception("Trọng tài không được rỗng!");
        }
        if (tranDau.getTrongTai().length() < 3 || tranDau.getTrongTai().length() > 50) {
            throw new Exception("Tên trọng tài phải từ 3-50 ký tự!");
        }
        
        // Validation: 2 đội không trùng nhau
        if (tranDau.getMaDB1().equals(tranDau.getMaDB2())) {
            throw new Exception("2 đội không được trùng nhau!");
        }
        
        // Validation: Check đội bóng có tồn tại không
        if (doiBongDAO.findById(tranDau.getMaDB1()) == null) {
            throw new Exception("Đội '" + tranDau.getMaDB1() + "' không tồn tại!");
        }
        if (doiBongDAO.findById(tranDau.getMaDB2()) == null) {
            throw new Exception("Đội '" + tranDau.getMaDB2() + "' không tồn tại!");
        }
        
        if (!dao.update(tranDau, oldSanDau, newSanDau)) {
            throw new Exception("Cập nhật trận đấu thất bại!");
        }
    }

    // DELETE
    public void delete(String maTD, String sanDau) throws Exception {
        // Validation: Trận phải tồn tại
        if (dao.findById(maTD) == null) {
            throw new Exception("Trận đấu không tồn tại!");
        }
        
        // Validation: Không cho xóa nếu có cầu thủ tham gia
        int participationCount = thamGiaService.countByMaTD(maTD);
        if (participationCount > 0) {
            throw new Exception("Không thể xóa trận đấu!\nTrận này có " + participationCount + 
                " lượt tham gia. Vui lòng xóa dữ liệu tham gia trước.");
        }
        
        if (!dao.delete(maTD, sanDau)) {
            throw new Exception("Xóa trận đấu thất bại!");
        }
    }

    // QUERY
    public TranDau findById(String maTD) throws SQLException {
        return dao.findById(maTD);
    }
    
    public List<TranDau> findAll() throws SQLException {
        return dao.findAllMerged();
    }
    
    public List<TranDau> findByDB(int dbIndex) throws SQLException {
        Connection conn = (dbIndex == 1) 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        return dao.findAll(conn);
    }
    
    public List<TranDau> searchByTrongTai(String trongTai) throws SQLException {
        return dao.searchByTrongTai(trongTai);
    }
}
