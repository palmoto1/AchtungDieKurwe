import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Server handling the chat via TCP-protocol
 */

public class ChatServer implements Runnable {


    private final static int DEFAULT_PORT = 8080;
    private final static int MAX_CLIENTS = 6;

    private final Thread thread;
    private final int port;
    private final ConcurrentHashMap<Long, ClientHandler> connectedClients;
    private final BlockingQueue<String> messageQueue;
    private volatile boolean running;


    public ChatServer(int port) {
        this.port = port;
        thread = new Thread(this);
        connectedClients = new ConcurrentHashMap<>(MAX_CLIENTS);
        messageQueue = new LinkedBlockingQueue<>();
    }

    public ChatServer() {
        this(DEFAULT_PORT);
    }


    /**
     * Starts the main thread and another thread consuming messages from the message queue
     */
    public void start() {
        thread.start();
        running = true;

        new Thread(() -> {
            while(running){
                try {
                    String message = messageQueue.take();
                    broadcast(message);
                }catch (InterruptedException e){
                    System.out.println(e.getMessage());
                    break;
                }
            }
        }).start();
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
                long id = System.currentTimeMillis();
                ClientHandler client = new ClientHandler(socket, this);
                if (addClient(id, client)){
                    client.start();
                }
                else {
                    client.dispose();
                }
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
     * Adds a message to the message queue
     * @param message the message to put in the queue
     */
    public void addMessage(String message){
        if (message != null) {
            try {
                messageQueue.put(message);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Broadcasts a message to all clients
     * @param message the message to be sent
     */
    private void broadcast(String message) {
        connectedClients.forEach(1, (id, client) -> client.printMessage(message));
    }


    /**
     * Removes a client from the server
     * @param id the client id
     */
    public void removeClient(long id) {
        ClientHandler removed = connectedClients.remove(id);
        if (removed != null){
            System.out.println("The user " + removed.getName() + " quit");

        }
    }

    /**
     * Adds a client as long as the server is not full
     * @param id the id of the client
     * @param clientHandler the client
     * @return true or false if the client was added or not
     */
    public boolean addClient(long id, ClientHandler clientHandler) {
        if (connectedClients.size() == MAX_CLIENTS){
            return false;
        }
        clientHandler.setId(id);
        connectedClients.putIfAbsent(id, clientHandler);
        return true;
    }
}
