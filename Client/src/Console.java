import java.net.Socket;

public class Console {
    protected static boolean debug = false;
    public static ServerListener sl = null;
    private Socket ss;
    public static String
            user = "",
            userpass = "",
            host = "full.sralse.xyz",
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
        Console.debug("Debug Enabled - Created console with arguments: "
                +host+":"+port+" user="+user+" pw="+userpass+" logfile="+logfile);
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
        ss = Communication.initConnection(host, port);
        sl = Communication.getListener();
        if(!Communication.testConnection(ss)) {
            Client.client.setStatus(host+":"+port+" not found.",true); return;}
        Client.client.addLoading();

        // Check if we have valid userdata
        if(!validate()) return;
        Client.client.addLoading();

        // Get credentials
        if(!getCredentials()){
            Client.client.setStatus("Wrong credentials.",true);
            return;}

        getAllMessages();
        // We logged in succesfully
        Client.client.setStatus("Connected succesfully.",false);
        Client.client.addLoading();
        Client.client.enableChat();
    }

    private void getAllMessages() {

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
}
