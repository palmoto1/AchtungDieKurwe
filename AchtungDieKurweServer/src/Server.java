import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;


//TODO: write comments, dela upp i två delar o anpassa för det, dela upp metoder mer t.ex. fixa loopar med sleep o kill

public class Server implements Runnable {


    private static final int DEFAULT_PORT = 2000;

    private final Thread thread;
    private final int port;

    private boolean running;
    private ArrayBlockingQueue<ClientHandler> clientHandlers; //lägg i adapter klass
    private int capacity;

    public Server(int port) {
        this.thread = new Thread(this);
        this.port = port;
        this.capacity = 10;
        this.clientHandlers = new ArrayBlockingQueue<>(capacity);
    }

    public Server() {
        this(DEFAULT_PORT);
    }

    public void start() {
        thread.start();
        running = true;
    }

    public void kill() {
        running = false;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("Connecting new user!");
                addThread(new ClientHandler(socket, this));
                Thread.sleep(100);

            }

        } catch (IOException ioException) {
            System.err.println("Server error: " + ioException.getMessage());
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    public synchronized void broadcast(Object data) {
        for (ClientHandler ch : clientHandlers) {
            ch.writeData(data);
        }
    }

    public synchronized void removeThread(ClientHandler clientHandler) {
        if (clientHandlers.remove(clientHandler)) {
            System.out.println("Player with id: " + clientHandler.getId() + " quit");
        }
    }

    public synchronized void addThread(ClientHandler clientHandler) {

        if (clientHandlers.size() == capacity) {
            increaseCapacity();
        }
        clientHandlers.add(clientHandler);

    }

    private void increaseCapacity() {
        capacity *= 2;
        ArrayBlockingQueue<ClientHandler> copy = new ArrayBlockingQueue<>(capacity);
        copy.addAll(clientHandlers);
        clientHandlers = copy;
    }
}
