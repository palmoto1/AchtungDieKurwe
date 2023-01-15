import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient implements Runnable {

    private static final int DEFAULT_PORT = 2001;
    private static final String DEFAULT_HOST = "127.0.0.1";

    private final String host;
    private final int port;

    private Socket socket;

    private BufferedReader in;
    private PrintWriter out;


    private Chat chat;

    private String user;

    private boolean running;


    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
        running = false;
    }

    public ChatClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ChatClient(String host) {
        this(host, DEFAULT_PORT);
    }


    /**
     * Enables the client to receive input streams and to write on the output stream.
     * Starts the thread.
     */
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


    /**
     * Continuously listens for messages to read from the input stream.
     */
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
                }

            } catch (IOException ioException) {
                System.out.println(ioException.getMessage());
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

    /**
     * Writes text message to the server via the output stream
     * @param message the message to be sent
     */
    public void write(String message) {
        out.println(message);
        out.flush();
    }

    /**
     * Kills the thread, and closes sockets and streams
     */
    public void kill() {
        running = false;

        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage());
            ioException.printStackTrace();
        }


    }
}



