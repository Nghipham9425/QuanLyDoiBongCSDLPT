package org.football.models;

/**
 * Entity class representing Participation (ThamGia)
 * Database: ThamGia (MaTD, MaCT, SoTrai)
 */
public class ThamGia {
    private String maTD;        // Mã trận đấu (Foreign Key)
    private String maCT;        // Mã cầu thủ (Foreign Key)
    private int soTrai;         // Số trái (số bàn thắng)

    public ThamGia() {
    }

    public ThamGia(String maTD, String maCT, int soTrai) {
        this.maTD = maTD;
        this.maCT = maCT;
        this.soTrai = soTrai;
    }

    // Getters and Setters
    public String getMaTD() {
        return maTD;
    }

    public void setMaTD(String maTD) {
        this.maTD = maTD;
    }

    public String getMaCT() {
        return maCT;
    }

    public void setMaCT(String maCT) {
        this.maCT = maCT;
    }

    public int getSoTrai() {
        return soTrai;
    }

    public void setSoTrai(int soTrai) {
        this.soTrai = soTrai;
    }

    @Override
    public String toString() {
        return "ThamGia{" +
                "maTD='" + maTD + '\'' +
                ", maCT='" + maCT + '\'' +
                ", soTrai=" + soTrai +
                '}';
    }
}
