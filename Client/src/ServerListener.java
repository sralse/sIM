
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class ServerListener extends Thread {

    protected static boolean incomingMessage = false;
    private BufferedReader server;

    public ServerListener(BufferedReader server) {
        this.server = server;
    }

    public void run() {
        try {
            String serverInput, sender = null, receiver = null, message= null;
            while((serverInput = server.readLine()) != null) {
                String args[] = serverInput.split(" ");
                if(incomingMessage) {
                    if (Console.debug) Console.debug("GET: "+serverInput);
                    if (args[0].equals("sender")) sender = args[1];
                    if (args[0].equals("receiver")) receiver = args[1];
                    if (args[0].equals("message")) message = serverInput.substring("message".length()+1,serverInput.length());
                    if (args[0].equals("end")) {
                        File file =new File(Console.logfile);
                        if(!file.exists()) file.createNewFile();
                        FileWriter fw;
                        fw = new FileWriter(file);
                        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));
                        pw.println(MessageList.getLastMSGID());
                        pw.println(sender);
                        pw.println(receiver);
                        pw.println(fromHex(message));
                        pw.close();
                        MessageList.addMessage(MessageList.getLastMSGID(), sender, receiver, fromHex(message));
                        incomingMessage = false;
                    }
                } else if(args[0].equals("token")) {
                    Console.setToken(args[1]);
                    Console.debug("Server > " + serverInput);
                } else if(args[0].equals("compose")) {
                    MessageList.setLastMSGID(Integer.valueOf(args[2]));
                    Console.debug("Incoming message ID: " + MessageList.getLastMSGID());
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
