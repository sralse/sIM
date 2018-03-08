import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class clientThread extends Thread{

    public static volatile PrintWriter out;
    private Socket client;

    public clientThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {

        try {
            (out = new PrintWriter(client.getOutputStream(), true)).println("Connection established.");
            Thread listener = new clientListenerThread(client);
            listener.start();
            while (listener.isAlive()) {

            }
            listener.join();
            System.out.println(System.currentTimeMillis() + " > Client (" + client + ") has disconnected.");
            serverHandle.clients.remove(client);
            return;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
