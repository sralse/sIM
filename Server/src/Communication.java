import org.sqlite.JDBC;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class Communication extends Thread {
    public static Connection con;
    public static Statement st;

    public Communication(String sqlDatabase) {
        String url = "jdbc:sqlite:" + sqlDatabase + ".db";
        Server.log("Connecting to: " + url);
        try {
            File dbFile = new File(sqlDatabase+".db");
            if(!dbFile.exists()) dbFile.createNewFile();
            DriverManager.registerDriver(new JDBC());
            con = DriverManager.getConnection(url);
            st = con.createStatement();
            ResultSet rs = st.executeQuery("PRAGMA user_version");
            if (rs.next()) Server.log("Connected to database! " + rs.getString(1));
            Server.sqlRunning = !con.isClosed();
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ex) {
                Server.error(ex.getMessage(), ex);
            }
        }
    }

    public Communication(String sqlHost, String sqlUser, String sqlPass, String sqlPort, String sqlDatabase) {
        String url = "jdbc:mysql://"+sqlHost+":"+sqlPort+"/";
        Server.log("Connecting to: " + url);
        try {
            con = DriverManager.getConnection(url, sqlUser, sqlPass);
            if (con.isClosed()) {
                Server.error("Could not make database connection",true);
                Server.sqlRunning = false;
                return;
            }
            st = con.createStatement();
            Server.sqlRunning = true;
            ResultSet rs = st.executeQuery("SELECT VERSION()");
            if (rs.next()) Server.log("Connected to server! " + rs.getString(1));
            makeDatabase(st);
        } catch (SQLException ex) {
            Server.error("SQL Error",ex);
        }
    }

    @Override
    public void run() {
        Server.log("SQL Thread running.");
        while(Server.sqlRunning) {
            //TODO check for a sent message.

        }
        Server.warn("SQL Thread stopped");
    }

    public static void makeDatabase(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("SHOW DATABASES LIKE 'chatServer';");
        if(rs.next()) {
            Server.log("Database already exists: " + rs.getString(1));
            st.execute("USE chatServer;");
            rs = st.executeQuery("SHOW TABLES LIKE 'users';");
            result(rs);
            if(rs.next()) initTable(st); else result(rs);
        } else {
            Server.log("Database does not exist");
            initDatabase(st);
            initTable(st);
        }
    }

    public static void result(ResultSet rs) throws SQLException {
        if (rs.next()) Server.log("SQL returned: " + rs.getString(1));
    }

    private static void initDatabase(Statement st) throws SQLException {
        Server.log("Creating database 'chatServer'");
        st.execute("CREATE DATABASE IF NOT EXISTS chatServer;");
    }

    private static void initTable(Statement st) throws SQLException {
        Server.log("Creating table 'users'");
        st.executeUpdate("USE chatServer;");
        st.execute("CREATE TABLE IF NOT EXISTS users (" +
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

    public static void registerUser(Statement st, String username, String emailaddress,
                                    String privatekey, boolean activated, int privileges)
            throws SQLException, NoSuchAlgorithmException {
        if (st == null) st = Communication.st;
        st.executeUpdate("USE chatServer;");
        st.execute("INSERT INTO users VALUES (" +
                "null, "
                +username+", "
                +emailaddress+", "
                +Security.mda5(privatekey)+", "
                +activated+", "
                +privileges+");");

    }

    public static void close() throws SQLException {
        con.close();
    }
}
