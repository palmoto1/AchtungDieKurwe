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
    private DatagramSocket socket;
    private final int port;

    private boolean running;
    private final HashMap<String, PlayerHandlerUDP> players;
    private final ArrayList<Coordinate> coordinates;

    public ServerUDP(int port) {
        this.port = port;
        thread = new Thread(this);
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
        running = true;
        thread.start();
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
            case MessageType.CONNECT:
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");
                addPlayer(new PlayerHandlerUDP(this, address, port, name, NextColorID++));
                //should refresh so starting points are shown to all players
                break;
            case MessageType.DISCONNECT:
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");
                removePlayer(name);
                break;
            case MessageType.READY:
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                player = findPlayer(name);
                player.setReady();
                toSend = MessageType.READY + ", ," + name;
                sendToAllButOne(toSend.getBytes(StandardCharsets.UTF_8), player);
                if (allPlayersReady()){ // och om antalet min-spelare är nått
                    activatePlayers();
                }
                break;
            case MessageType.MOVE:
                player = findPlayer(name);
                if (player != null && player.isActive()) {
                    String direction = tokens[1];
                    player.move(direction);
                    Coordinate coordinate = player.getHead();
                    coordinates.add(coordinate);
                    toSend = MessageType.MOVE + "," + coordinate.toString() + "," + name;
                    sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
                    if (hasCollision(coordinate)){
                        player.deactivate();
                    }
                }
                break;
        }
    }



    private synchronized void activatePlayers() {
        for (Map.Entry<String, PlayerHandlerUDP> set : players.entrySet()) {
            PlayerHandlerUDP player = set.getValue();
            player.activate();
        }
    }

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

