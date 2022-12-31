import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


//TODO: write comments, dela upp i två delar o anpassa för det, dela upp metoder mer t.ex. fixa loopar med sleep o kill

public class Server implements Runnable {


    private static final int DEFAULT_PORT = 2000;

    private final Thread thread;
    private final int port;

    private boolean running;
    private ArrayBlockingQueue<PlayerHandler> playerHandlers; //lägg i adapter klass
    private ArrayList<Coordinate> coordinates;
    private int capacity;

    public Server(int port) {
        thread = new Thread(this);
        this.port = port;
        capacity = 10;
        playerHandlers = new ArrayBlockingQueue<>(capacity);
        coordinates = new ArrayList<>();

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
                addThread(new PlayerHandler(socket, this));
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
        for (PlayerHandler ch : playerHandlers) {
            ch.writeData(data);
        }
    }

    public synchronized void removeThread(PlayerHandler playerHandler) {
        if (playerHandlers.remove(playerHandler)) {
            System.out.println("Player with id: " + playerHandler.getId() + " quit");
        }
    }

    public synchronized void addThread(PlayerHandler playerHandler) {

        if (playerHandlers.size() == capacity) {
            increaseCapacity();
        }
        playerHandlers.add(playerHandler);

    }

    public boolean hasCollision (Coordinate coordinate) {
        /*for (Map.Entry<Integer, ArrayList<Coordinate>> set : paths.entrySet()) {
            ArrayList<Coordinate> path = set.getValue();
            for (int i = 1 ; i < path.size(); i++) {
                if(player.hasCollision(path.get(i))) {
                    player.pause();
                }
            }
        }*/
        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinate.hasCollision(coordinates.get(i))) {
                return true;
            }
        }
        return false;
    }

    public void addCoordinate (Coordinate coordinate) {

        coordinates.add(coordinate);
    }

    private void increaseCapacity() {
        capacity *= 2;
        ArrayBlockingQueue<PlayerHandler> copy = new ArrayBlockingQueue<>(capacity);
        copy.addAll(playerHandlers);
        playerHandlers = copy;
    }
}
