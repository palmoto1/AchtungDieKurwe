import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class ClientTCP implements Runnable {

    private static final int DEFAULT_PORT = 2001;
    private static final String DEFAULT_HOST = "127.0.0.1";

    private final Thread thread;

    private final String host;
    private final int port;

    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;


    private Chat chat;

    private String user;

    private boolean running;


    public ClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
        running = false;
        thread = new Thread(this);
    }

    public ClientTCP() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ClientTCP(String host) {
        this(host, DEFAULT_PORT);
    }


    public void start() {
        try {
            socket = new Socket(host, port);

            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            in = new BufferedReader(inputStreamReader);

            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    outputStream,
                    StandardCharsets.ISO_8859_1
            );
            out = new PrintWriter(outputStreamWriter, true);
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }
        running = true;
        new Thread(this).start();
    }


    public void run() {

        write(user);

        while (running) {
            try {
                String response = in.readLine();
                chat.append(response);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

            } catch (IOException ioException) {
                System.err.println("IO error while reading: " + ioException.getMessage());
                kill();
            }
        }


    }

    public void setChat(Chat gui) {
        this.chat = gui;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void write(String text) {
        out.println(text);
        out.flush();
    }

    public void kill() {
        running = false;

        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException ioException) {
            System.err.println("IOException generated: " + ioException);
            ioException.printStackTrace();
        }

        System.exit(1);
    }
}



