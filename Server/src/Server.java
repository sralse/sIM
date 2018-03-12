import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class Server {
    protected static volatile ArrayList<Socket> clients = new ArrayList<>();
    private static Communication sqlThread;

    static {
        sqlThread = null;
    }

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
                    if (s.toLowerCase().equals("-o")) serverPort = args[i];
                    else if (s.toLowerCase().equals("-h")) sqlHost = args[i];
                    else if (s.toLowerCase().equals("-u")) sqlUser = args[i];
                    else if (s.toLowerCase().equals("-p")) sqlPass = args[i];
                    else if (s.toLowerCase().equals("-s")) sqlPort = args[i];
                    else if (s.toLowerCase().equals("-d")) sqlDatabase = args[i];
                }
                if (s.toLowerCase().equals("--sqlite")) sqlite = true;
                else if (s.toLowerCase().equals("--debug")) debug = true;
                else if (s.toLowerCase().equals("--help")) {
                    System.out.println(Init.help);
                    if (args.length == 1) System.exit(0);
                }
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
        int i = 0;
        while(!sqlRunning) {
            if (i > 100) {
                error("Could not start SQL Thread. Timed out.", true);
                return;
            }
            Thread.sleep(50);
            i++;
        }
        sqlThread.start();
        log("SQL Thread started.");
    }

    private void startConsole() {
        new Console().start();
    }

    private void startListener() {
        try {
            ServerSocket ss = new ServerSocket(Integer.parseInt(serverPort));
            log("Chat Server started... listening on port: " + serverPort);
            while(running) {
                new Client((client = ss.accept())).start();
                log("Client connection from: " + client.getInetAddress().getHostAddress());
                clients.add(client);
            }
            warn("Shutting down server...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String s) {
        outputLog(s,false,false, false, null);
    }

    public static void debug(String s) {
        outputLog(s,false,true, false, null);
    }

    public static void warn(String s) {
        outputLog(s, true, false, false, null);
    }

    public static void error(String s, boolean error) {
        outputLog(s, error, false, true, null);
    }

    public static void error(String s, Exception e) {
        outputLog(s, true, false, true, e);
    }

    private static void outputLog(String s, boolean warning, boolean debug, boolean error, Exception e) {
        if(warning && error && e != null) System.err.println(System.currentTimeMillis()+" > [CRASH]: "+s+" : "+e.getMessage());
        else if(warning && error) System.err.println(System.currentTimeMillis()+" > [CRITICAL]: "+s);
        else if(error) System.err.println(System.currentTimeMillis()+" > [ERROR]: "+s);
        else if(warning) System.out.println(System.currentTimeMillis()+" > [WARNING]: "+s);
        else if(debug && Server.debug) System.out.println(System.currentTimeMillis()+" > [DEBUG]: "+s);
        else System.out.println(System.currentTimeMillis()+" > "+s);
    }
}
