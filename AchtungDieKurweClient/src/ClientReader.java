import java.io.*;
import java.net.Socket;

public class ClientReader implements Runnable {


    private final Thread thread;
    private final ClientTCP client;
    private final Socket socket;
    private BufferedReader in;


    private boolean running;

    public ClientReader(ClientTCP client, Socket socket) {
        this.client = client;
        this.socket = socket;

        thread = new Thread(this);
        start();

    }

    public void start() {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            in = new BufferedReader(inputStreamReader);
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }
        running = true;
        thread.start();
    }

    public void kill() {
        running = false;

        try {
            in.close();
            socket.close();
        } catch (IOException ioException) {
            System.err.println("IOException generated: " + ioException);
            ioException.printStackTrace();
        }

        System.exit(1);
    }


    @Override
    public void run() {

        while (running) {
            try {
                String response = in.readLine();
                System.out.println("\n" + response);
                if (client.getUser() != null) {
                    System.out.print(client.getUser() + ": ");
                }
                Thread.sleep(100);

            } catch (IOException ioException) {
                System.err.println("IO error while reading: " + ioException.getMessage());
                kill();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
