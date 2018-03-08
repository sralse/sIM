import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class sqlCommunication extends Thread {
    public static Connection con;

    public sqlCommunication(String[] args) throws SQLException, ClassNotFoundException {
        con = sqlConnection(args[0], args[1], args[2], args[3], args[4]);
    }

    private Connection sqlConnection(String userName, String password, String serverAdress, String portNumber, String db) throws SQLException, ClassNotFoundException {
        if(db == null) db = "";
        Connection con = null;
        Statement st;
        ResultSet rs;
        String url = "jdbc:mysql://"+serverAdress+":"+portNumber+"/";
        System.out.println("Connecting to: " + url);
        try {
            con = DriverManager.getConnection(url, userName, password);
            st = con.createStatement();
            if(db.equals("")) makeDatabase(st);
            rs = st.executeQuery("SELECT VERSION()");
            if (rs.next()) System.out.println("Connected to database! " + rs.getString(1));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return con;
    }

    @Override
    public void run() {
        System.out.println("SQL Thread running.");
        while(serverHandle.running) {
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

    @SuppressWarnings("Duplicates")
    public static String mda5(String pass) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        byte[] data = pass.getBytes();
        m.update(data,0,data.length);
        BigInteger i = new BigInteger(1,m.digest());
        return String.format("%1$032X", i);
    }

    public static String sha256(String pass) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        //String(digest.digest(pass.getBytes(StandardCharsets.UTF_8), pass);
        return pass;
    }
}
