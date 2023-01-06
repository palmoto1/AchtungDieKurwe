import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ServerUDP implements Runnable {


    private static final int DEFAULT_PORT = 2000;
    private static int NextColorID = 0;

    private final Thread thread;
    private final int port;

    private boolean running;
    private HashMap<String, PlayerHandlerUDP> players; //lägg i adapter klass
    private final ArrayList<Coordinate> coordinates; // egen klass?


    private DatagramSocket socket;

    public ServerUDP(int port) {
        thread = new Thread(this);
        this.port = port;
        players = new HashMap<>();
        coordinates = new ArrayList<>();

    }

    public ServerUDP() {
        this(DEFAULT_PORT);
    }

    public void start() {
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        thread.start();
        running = true;
        System.out.println("Server started on port " + port);
    }

    public void kill() {
        running = false;
    }

    @Override
    public void run() {

        while (running) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(packet.getData(), 0, packet.getLength());
            parseMessage(message, packet.getAddress(), packet.getPort());

        }
    }

    private void parseMessage(String message, InetAddress address, int port) {
        String[] tokens = message.split(",");
        String type = tokens[0];
        String name = tokens[2];
        PlayerHandlerUDP player;
        String toSend;
        switch (type) {
            case MessageType.CONNECT: // connect
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");
                addPlayer(new PlayerHandlerUDP(this, address, port, name, NextColorID++));
                break;
            case MessageType.DISCONNECT: // connect
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");
                removePlayer(name);
                break;
            case MessageType.READY: // ready (skicka till alla att han e redo) // starta spel om alla spelare e redo
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                player = findPlayer(name);
                toSend = MessageType.READY + ", ," + name;
                sendToAllButOne(toSend.getBytes(StandardCharsets.UTF_8), player);
                break;
            case MessageType.MOVE: // move (updatera move och skicka kordinat samt addera den och kolla kollision)
                //gör bara om redo!!
                player = findPlayer(name);
                player.move(tokens[1]);
                Coordinate coordinate = player.getHead();
                coordinates.add(coordinate);
                toSend = MessageType.MOVE + "," + coordinate.toString() + "," + name;
                sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
                break;
        }
    }



    /*private synchronized void startGame() {
        for (PlayerHandlerUDP ch : players) {
            ch.start();
        }
    }*/

    private PlayerHandlerUDP findPlayer(String name) {
        return players.get(name);
    }

    private void send(byte[] data, InetAddress ipAddress, int port) {

        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean hasPlayer(String name) {
        return findPlayer(name) != null;
    }


    private synchronized void sendToAll(byte[] data) {
        for (Map.Entry<String, PlayerHandlerUDP> set : players.entrySet()) {
            PlayerHandlerUDP player = set.getValue();
            send(data, player.getAddress(), player.getPort());
        }
    }

    private synchronized void sendToAllButOne(byte[] data, PlayerHandlerUDP excluded) {
        for (Map.Entry<String, PlayerHandlerUDP> set : players.entrySet()) {
            PlayerHandlerUDP player = set.getValue();
            if (!set.getKey().equals(excluded.getName())) {
                send(data, player.getAddress(), player.getPort());
            }
        }
    }

    private synchronized void removePlayer(String name) {
        if (hasPlayer(name)) {
            players.remove(name);
            if (players.isEmpty()) {
                coordinates.clear();
            }
            NextColorID--;
        }
    }

    private synchronized void addPlayer(PlayerHandlerUDP playerHandler) {
        if (!hasPlayer(playerHandler.getName())) {
            players.put(playerHandler.getName(), playerHandler);
        }

    }

    public boolean hasCollision(Coordinate coordinate) {
        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinate.hasCollision(coordinates.get(i))) {
                return true;
            }
        }
        return false;
    }

    public int getNumberOfPlayers() {
        return players.size();
    }

    public boolean allPlayersReady() {
        for (Map.Entry<String, PlayerHandlerUDP> set : players.entrySet()) {
            PlayerHandlerUDP player = set.getValue();
            if (!player.isReady()) {
                return false;
            }
        }
        return !players.isEmpty();
    }

}

