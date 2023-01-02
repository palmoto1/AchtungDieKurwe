import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class PlayerHandler implements Runnable {


    private final Thread thread;
    private final Socket socket;
    private final Server server;
    private final int id;
    private final int colorId;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    Player player;
    private boolean ready;

    public PlayerHandler(int id, int colorId, Socket socket, Server server) {
        this.id = id;
        this.colorId = colorId;
        this.socket = socket;
        this.server = server;
        thread = new Thread(this);
        start();
    }

    private void start(){
        try {
            InputStream inputStream = socket.getInputStream();
            in = new ObjectInputStream(inputStream);

            OutputStream outputStream = socket.getOutputStream();
            out = new ObjectOutputStream(outputStream);

            ready = false;

            System.out.println("New user connected with ID: " + id);

            player = new Player(colorId);


            thread.start();
        } catch (IOException ioException) {
            System.err.println("Error when initializing socket streams: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    // dela upp
    @Override
    public void run() {
        try {

            addCoordinate(player.getHead()); //write start point

            waitForPlayersToJoin();

            System.out.println("All players ready! Activating players!");
            player.start();

            Object data = in.readObject();

            while (data != null) {

                player.setDirection((int)data);
                addCoordinate(player.getHead());
                data = in.readObject();
                //Thread.sleep(50);
            }

            in.close();
            socket.close();


        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
            //socketException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("User error: " + ioException.getMessage());
            ioException.printStackTrace();
       // } catch (InterruptedException e) {
      //      Thread.currentThread().interrupt();
        } catch (ClassNotFoundException e) {
            System.err.println("Data of class not found: " + e.getMessage());
            e.printStackTrace();
        }

        server.removeThread(this);
        System.out.println("User with ID: " + id + " has quit!");

    }

    private void waitForPlayersToJoin() {

        try {
            System.out.println("Waiting for more players!");
            while (server.getNumberOfPlayers() != 2) {
                Thread.sleep(25);
            }

            server.clearCoordinates();
            addCoordinate(player.getHead());

            Object data;
            while (!server.allPlayersReady()) {
                if (!ready) {
                    data = in.readObject();
                    if ((int) data == 10) {
                        System.out.println("User with ID: " + id + " is ready!");
                        ready = true;
                    }
                }
            }
        } catch (InterruptedException | IOException | ClassNotFoundException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void writeData(Object data) {
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException e) {
            System.err.println("IO exception when writing object data: " + e.getMessage());
            //e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public boolean isReady() {
        return ready;
    }

    private void addCoordinate(Coordinate coordinate){
        if (server.hasCollision(coordinate)){
            //System.out.println("Collision");
            player.pause();
        }
        server.addCoordinate(coordinate);
        server.broadcast(coordinate.toString());
    }
}
