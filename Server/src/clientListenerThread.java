import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class clientListenerThread extends Thread {
    private static float TIME_SINCE_LAST = 0, currentTime = 0, lastTime = 0;
    private Socket client;
    private String clientName;
    private String token;

    public clientListenerThread(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            String clientSays;
            PrintWriter out = new PrintWriter(client.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            ResultSet rs;
            while((clientSays = in.readLine()) != null) {
                if(serverHandle.debug) System.out.println(System.currentTimeMillis() +
                        " > CLIENT : (" + client + ") > " + clientSays);
                String[] args = clientSays.split(" ");
                Statement st = sqlCommunication.con.createStatement();
                if(TIME_SINCE_LAST <= 100f) {
                    currentTime = System.currentTimeMillis();
                    TIME_SINCE_LAST = currentTime - lastTime;
                } else if (args[0].equals("login") && args.length >= 3) {
                    try {
                        if (args[1].contains("@"))rs = st.executeQuery("SELECT emailAdress, privateKey" +
                                " FROM users WHERE emailAdress = '"+args[1]+"' AND privateKey  = '"+args[2]+"';");
                        else rs = st.executeQuery("SELECT userName, privateKey" +
                                " FROM users WHERE userName = '"+args[1]+"' AND privateKey  = '"+args[2]+"';");
                        if(rs.next()) {
                            sqlCommunication.result(rs);
                            if(args[1].contains("@")) {
                                rs = st.executeQuery("SELECT userName FROM users WHERE emailAdress = '"+args[1]+"';");
                                if(rs.next()) clientName = rs.getString(1);
                                else System.err.println(System.currentTimeMillis() +
                                        " > No user found for email adress: " + args[1]);
                            } else clientName = args[1];
                            token = sqlCommunication.mda5(String.valueOf(Math.random()));
                            out.println("token " + token);
                            st.executeUpdate("UPDATE users SET sessionKey = '" + token +
                                    "' WHERE userName = '"+clientName+"' AND privateKey  = '"+args[2]+"';");
                            System.out.println(System.currentTimeMillis() +
                                    " > Client("+clientName+") logged in with token: " + token);
                        }
                    } catch (SQLException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                } else if (args[0].equals("say") && args.length > 3) {
                    if (token == null) token = args[1];
                    getClientInfo(st);
                    rs = st.executeQuery("SELECT userName FROM users " +
                            "WHERE userName = '" + args[2] + "';");
                    if(rs.next()) {
                        // Here we handle our sending of a message.
                        System.out.println(System.currentTimeMillis() + " > Sending message to: " + args[2]);
                        String msg = clientSays.substring(
                                args[0].length()+args[1].length()+args[2].length() + 3, clientSays.length());
                        st.executeUpdate(
                                "INSERT INTO chatLog VALUES " +
                                        "(NULL,'"+clientName+"','"+args[2]+"','"+msg+"',NULL);");
                    } else {
                        sqlCommunication.result(rs);
                        System.err.println(System.currentTimeMillis() + " > User not found.");
                        return;
                    }
                } else if (args[0].equals("get") && (args.length > 2 || args.length < 4)) {
                    if (token == null) token = args[1];
                    getClientInfo(st);
                    rs = st.executeQuery("SELECT userName FROM users " +
                            "WHERE sessionKey = '" + token + "';");
                    if(rs.next()) {
                        clientName = rs.getString(1);
                        System.out.println(System.currentTimeMillis() + " > Retrieving info for user: " + clientName);
                        if(args.length == 2) rs = st.executeQuery("SELECT messageID, sender, receiver, message FROM chatLog" +
                                " WHERE receiver = '"+clientName+"' || sender = '"+clientName+"';");
                        else rs = st.executeQuery("SELECT messageID, sender, receiver, message FROM chatLog" +
                                " WHERE (receiver = '"+clientName+"' || sender = '"+clientName+"') && messageID > "+args[2]+";");
                        while(rs.next()) {
                            out.println("compose id " + rs.getString(1) +
                                    "\nsender "+ rs.getString(2) +
                                    "\nreceiver "+ rs.getString(3)+
                                    "\nmessage "+ toHex(rs.getString(4))+
                                    "\nend");
                        }
                    } else {
                        sqlCommunication.result(rs);
                        System.err.println(System.currentTimeMillis() + " > User not found.");
                        return;
                    }
                }
                lastTime = System.currentTimeMillis();
            }
            System.out.println(System.currentTimeMillis() + " > Client has disconnected.");
        } catch (IOException e) {
            System.err.println(System.currentTimeMillis() + " > Listener thread stopped: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println(System.currentTimeMillis() + " > SQL ERROR.");
            e.printStackTrace();
        }
    }

    public void getClientInfo(Statement st) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT sessionKey FROM users " +
                "WHERE sessionKey = '" + token + "';");
        if(!rs.next()) return;
        if(clientName == null) {
            rs = st.executeQuery("SELECT userName FROM users" +
                    " WHERE sessionKey = '"+token+"';");
            if(rs.next()) clientName = rs.getString(1);
        }
    }

    public static String toHex(String text) throws UnsupportedEncodingException
    {
        byte[] myBytes = text.getBytes("UTF-8");

        return DatatypeConverter.printHexBinary(myBytes);
    }
}
