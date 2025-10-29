package org.football.models;

/**
 * Entity class representing Match (TranDau)
 * Database: TranDau (MaTD, MaDB1, MaDB2, TrongTai, SanDau)
 */
public class TranDau {
    private String maTD;        // Mã trận đấu
    private String maDB1;       // Mã đội bóng 1 (Foreign Key)
    private String maDB2;       // Mã đội bóng 2 (Foreign Key)
    private String trongTai;    // Trọng tài
    private String sanDau;      // Sân đấu

    public TranDau() {
    }

    public TranDau(String maTD, String maDB1, String maDB2, String trongTai, String sanDau) {
        this.maTD = maTD;
        this.maDB1 = maDB1;
        this.maDB2 = maDB2;
        this.trongTai = trongTai;
        this.sanDau = sanDau;
    }

    // Getters and Setters
    public String getMaTD() {
        return maTD;
    }

    public void setMaTD(String maTD) {
        this.maTD = maTD;
    }

    public String getMaDB1() {
        return maDB1;
    }

    public void setMaDB1(String maDB1) {
        this.maDB1 = maDB1;
    }

    public String getMaDB2() {
        return maDB2;
    }

    public void setMaDB2(String maDB2) {
        this.maDB2 = maDB2;
    }

    public String getTrongTai() {
        return trongTai;
    }

    public void setTrongTai(String trongTai) {
        this.trongTai = trongTai;
    }

    public String getSanDau() {
        return sanDau;
    }

    public void setSanDau(String sanDau) {
        this.sanDau = sanDau;
    }

    @Override
    public String toString() {
        return "TranDau{" +
                "maTD='" + maTD + '\'' +
                ", maDB1='" + maDB1 + '\'' +
                ", maDB2='" + maDB2 + '\'' +
                ", trongTai='" + trongTai + '\'' +
                ", sanDau='" + sanDau + '\'' +
                '}';
    }
}
