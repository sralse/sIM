import java.sql.SQLException;

public class Init {
    public static Server server = null;
    public static final String help =
            "This jar will use sqLite if no MySQL data is provided or if -sqlite is enforced or if no MySQL client is found." +
                    "\n-o [port]        Obligated PORT_NUMBER for the server to run on. Default is 45459" +
                    "\n-h [hostname]    If you are using a remote MySQL server host" +
                    "\n-u [user]        User for the MySQL database" +
                    "\n-p [password]    Password of the database" +
                    "\n-s [sql port]    Port of the MySQL server" +
                    "\n-d [database]    Name of database (or file to store data if using sqlite)" +
                    "\n--sqlite         Enforce the use of sqlite" +
                    "\n--help           Show this dialog";

    public static void main(String args[]) throws InterruptedException, SQLException, ClassNotFoundException {
        if (args.length > 12) {
            System.err.println("Usage: java -jar server.jar <port> -h [hostname] -u [user] -p [password] " +
                    "-s [sql port] -d [database] --sqlite --help" + "\n" + help);
            System.exit(-1);
        }
        server = new Server(args);
    }
}
