import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{

    public static volatile PrintWriter out;
    private Socket client;

    public Client(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {

        try {
            (out = new PrintWriter(client.getOutputStream(), true)).println("Connection established.");
            Thread listener = new ClientListener(client);
            listener.start();
            while (listener.isAlive()) {

            }
            listener.join();
            Server.log("Client (" + client + ") has disconnected.");
            Server.clients.remove(client);
            return;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
