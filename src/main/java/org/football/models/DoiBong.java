package org.football.models;

/**
 * Entity class representing Team (DoiBong)
 * Database: DoiBong (MaDB, TenDB, CLB)
 */
public class DoiBong {
    private String maDB;
    private String tenDB;
    private String clb;
    private int soCauThu;

    public DoiBong() {
    }

    public DoiBong(String maDB, String tenDB, String clb) {
        this.maDB = maDB;
        this.tenDB = tenDB;
        this.clb = clb;
        this.soCauThu = 0;
    }

    public String getMaDB() {
        return maDB;
    }

    public void setMaDB(String maDB) {
        this.maDB = maDB;
    }

    public String getTenDB() {
        return tenDB;
    }

    public void setTenDB(String tenDB) {
        this.tenDB = tenDB;
    }

    public String getClb() {
        return clb;
    }

    public void setClb(String clb) {
        this.clb = clb;
    }

    public int getSoCauThu() {
        return soCauThu;
    }

    public void setSoCauThu(int soCauThu) {
        this.soCauThu = soCauThu;
    }

    @Override
    public String toString() {
        return "DoiBong{" +
                "maDB='" + maDB + '\'' +
                ", tenDB='" + tenDB + '\'' +
                ", clb='" + clb + '\'' +
                '}';
    }
}
