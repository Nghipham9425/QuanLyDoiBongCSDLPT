package org.football.services;

import org.football.dao.CauThuDAO;
import org.football.models.CauThu;
import org.football.models.TranDau;
import org.football.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xử lý 10 câu truy vấn phân tán
 */
public class QueryService {
    
    private CauThuDAO cauThuDAO = new CauThuDAO();
    
    /**
     * Query 1: Tìm cầu thủ theo CLB
     */
    public List<CauThu> query1_FindByCLB(String clb) throws SQLException {
        Connection conn = clb.equals("CLB1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        
        return cauThuDAO.findByCLB(clb, conn);
    }
    
    /**
     * Query 2: Đếm số trận của cầu thủ (cần query cả 2 DB)
     */
    public int query2_CountMatches(String hoTen) throws SQLException {
        int count1 = countMatchesInDB(hoTen, DatabaseConnection.getConnection1());
        int count2 = countMatchesInDB(hoTen, DatabaseConnection.getConnection2());
        return count1 + count2;
    }
    
    private int countMatchesInDB(String hoTen, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT TG.MaTD) " +
                    "FROM CauThu CT " +
                    "JOIN ThamGia TG ON CT.MaCT = TG.MaCT " +
                    "WHERE CT.HoTen LIKE ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + hoTen + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Query 3: Số trận hòa theo sân
     */
    public int query3_CountDrawMatches(String sanDau) throws SQLException {
        Connection conn = sanDau.equals("SD1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        
        String sql = 
            "SELECT COUNT(*) FROM ( " +
            "    SELECT TD.MaTD " +
            "    FROM TranDau TD " +
            "    LEFT JOIN ( " +
            "        SELECT TG.MaTD, CT.MaDB, SUM(TG.SoTrai) AS TongBan " +
            "        FROM ThamGia TG " +
            "        JOIN CauThu CT ON TG.MaCT = CT.MaCT " +
            "        GROUP BY TG.MaTD, CT.MaDB " +
            "    ) AS BanThang ON TD.MaTD = BanThang.MaTD " +
            "    WHERE TD.SanDau = ? " +
            "    GROUP BY TD.MaTD, TD.MaDB1, TD.MaDB2 " +
            "    HAVING ISNULL(SUM(CASE WHEN BanThang.MaDB = TD.MaDB1 THEN BanThang.TongBan ELSE 0 END), 0) = " +
            "           ISNULL(SUM(CASE WHEN BanThang.MaDB = TD.MaDB2 THEN BanThang.TongBan ELSE 0 END), 0) " +
            ") AS TranHoa";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sanDau);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Query 4: Vua phá lưới (merge cả 2 DB)
     */
    public List<Map<String, Object>> query4_TopScorers() throws SQLException {
        Map<String, TopScorerData> scorers = new HashMap<>();
        
        // Query DB1
        addTopScorersFromDB(scorers, DatabaseConnection.getConnection1());
        
        // Query DB2
        addTopScorersFromDB(scorers, DatabaseConnection.getConnection2());
        
        // Convert to list và sort
        List<Map<String, Object>> result = new ArrayList<>();
        scorers.forEach((maCT, data) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("maCT", data.maCT);
            item.put("hoTen", data.hoTen);
            item.put("viTri", data.viTri);
            item.put("tongBan", data.tongBan);
            result.add(item);
        });
        
        result.sort((a, b) -> Integer.compare((int)b.get("tongBan"), (int)a.get("tongBan")));
        return result;
    }
    
    private void addTopScorersFromDB(Map<String, TopScorerData> scorers, Connection conn) throws SQLException {
        String sql = 
            "SELECT CT.MaCT, CT.HoTen, CT.ViTri, SUM(TG.SoTrai) AS TongBan " +
            "FROM CauThu CT " +
            "JOIN ThamGia TG ON CT.MaCT = TG.MaCT " +
            "GROUP BY CT.MaCT, CT.HoTen, CT.ViTri";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String maCT = rs.getString("MaCT");
                if (scorers.containsKey(maCT)) {
                    scorers.get(maCT).tongBan += rs.getInt("TongBan");
                } else {
                    scorers.put(maCT, new TopScorerData(
                        maCT,
                        rs.getString("HoTen"),
                        rs.getString("ViTri"),
                        rs.getInt("TongBan")
                    ));
                }
            }
        }
    }
    
    class TopScorerData {
        String maCT, hoTen, viTri;
        int tongBan;
        TopScorerData(String maCT, String hoTen, String viTri, int tongBan) {
            this.maCT = maCT;
            this.hoTen = hoTen;
            this.viTri = viTri;
            this.tongBan = tongBan;
        }
    }
    
    /**
     * Query 5: Trận có cầu thủ X và trọng tài Y
     */
    public List<TranDau> query5_FindMatchesByPlayerAndRef(String hoTen, String trongTai) throws SQLException {
        List<TranDau> result = new ArrayList<>();
        
        // Query DB1
        result.addAll(findMatchesByPlayerAndRef(hoTen, trongTai, DatabaseConnection.getConnection1()));
        
        // Query DB2
        result.addAll(findMatchesByPlayerAndRef(hoTen, trongTai, DatabaseConnection.getConnection2()));
        
        return result;
    }
    
    private List<TranDau> findMatchesByPlayerAndRef(String hoTen, String trongTai, Connection conn) throws SQLException {
        List<TranDau> result = new ArrayList<>();
        
        String sql = 
            "SELECT DISTINCT TD.* " +
            "FROM TranDau TD " +
            "JOIN ThamGia TG ON TD.MaTD = TG.MaTD " +
            "JOIN CauThu CT ON TG.MaCT = CT.MaCT " +
            "WHERE CT.HoTen LIKE ? AND TD.TrongTai LIKE ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + hoTen + "%");
            stmt.setString(2, "%" + trongTai + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(new TranDau(
                    rs.getString("MaTD"),
                    rs.getString("MaDB1"),
                    rs.getString("MaDB2"),
                    rs.getString("TrongTai"),
                    rs.getString("SanDau")
                ));
            }
        }
        
        return result;
    }
    
    /**
     * Query 6: Kiểm tra 2 cầu thủ cùng CLB
     */
    public boolean query6_SameCLB(String hoTen1, String hoTen2) throws SQLException {
        String clb1 = findCLBByName(hoTen1);
        String clb2 = findCLBByName(hoTen2);
        
        return clb1 != null && clb1.equals(clb2);
    }
    
    private String findCLBByName(String hoTen) throws SQLException {
        // Try DB1
        String clb = findCLBByNameInDB(hoTen, DatabaseConnection.getConnection1());
        if (clb != null) return clb;
        
        // Try DB2
        return findCLBByNameInDB(hoTen, DatabaseConnection.getConnection2());
    }
    
    private String findCLBByNameInDB(String hoTen, Connection conn) throws SQLException {
        String sql = 
            "SELECT DB.CLB " +
            "FROM CauThu CT " +
            "JOIN DoiBong DB ON CT.MaDB = DB.MaDB " +
            "WHERE CT.HoTen LIKE ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + hoTen + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("CLB");
            }
        }
        return null;
    }
    
    /**
     * Query 7: Cầu thủ ghi 0 bàn
     */
    public List<Map<String, Object>> query7_PlayersWithZeroGoals() throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Query DB1
        result.addAll(playersWithZeroGoalsInDB(DatabaseConnection.getConnection1()));
        
        // Query DB2
        result.addAll(playersWithZeroGoalsInDB(DatabaseConnection.getConnection2()));
        
        return result;
    }
    
    private List<Map<String, Object>> playersWithZeroGoalsInDB(Connection conn) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String sql = 
            "SELECT CT.MaCT, CT.HoTen, CT.ViTri, COUNT(DISTINCT TG.MaTD) AS SoTran " +
            "FROM CauThu CT " +
            "JOIN ThamGia TG ON CT.MaCT = TG.MaCT " +
            "WHERE TG.SoTrai = 0 " +
            "GROUP BY CT.MaCT, CT.HoTen, CT.ViTri";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("maCT", rs.getString("MaCT"));
                item.put("hoTen", rs.getString("HoTen"));
                item.put("viTri", rs.getString("ViTri"));
                item.put("soTran", rs.getInt("SoTran"));
                result.add(item);
            }
        }
        
        return result;
    }
    
    /**
     * Query 8: Cầu thủ tham gia >= 3 trận
     */
    public List<Map<String, Object>> query8_ActivePlayers() throws SQLException {
        Map<String, ActivePlayerData> players = new HashMap<>();
        
        // Query DB1
        addActivePlayersFromDB(players, DatabaseConnection.getConnection1());
        
        // Query DB2
        addActivePlayersFromDB(players, DatabaseConnection.getConnection2());
        
        // Filter >= 3 trận
        List<Map<String, Object>> result = new ArrayList<>();
        players.forEach((maCT, data) -> {
            if (data.soTran >= 3) {
                Map<String, Object> item = new HashMap<>();
                item.put("maCT", data.maCT);
                item.put("hoTen", data.hoTen);
                item.put("viTri", data.viTri);
                item.put("soTran", data.soTran);
                result.add(item);
            }
        });
        
        result.sort((a, b) -> Integer.compare((int)b.get("soTran"), (int)a.get("soTran")));
        return result;
    }
    
    private void addActivePlayersFromDB(Map<String, ActivePlayerData> players, Connection conn) throws SQLException {
        String sql = 
            "SELECT CT.MaCT, CT.HoTen, CT.ViTri, COUNT(DISTINCT TG.MaTD) AS SoTran " +
            "FROM CauThu CT " +
            "JOIN ThamGia TG ON CT.MaCT = TG.MaCT " +
            "GROUP BY CT.MaCT, CT.HoTen, CT.ViTri";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String maCT = rs.getString("MaCT");
                if (players.containsKey(maCT)) {
                    players.get(maCT).soTran += rs.getInt("SoTran");
                } else {
                    players.put(maCT, new ActivePlayerData(
                        maCT,
                        rs.getString("HoTen"),
                        rs.getString("ViTri"),
                        rs.getInt("SoTran")
                    ));
                }
            }
        }
    }
    
    class ActivePlayerData {
        String maCT, hoTen, viTri;
        int soTran;
        ActivePlayerData(String maCT, String hoTen, String viTri, int soTran) {
            this.maCT = maCT;
            this.hoTen = hoTen;
            this.viTri = viTri;
            this.soTran = soTran;
        }
    }
    
    /**
     * Query 9: Tổng bàn thắng của đội
     */
    public int query9_TeamTotalGoals(String maDB) throws SQLException {
        // Tìm CLB của đội
        String clb = findCLBByTeam(maDB);
        if (clb == null) return 0;
        
        Connection conn = clb.equals("CLB1") 
            ? DatabaseConnection.getConnection1() 
            : DatabaseConnection.getConnection2();
        
        String sql = 
            "SELECT SUM(TG.SoTrai) AS TongBan " +
            "FROM ThamGia TG " +
            "JOIN CauThu CT ON TG.MaCT = CT.MaCT " +
            "WHERE CT.MaDB = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maDB);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("TongBan");
            }
        }
        return 0;
    }
    
    private String findCLBByTeam(String maDB) throws SQLException {
        // Try DB1
        String clb = findCLBByTeamInDB(maDB, DatabaseConnection.getConnection1());
        if (clb != null) return clb;
        
        // Try DB2
        return findCLBByTeamInDB(maDB, DatabaseConnection.getConnection2());
    }
    
    private String findCLBByTeamInDB(String maDB, Connection conn) throws SQLException {
        String sql = "SELECT CLB FROM DoiBong WHERE MaDB = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, maDB);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("CLB");
            }
        }
        return null;
    }
    
    /**
     * Query 10: Cầu thủ chưa thi đấu
     */
    public List<CauThu> query10_PlayersNeverPlayed() throws SQLException {
        List<CauThu> result = new ArrayList<>();
        
        // Query DB1
        result.addAll(playersNeverPlayedInDB(DatabaseConnection.getConnection1()));
        
        // Query DB2
        result.addAll(playersNeverPlayedInDB(DatabaseConnection.getConnection2()));
        
        return result;
    }
    
    private List<CauThu> playersNeverPlayedInDB(Connection conn) throws SQLException {
        List<CauThu> result = new ArrayList<>();
        
        String sql = 
            "SELECT CT.* " +
            "FROM CauThu CT " +
            "LEFT JOIN ThamGia TG ON CT.MaCT = TG.MaCT " +
            "WHERE TG.MaTD IS NULL";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new CauThu(
                    rs.getString("MaCT"),
                    rs.getString("HoTen"),
                    rs.getString("ViTri"),
                    rs.getString("MaDB")
                ));
            }
        }
        
        return result;
    }
}
