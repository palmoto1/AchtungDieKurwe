import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ClientHandler implements Runnable {


    private final Thread thread;
    private final Socket socket;
    private final ChatServer server;
    private String name;
    private long id;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        thread = new Thread(this);
        id = -1;
    }

    /**
     * Connects the ClientHandler with the client and starts the thread
     */
    public void start() {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            in = new BufferedReader(inputStreamReader);

            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1);
            out = new PrintWriter(outputStreamWriter, true);

            name = in.readLine();
            System.out.println("The user " + name + " connected to chat server");
        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
        } catch (IOException ioException) {
            System.err.println(ioException.getMessage());
            ioException.printStackTrace();
        }
        thread.start();
    }

    /**
     * Listens for messages from the connected client and broadcasts them to all other clients.
     * If the client disconnects the ClientHandler stops running and is disposed.
     */
    @Override
    public void run() {
        try {
            String clientMessage = "";

            while (clientMessage != null) {
                clientMessage = in.readLine();
                server.addMessage(name + ": " + clientMessage);
                try {
                Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException ioException) {
            System.out.println("User connection lost!");
        }
        dispose();

    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * Writes a message to the client via the output stream
     * @param message the message to be sent
     */
    public void printMessage(String message) {
        out.println(message);
    }

    /**
     * Closes streams and socket.
     * Removes the ClientHandler from the server.
     */
    public void dispose() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            socket.close();
        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
        } catch (IOException ioException) {
            System.err.println("User error: " + ioException.getMessage());
            ioException.printStackTrace();
        }
        server.removeClient(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientHandler other)) {
            return false;
         }
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
