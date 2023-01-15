import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Server handling the chat via TCP-protocol
 */

public class ChatServer implements Runnable {


    private static final int DEFAULT_PORT = 2001;

    private final Thread thread;
    private final int port;

    private final ClientHandlerList clientHandlerList;

    private boolean running;


    public ChatServer(int port) {
        this.port = port;
        thread = new Thread(this);
        clientHandlerList = new ClientHandlerList();
    }

    public ChatServer() {
        this(DEFAULT_PORT);
    }


    /**
     * Starts the thread
     */
    public void start() {
        thread.start();
        running = true;
    }


    /**
     * Listens for a new client to connect to the server.
     * Creates and adds a new ClientHandler for the client
     */
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Chat server started on port " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                addClient(new ClientHandler(socket, this));
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

        } catch (IOException ioException) {
            System.err.println("Server error: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }


    /**
     * Broadcasts a message to all clients
     * @param message the message to be sent
     */
    public void broadcast(String message) {
        clientHandlerList.sendToAll(message);
    }

    public void removeClient(ClientHandler clientHandler) {
        clientHandlerList.remove(clientHandler);
    }

    public void addClient(ClientHandler clientHandler) {

        clientHandlerList.add(clientHandler);
    }
}
