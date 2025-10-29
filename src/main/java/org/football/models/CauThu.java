package org.football.models;

/**
 * Entity class representing Player (CauThu)
 * Database: CauThu (MaCT, HoTen, ViTri, MaDB)
 */
public class CauThu {
    private String maCT;        // Mã cầu thủ
    private String hoTen;       // Họ tên
    private String viTri;       // Vị trí
    private String maDB;        // Mã đội bóng (Foreign Key)

    public CauThu() {
    }

    public CauThu(String maCT, String hoTen, String viTri, String maDB) {
        this.maCT = maCT;
        this.hoTen = hoTen;
        this.viTri = viTri;
        this.maDB = maDB;
    }

    // Getters and Setters
    public String getMaCT() {
        return maCT;
    }

    public void setMaCT(String maCT) {
        this.maCT = maCT;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public String getMaDB() {
        return maDB;
    }

    public void setMaDB(String maDB) {
        this.maDB = maDB;
    }

    @Override
    public String toString() {
        return "CauThu{" +
                "maCT='" + maCT + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", viTri='" + viTri + '\'' +
                ", maDB='" + maDB + '\'' +
                '}';
    }
}
