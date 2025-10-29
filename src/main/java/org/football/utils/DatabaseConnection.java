package org.football.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_USER = "sa";
    private static final String DB_PASS = "123456";

    private static final String WINTER_INSTANCE = "Winter";
    private static final String WINTER_DB_NAME = "QL_BongDa";

    private static final String DB1_INSTANCE = "localhost\\NGH1";
    private static final String DB1_NAME = "QL_BongDa_CLB1_SD1";

    private static final String DB2_INSTANCE = "localhost\\NGH2";
    private static final String DB2_NAME = "QL_BongDa_CLB2_SD2";

    private static Connection connectionWinter = null;
    private static Connection connection1 = null;
    private static Connection connection2 = null;

    public static Connection getConnectionGlobal() {
        try {
            if (connectionWinter == null || connectionWinter.isClosed()) {
                String url = "jdbc:sqlserver://" + WINTER_INSTANCE + 
                             ";databaseName=" + WINTER_DB_NAME + 
                             ";encrypt=true;trustServerCertificate=true";
                connectionWinter = DriverManager.getConnection(url, DB_USER, DB_PASS);
            }
            return connectionWinter;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getConnection1() {
        try {
            if (connection1 == null || connection1.isClosed()) {
                String url = "jdbc:sqlserver://" + DB1_INSTANCE + 
                             ";databaseName=" + DB1_NAME + 
                             ";encrypt=true;trustServerCertificate=true";
                connection1 = DriverManager.getConnection(url, DB_USER, DB_PASS);
            }
            return connection1;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Connection getConnection2() {
        try {
            if (connection2 == null || connection2.isClosed()) {
                String url = "jdbc:sqlserver://" + DB2_INSTANCE + 
                             ";databaseName=" + DB2_NAME + 
                             ";encrypt=true;trustServerCertificate=true";
                connection2 = DriverManager.getConnection(url, DB_USER, DB_PASS);
            }
            return connection2;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void closeAll() {
        try {
            if (connectionWinter != null && !connectionWinter.isClosed()) connectionWinter.close();
            if (connection1 != null && !connection1.isClosed()) connection1.close();
            if (connection2 != null && !connection2.isClosed()) connection2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}