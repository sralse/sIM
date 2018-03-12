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
            System.out.println("Starting console.");
            while((msg = console.nextLine()) != null) {
                args = msg.toLowerCase().split(" ");
                if (args[0].equals("help")) {
                    System.out.println("Commands available:");
                    // TODO Make a commands class or interface. Stuff it with it's usage and the functions?
                } else if (args[0].equals("mda5")) {
                    if (msg.length() < 6) {
                        System.out.println("Usage: mda5 <string>");
                    }
                    msg = Security.mda5(msg.substring(5,msg.length()));
                    System.out.println("The MDA5 hash is: " + msg);
                } else if(args[0].equals("say")) {
                    if(args.length == 1) {
                        System.out.println("Usage: say [clientID] <message>");
                        return;
                    }
                    msg = msg.substring(4,msg.length());
                    for (Socket client: Init.server.clients) {
                        out = new PrintWriter(client.getOutputStream(), true);
                        out.println(msg);
                    }
                    System.out.println("Console @all > " + msg);
                } else if (args[0].equals("list")) {
                    System.out.println("All clients present:");
                    if (Init.server.clients.size() > 0) {
                        for (Socket client : Init.server.clients) {
                            System.out.println("CLIENT @ " + client);
                        }
                    } else {
                        System.out.println("There are currently no clients connected.");
                    }
                } else if(args[0].equals("restart")) {
                    //TODO Restart thread
                    System.out.println(System.currentTimeMillis() + " > Restarting server");
                } else {
                    System.out.println("No command found: " + msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
