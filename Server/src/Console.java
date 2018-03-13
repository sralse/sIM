import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Console extends Thread {

    private String msg;
    private PrintWriter out;
    private String args[];
    private Scanner console;
    private final String[] help = {"HELP_CODE"};

    @Override
    public void run() {
        try {
            console = new Scanner(System.in);
            Server.log("Starting console.");
            while((msg = console.nextLine()) != null) {
                args = msg.toLowerCase().split(" ");
                if (args[0].equals("help")) {
                    CommandHelp();
                } else if (args[0].equals("mda5")) {
                    CommandMDA5(args);
                } else if(args[0].equals("say")) {
                    CommandSay(args);
                } else if (args[0].equals("list")) {
                    CommandList(args);
                } else if(args[0].equals("restart")) {
                    CommandRestart(args);
                } else if(args[0].equals("register")) {
                    CommandRegister(args);
                } else if(args[0].equals("stop")) {
                    CommandStop(args);
                } else {
                    Server.error("No command found: " + msg,false);
                }
            }
        } catch (IOException | NoSuchAlgorithmException | SQLException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void CommandHelp() throws NoSuchAlgorithmException, IOException, SQLException {
        Server.log("Commands available:");
        // TODO Make a commands class or interface. Stuff it with it's usage and the functions?
        CommandMDA5(help);
        CommandList(help);
        CommandSay(help);
        CommandRegister(help);
        CommandRestart(help);
    }

    private void CommandMDA5(String[] args) throws NoSuchAlgorithmException {
        if(args.equals(help)) {
            Server.log("mda5 <String>           This will return the MDA5 hashed instance to console.");
            return;
        }
        if (msg.length() < 6) {
            Server.log("Usage: mda5 <String>");
        }
        msg = Security.mda5(msg.substring(5,msg.length()));
        Server.log("The MDA5 hash is: " + msg);
    }

    private void CommandSay(String[] args) throws IOException {
        if(args.equals(help)) {
            Server.log("say [String clientID] <String msg> "
                    + "\n                                        This send a message to all or a specified user.");
            return;
        }
        if(this.args.length == 1) {
            Server.log("Usage: say [clientID] <message>");
            return;
        }
        msg = msg.substring(4,msg.length());
        for (Socket client: Server.clients) {
            out = new PrintWriter(client.getOutputStream(), true);
            out.println(msg);
        }
        Server.log("Console @all > " + msg);
    }

    private void CommandList(String[] args) {
        if(args.equals(help)) {
            Server.log("list                    This will return a list of active users.");
            return;
        }
        Server.log("All clients present:");
        if (Init.server.clients.size() > 0) {
            for (Socket client : Server.clients) {
                Server.log("CLIENT @ " + client);
            }
        } else {
            Server.log("There are currently no clients connected.");
        }
    }

    private void CommandRestart(String[] args) {
        if(args.equals(help)) {
            Server.log("restart                 This will restart the server.");
            return;
        }
        //TODO Restart thread
        Server.warn("Restarting server");
    }

    private void CommandRegister(String[] args) throws SQLException, NoSuchAlgorithmException {
        if(args.equals(help)) {
            Server.log("register <String user> <String email> <String password> [boolean activated] [int privileges]"
                    + "\n                                        This will create a new user in the chatServer "
                    + "\n                                        database under the users table.");
            return;
        }
        if (this.args.length > 3) {
            boolean b = false;
            int i = 0;
            if (this.args.length >= 5) b = Boolean.getBoolean(this.args[4]);
            if (this.args.length >= 6) i = Integer.parseInt(this.args[5]);
            Communication.registerUser(Communication.con.createStatement(), this.args[1], this.args[2], this.args[3],b,i);
        } else Server.warn("Registering a user needs a minimal of 3 arguments.");
    }

    private void CommandStop(String[] args) throws SQLException, IOException, InterruptedException {
        if(args.equals(help)) {
            Server.log("stop                    This will stop the server.");
            return;
        }
        Server.warn("Stopping server...");
        Communication.close();
        Server.sqlRunning = false;
        Server.running = false;
        for (Socket c:Server.clients) c.close();
        Server.sqlThread.join();
        System.exit(0);
    }
}
