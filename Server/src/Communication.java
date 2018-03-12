import java.io.File;
import java.io.IOException;
import java.sql.*;

public class Communication extends Thread {
    public static Connection con;

    public Communication(String sqlDatabase) {
        String url = "jdbc:sqlite:" + sqlDatabase + ".db";
        System.out.println("Connecting to: " + url);
        try {
            File dbFile = new File(sqlDatabase+".db");
            if(!dbFile.exists()) dbFile.createNewFile();
            con = DriverManager.getConnection(url);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("PRAGMA user_version");
            if (rs.next()) System.out.println("Connected to database! " + rs.getString(1));
            Init.server.sqlRunning = !con.isClosed();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public Communication(String sqlHost, String sqlUser, String sqlPass, String sqlPort, String sqlDatabase) {
        String url = "jdbc:mysql://"+sqlHost+":"+sqlPort+"/";
        System.out.println("Connecting to: " + url);
        try {
            con = DriverManager.getConnection(url, sqlUser, sqlPass);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT VERSION()");
            if (rs.next()) System.out.println("Connected to database! " + rs.getString(1));
            Init.server.sqlRunning = !con.isClosed();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }  finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    @Override
    public void run() {
        System.out.println("SQL Thread running.");
        while(Init.server.sqlRunning) {
            //TODO check for a sent message.

        }
    }

    public static void makeDatabase(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("SHOW DATABASES LIKE 'chatServer';");
        if(rs.next()) {
            System.out.println(System.currentTimeMillis() + " > Database already exists: " + rs.getString(1));
            st.execute("USE chatServer;");
            rs = st.executeQuery("SHOW TABLES LIKE 'myTable';");
            result(rs);
            if(rs.next()) initTable(st); else result(rs);
        } else {
            System.out.println(System.currentTimeMillis() + " > Database does not exist");
            initDatabase(st);
            initTable(st);
        }
    }

    public static void result(ResultSet rs) throws SQLException {
        if (rs.next()) System.out.println("SQL returned: " + rs.getString(1));
    }

    private static void initDatabase(Statement st) throws SQLException {
        st.execute("CREATE DATABASE chatServer;");
    }

    private static void initTable(Statement st) throws SQLException {
        st.executeUpdate("USE chatServer;");
        st.execute("CREATE TABLE users (" +
                "ID int(64) NOT NULL AUTO_INCREMENT," +
                "userName varchar(255) NOT NULL," +
                "emailAdress varchar(255) NOT NULL," +
                "privateKey VARCHAR(255) NOT NULL," +
                "activated TINYINT(4) NOT NULL DEFAULT '0'," +
                "privileges INT(11) NOT NULL DEFAULT '0'," +
                "sessionKey VARCHAR(255) DEFAULT NULL," +
                "PRIMARY KEY (ID)," +
                "UNIQUE KEY `ID_UNIQUE` (`ID`)," +
                "UNIQUE KEY `userName_UNIQUE` (`userName`)," +
                "UNIQUE KEY `emailAdress_UNIQUE` (`emailAdress`)," +
                "UNIQUE KEY `sessionKey_UNIQUE` (`sessionKey`));");
    }
}
