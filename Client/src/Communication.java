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
    private static boolean init;

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
        if(init) return socket;
        try {
            socket = new Socket(host, Integer.parseInt(port));
            listener = new ServerListener(new BufferedReader(new InputStreamReader(socket.getInputStream())));
            listener.start();
            pw = new PrintWriter(socket.getOutputStream(), true);
            init = true;
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ServerListener getListener() {
        return listener;
    }

    public static void register(String user, String email, String s) {
        if (pw != null) {
            String pass = Security.mda5(s);
            Console.log("Registering user: " + user + " email: " + email + " pass: " + pass);
            pw.println("register " + user + " " + email + " " + pass);
        } else Console.error("Could not get PrintWriter.",false);
    }

    // Returns state of connection
    public static boolean testConnection() {
        if (socket == null) return false;
        if(!socket.isConnected()) return false;
        return !socket.isClosed();
    }

    public static void getMessages() {
        if(!init) return;
        pw.println("get "+Console.token);
        try {
            Thread.sleep(1000);
            while(listener.incomingMessage) Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String text, String receiver) {
        // TODO Add message to messagelist
        pw.println("say "+Console.token+" "+receiver+" "+text);
    }
}
