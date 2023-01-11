import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


//TODO: write comments, dela upp i två delar o anpassa för det, dela upp metoder mer t.ex. fixa loopar med sleep o kill
// testa att allt funkar bra

public class ServerTCP implements Runnable {


    private static final int DEFAULT_PORT = 2001;

    private final Thread thread;
    private final int port;

    private final ClientHandlerList clientHandlerList;

    private boolean running;


    public ServerTCP(int port) {
        this.port = port;
        thread = new Thread(this);
        clientHandlerList = new ClientHandlerList();
    }

    public ServerTCP() {
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


    public void broadcast(String msg, ClientHandler clientHandler) {
        clientHandlerList.broadcast(msg, clientHandler);
    }

    public void removeThread(ClientHandler clientHandler) {
        clientHandlerList.removeThread(clientHandler);
    }

    public void addThread(ClientHandler clientHandler) {

        clientHandlerList.addThread(clientHandler);
    }
}
