
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class BClientListener extends Thread {

    private BufferedReader server;
    private LoginForm client;
    protected static boolean incomingMessage = false;

    public BClientListener(LoginForm client, BufferedReader out) {
        this.client = client;
        this.server = out;
    }

    public void run() {
        try {
            String serverInput, sender = null, receiver = null, message= null;
            while(client.isRunning() && (serverInput = server.readLine()) != null) {
                String args[] = serverInput.split(" ");
                if(incomingMessage) {
                    if (client.isDebugEnabled()) System.out.println("GET: "+serverInput);
                    if (args[0].equals("sender")) sender = args[1];
                    if (args[0].equals("receiver")) receiver = args[1];
                    if (args[0].equals("message")) message = serverInput.substring("message".length()+1,serverInput.length());
                    if (args[0].equals("end")) {
                        File file =new File(client.getLogPath());
                        if(!file.exists()) file.createNewFile();
                        FileWriter fw;
                        if (client.APPEND) fw = new FileWriter(file, true);
                        else fw = new FileWriter(file);
                        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                        pw.println(client.getLastMSGID());
                        pw.println(sender);
                        pw.println(receiver);
                        pw.println(fromHex(message));
                        pw.close();
                        incomingMessage = false;
                    }
                } else if(args[0].equals("token")) {
                    client.setToken(args[1]);
                    System.out.println("Server > " + serverInput);
                } else if(args[0].equals("compose")) {
                    client.setLastMSGID(Integer.valueOf(args[2]));
                    System.out.println("Incoming message ID: " + client.getLastMSGID());
                    incomingMessage = true; //tepels
                }
            }

        } catch (IOException e) {
            System.out.println("Client listener stopped: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String fromHex(String hex) {
        ByteBuffer buff = ByteBuffer.allocate(hex.length()/2);
        for (int i = 0; i < hex.length(); i+=2) buff.put((byte)Integer.parseInt(hex.substring(i, i+2), 16));
        buff.rewind();
        Charset cs = Charset.forName("UTF-8");
        CharBuffer cb = cs.decode(buff);
        return cb.toString();
    }
}
