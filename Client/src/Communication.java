import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Communication {
    private static Socket socket;
    private static PrintWriter pw;
    private static ServerListener listener;

    public static boolean testConnection(String ip, int port, int timeout) {
        try {
            InetSocketAddress i = new InetSocketAddress(ip, port);
            Console.debug("Connecting to: "+i.getAddress()+" port: "+i.getPort());
            Socket s = new Socket();
            s.connect(i, 10000);
            s.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean testConnection(String ip, String port) {
        try {
            InetSocketAddress i = new InetSocketAddress(ip, Integer.parseInt(port));
            Console.debug("Connecting to: "+i.getAddress()+":"+i.getPort());
            Socket s = new Socket();
            s.connect(i, 10000);
            s.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean testConnection(Socket ss) {
        return !ss.isClosed();
    }

    public static void sendRaw(String s) {
        while (pw == null) {
            pw.println(s);
        }
    }

    public static void login(String user, String userpass) {
        if (pw != null) {
            Console.log("Logging in with user: " + user + " password: " + userpass);
            pw.println("login " + user + " " + userpass);
        } else Console.error("Could not get PrintWriter.",false);
    }

    public static Socket initConnection(String host, String port) {
        try {
            socket = new Socket(host, Integer.parseInt(port));
            listener = new ServerListener(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            listener.start();
            pw = new PrintWriter(socket.getOutputStream(), true);
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ServerListener getListener() {
        return listener;
    }
}
