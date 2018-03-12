import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class Server {
    public static volatile ArrayList<Socket> clients = new ArrayList<>();
    private static Communication sqlThread = null;
    private static Socket client;
    public static boolean
            running = true,
            sqlRunning = false,
            debug = false,
            sqlite = false;
    private static String
            sqlHost = "127.0.0.1",
            sqlUser = "root",
            sqlPass = "root",
            sqlPort = "3306",
            sqlDatabase = "chatServer",
            serverPort = "45459";


    public Server(String[] args) throws InterruptedException, SQLException, ClassNotFoundException {
        int i = 1;
        int l = args.length;
        for (String s : args) {
            if (i < l + 1) {
                if (i < l) {
                    if (s.toLowerCase().equals("-p")) serverPort = args[i];
                    if (s.toLowerCase().equals("-h")) sqlHost = args[i];
                    if (s.toLowerCase().equals("-u")) sqlUser = args[i];
                    if (s.toLowerCase().equals("-p")) sqlPass = args[i];
                    if (s.toLowerCase().equals("-s")) sqlPort = args[i];
                    if (s.toLowerCase().equals("-d")) sqlDatabase = args[i];
                }
                if (s.toLowerCase().equals("--sqlite")) sqlite = true;
                if (s.toLowerCase().equals("--help")) System.out.println(Init.help);
            }
            i++;
        }
        startSQLCommunication();
        startConsole();
        startListener();
    }

    private void startSQLCommunication() throws SQLException, ClassNotFoundException, InterruptedException {
        if(sqlite) sqlThread = new Communication(sqlDatabase);
        else sqlThread = new Communication(sqlHost,sqlUser,sqlPass, sqlPort,sqlDatabase);
        sqlThread.start();
        while(sqlRunning = false) Thread.sleep(10);
        System.out.println("SQL Thread started.");
        return;
    }

    private void startConsole() {
        new Console().start();
    }

    private void startListener() {
        try {
            ServerSocket ss = new ServerSocket(Integer.parseInt(serverPort));
            System.out.println("Chat Server started... listening on port: " + serverPort);
            while(running) {
                new Client((client = ss.accept())).start();
                System.out.println(System.currentTimeMillis() + " > Client connection from: " + client.getInetAddress().getHostAddress());
                clients.add(client);
            }
            System.out.println("Shutting down server...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
