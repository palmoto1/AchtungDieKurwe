import java.io.*;
import java.net.Socket;
import java.net.SocketException;

//TODO: fixa GUIn så man ser om spelare är ready osv
// och bestäm hur du vill ha spelflödet rent generellt (inspo, kansk en separat chatt)
// samt testa att det funkar korrekt över flera datorer (gäller alla uppgifter)
// förök fixa udp enligt https://github.com/vanZeben/2D-Game-Engine/blob/master/src/ca/vanzeben/game/net/GameServer.java
// och kör chatten med egen server som tcp. Testa om det funkar (UDP verkar ej behöva trådade spelare?)
// kör vidare med bara TCP annars med lite mer gui och nöj dig (viktiga är att det funkar över flera datorer)

public class PlayerHandler implements Runnable {

    private static final int READY = 10;


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

        initialize();
    }

    private void initialize(){
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

    public void start(){
        thread.start();
    }

    // dela upp
    @Override
    public void run() {
        try {

            waitForPlayers();

            player.start();

            Object data = in.readObject();
            while (data != null) {

                player.setDirection((int)data);
                //player.update();
                addCoordinate(player.getHead());
                data = in.readObject();
            }

            in.close();
            out.close();
            socket.close();


        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
            //socketException.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
            e.printStackTrace();
        }

        server.removeThread(this);

    }

    private void waitForPlayers() {

        try {
            // should be sent
            System.out.println("Waiting for more players to connect!");
            addCoordinate(player.getHead()); //write start point

            while (server.getNumberOfPlayers() < 2) {
                in.readObject();
                Thread.sleep(25);
            }
            //System.out.println(server.getNumberOfPlayers());

            addCoordinate(player.getHead()); //refresh start point so that it is visible to newly connected players

            while (!server.allPlayersReady()) {
                if (!ready) {
                    if ((int)in.readObject() == READY) {
                        server.broadcast(id); // broadcast id to all connected players
                        // should be sent
                        System.out.println("User with ID: " + id + " is ready!");
                        ready = true;
                    }
                }
            }

            //should be sent
            System.out.println("All players ready! Activating players!");

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
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
        if (server.hasCollision(coordinate) && player.active()){
            System.out.println("Collision");
            player.stop();
        }
        server.addCoordinate(coordinate);
        server.broadcast(coordinate.toString());
    }
}
