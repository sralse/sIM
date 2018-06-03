import java.net.Socket;

public class Console {
    protected static boolean debug = false;
    public static ServerListener sl = null;
    public static boolean init;
    private Socket ss = null;
    public static String
            user = "",
            userpass = "",
            host = "",
            logfile = "logfile.txt",
            port = "45459",
            token = "";

    public Console(String[] args) {
        // Save arguments
        int i = 1, j = args.length;
        for (String s:args) {
            if (s.equals("-h") && i < j) host = args[i];
            else if (s.equals("-f") && i < j) logfile = args[i];
            else if (s.equals("-u") && i < j) user = args[i];
            else if (s.equals("-pw") && i < j) userpass = Security.mda5(args[i]);
            else if (s.equals("-p") && i < j) port = args[i];
            else if (s.equals("--debug")) debug = true;
            i++;
        }
        Console.debug("ENABLED - Created console with arguments: "
                +"host="+host+":"+port+" user="+user+" pw="+userpass+" logfile="+logfile);
        setup();
    }

    private void setup() {
        if (init) return;
        // Set up the textfields with the default data.
        Client.client.spinner1.setValue(Integer.parseInt(port));
        debug("Setting host to: "+host);
        Client.client.txtFieldServer.setText(host);
        Client.client.txtFieldLogFile.setText(logfile);
        Client.client.txtFieldUser.setText(user);
        init = true;
    }

    public void login() {
        // Status update
        Client.client.setStatus("Logging in...",false);
        Client.client.setLoading(4);

        // Check if we can connect (aka valid host)
        if(!Communication.testConnection(host, port)) {
            Client.client.setStatus(host+":"+port+" not found.", true);
            return;}
        Client.client.setStatus("Connected to: "+host+":"+port, false);
        Client.client.addLoading();

        // Initialize server connection
        initServerConnection(host, port);
        Client.client.addLoading();

        // Check if we have valid userdata
        if(!validate()) return;
        Client.client.addLoading();

        // Get credentials
        if(!getCredentials()){
            Client.client.setStatus("Wrong credentials.",true);
            return;}

        // We logged in succesfully
        Client.client.setStatus("Connected succesfully.",false);
        Client.client.addLoading();
        Client.client.enableChat();

        // Get all messages
        Client.client.setLoading(1);
        Client.client.setStatus("Loading messages...", false);
        MessageList.init();

        // Show messages
        Client.client.setStatus("Done.", false);
        Client.client.addLoading();
        Client.client.txtFieldChat.requestFocus();

        // Select current user
        Client.client.listUsers.setSelectedValue(MessageList.getLastUser(), true);

        // Start refresh thread
        Communication.startThread();
    }

    private boolean getCredentials() {
        Communication.login(user,userpass);
        int i=0;
        while (i < 100) {
            try {
                if (token != null && !token.equals("")) {
                    log("Server has returned token: "+token);
                    return true;
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        // todo wait for feedback
        return false;
    }

    private boolean validate() {
        debug("Logging in with user data: user="+user+" userpass="+userpass);
        if(user == null || userpass == null || user.equals("") || userpass.equals("")) {
            error("Userdata cannot be null", false);
            Client.client.setStatus("Please fill in all the forms.",true);
            return false;
        } return true;
    }


    public static void setToken(String token) {
        Console.token = token;
    }

    public boolean isAlive() {
        return true;
    }

    public static void log(String s) {
        System.out.println(System.currentTimeMillis()+" > "+s);
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
        if(warning && error && e != null) System.err.println(System.currentTimeMillis()+" [CRASH]> "+s+" : "+e.getMessage());
        else if(warning && error) System.err.println(System.currentTimeMillis()+" [CRITICAL]> "+s);
        else if(error) System.err.println(System.currentTimeMillis()+" [ERROR]> "+s);
        else if(warning) System.out.println(System.currentTimeMillis()+" [WARNING]> "+s);
        else if(debug && Console.debug) System.out.println(System.currentTimeMillis()+" [DEBUG]> "+s);
    }

    public void register(String user, String email, char[] password, String text, int p) {
        String port = String.valueOf(p);
        // Status update
        Client.client.setStatus("Registering...",false);
        Client.client.setLoading(3);

        // Check if we can connect (aka valid host)
        if(!Communication.testConnection()) {
            if (!Communication.testConnection(host, port)) {
                Client.client.setStatus(text + ":" + port + " not found.", true);
                return;
            }
            // Initialise server connection
            initServerConnection(host, port);
            Client.client.addLoading();
        }
        Client.client.setStatus("Connected to: "+text+":"+port, false);
        Client.client.addLoading();

        Communication.register(
                user,
                email,
                String.valueOf(password));
    }

    private void initServerConnection(String text, String port) {
        // Initialize server connection
        if(ss != null && sl != null) {
            if (ss.isConnected() && sl.isAlive()) {
                debug("Server Socket: "+ss.isConnected()+" Server Listener: "+sl.isAlive());
                return;
            }
        }
        ss = Communication.initConnection(text, port);
        sl = Communication.getListener();
        if(!Communication.testConnection(ss)) {
            Client.client.setStatus(text+":"+port+" not found.",true); return;}
    }
}
