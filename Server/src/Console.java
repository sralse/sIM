import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Console extends Thread {
    @Override
    public void run() {
        try {
            Scanner console = new Scanner(System.in);
            String args[];
            String msg;
            PrintWriter out;
            Server.log("Starting console.");
            while((msg = console.nextLine()) != null) {
                args = msg.toLowerCase().split(" ");
                if (args[0].equals("help")) {
                    Server.log("Commands available:");
                    // TODO Make a commands class or interface. Stuff it with it's usage and the functions?
                } else if (args[0].equals("mda5")) {
                    if (msg.length() < 6) {
                        Server.log("Usage: mda5 <string>");
                    }
                    msg = Security.mda5(msg.substring(5,msg.length()));
                    Server.log("The MDA5 hash is: " + msg);
                } else if(args[0].equals("say")) {
                    if(args.length == 1) {
                        Server.log("Usage: say [clientID] <message>");
                        return;
                    }
                    msg = msg.substring(4,msg.length());
                    for (Socket client: Init.server.clients) {
                        out = new PrintWriter(client.getOutputStream(), true);
                        out.println(msg);
                    }
                    Server.log("Console @all > " + msg);
                } else if (args[0].equals("list")) {
                    Server.log("All clients present:");
                    if (Init.server.clients.size() > 0) {
                        for (Socket client : Server.clients) {
                            Server.log("CLIENT @ " + client);
                        }
                    } else {
                        Server.log("There are currently no clients connected.");
                    }
                } else if(args[0].equals("restart")) {
                    //TODO Restart thread
                    Server.warn("Restarting server");
                } else {
                    Server.error("No command found: " + msg,false);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
