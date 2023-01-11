import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;


public class ClientWriter implements Runnable {


    private static final String END_COMMAND = "exit";


    private final Thread thread;
    private final ClientTCP client;
    private final Socket socket;
    private PrintWriter out;

    private boolean running;

    public ClientWriter(ClientTCP client, Socket socket) {
        this.thread = new Thread(this);
        this.client = client;
        this.socket = socket;
        this.running = true;
        this.start();

    }

    public void start() {

        try {
            System.out.println("Connected to chat server");

            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    outputStream,
                    StandardCharsets.ISO_8859_1
            );
            out = new PrintWriter(outputStreamWriter, true);

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }

        thread.start();
    }

    public void kill() {
        running = false;

        try {
            out.close();
            socket.close();
        } catch (IOException ioException) {
            System.err.println("IOException generated: " + ioException);
            ioException.printStackTrace();
        }

        System.exit(1);
    }

    @Override
    public void run() {


        Console console = System.console();

        String userName = console.readLine("\nEnter name: ");
        client.setUser(userName);
        out.println(userName);

        while (running) {
            try {
                String text = console.readLine(userName + ": ");
                out.println(text);

                if (text.equalsIgnoreCase(END_COMMAND)) {
                    kill();
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

        }
    }
}

