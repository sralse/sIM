import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class serverHandle {
    public static volatile boolean running = true, debug = false, gui = true;
    public static volatile ArrayList<Socket> clients = new ArrayList<>();
    public static volatile int port;
    public static volatile String sqlUser = "root", sqlPort = "3306", sqlDatabase, sqlHost = "127.0.0.1";
    private static Socket client;
    private static sqlCommunication sqlThread;

    public static void main(String args[]) throws InterruptedException, SQLException, ClassNotFoundException {
        if (args.length == 0 || args.length > 11) {
            System.err.println("Usage: java server <port> -u [sql user] -p [sql password] -s [sql port] -d [sql database]");
            System.exit(-1);
        }
        port = Integer.parseInt(args[0]);
        int i = 1;
        String sqlPass = null;
        for (String s:args) {
            if (i < args.length) {
                if (s.toLowerCase().equals("-u")) sqlUser = args[i];
                if (s.toLowerCase().equals("-p")) sqlPass = args[i];
                if (s.toLowerCase().equals("-s")) sqlPort = args[i];
                if (s.toLowerCase().equals("-d")) sqlDatabase = args[i];
                if (s.toLowerCase().equals("-h")) sqlHost = args[i];
                if (s.toLowerCase().equals("-nogui")) gui = false;
            }
            i++;
        }
        if (!gui) System.out.println("Starting in console mode...");
        startSQLCommunication(new String[]{sqlUser, sqlPass, sqlHost, sqlPort, sqlDatabase});
        while (!sqlThread.isAlive()) Thread.sleep(100);
        startConsole();
        startListener();
    }

    private static void startSQLCommunication(String[] args) throws SQLException, ClassNotFoundException {
        (sqlThread = new sqlCommunication(args)).start();
        System.out.println("SQL Thread started.");
        return;
    }

    private static void startConsole() {
        new serverConsole().start();
    }

    private static void startListener() {
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("Chat Server started... listening on port: " + port);
            while(running) {
                new clientThread((client = ss.accept())).start();
                System.out.println(System.currentTimeMillis() + " > Client connection from: " + client.getInetAddress().getHostAddress());
                clients.add(client);
            }
            System.out.println("Shutting down server...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
