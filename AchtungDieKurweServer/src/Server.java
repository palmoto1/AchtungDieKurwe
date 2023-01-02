import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


//TODO: byt till UDP för att se om det går snabbare, eller kör via clienten igen

public class Server implements Runnable {


    private static final int DEFAULT_PORT = 2000;

    private static int NextID = 0;
    private static int NextColorID = 0;

    private final Thread thread;
    private final int port;

    private boolean running;
    private ArrayBlockingQueue<PlayerHandler> players; //lägg i adapter klass
    private final ArrayList<Coordinate> coordinates; // egen klass?
    private int capacity;

    public Server(int port) {
        thread = new Thread(this);
        this.port = port;
        capacity = 10;
        players = new ArrayBlockingQueue<>(capacity);
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
                addThread(new PlayerHandler(NextID++, NextColorID++, socket, this));
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
        for (PlayerHandler ch : players) {
            ch.writeData(data);
        }
    }

    public synchronized void broadcastExcluding(Object data, PlayerHandler playerHandler) {
        for (PlayerHandler ch : players) {
            if (playerHandler.getId() != ch.getId()) {
                ch.writeData(data);
            }
        }
    }

    public synchronized void removeThread(PlayerHandler playerHandler) {
        if (players.remove(playerHandler)) {
            System.out.println("Player with id: " + playerHandler.getId() + " quit");
            NextColorID--;
        }
    }

    public synchronized void addThread(PlayerHandler playerHandler) {

        if (players.size() == capacity) {
            increaseCapacity();
        }
        players.add(playerHandler);

    }

    public boolean hasCollision (Coordinate coordinate) {
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

    public void clearCoordinates () {

        coordinates.clear();
    }

    public int getNumberOfPlayers(){
        return players.size();
    }

    public boolean allPlayersReady(){
        for (PlayerHandler player : players){
            if (!player.isReady()){
                return false;
            }
        }
        return !players.isEmpty();
    }

    private void increaseCapacity() {
        capacity *= 2;
        ArrayBlockingQueue<PlayerHandler> copy = new ArrayBlockingQueue<>(capacity);
        copy.addAll(players);
        players = copy;
    }
}
