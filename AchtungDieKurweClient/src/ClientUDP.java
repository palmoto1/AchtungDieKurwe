import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

//TODO: fixa tokens så det blir mer konsekvent

public class ClientUDP implements Runnable {

    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private final String host;
    private final int port;
    private DatagramSocket socket;
    private InetAddress address;
    private Game game;

    private boolean running;


    public ClientUDP(String host) {
        this(host, DEFAULT_PORT);

    }

    public ClientUDP() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ClientUDP(String host, int port) {
        this.host = host;
        this.port = port;


    }

    public void setGame(Game gamePanel){
        this.game = gamePanel;
    }


    public void start() {
        try {
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName(host);

            running = true;

            new Thread(this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    public void send(byte[] data) {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    @Override
    public void run() {

        System.out.println("Client is listening");

        while (running) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String message = new String(packet.getData(), 0, packet.getLength());
            parse(message, packet.getAddress(), packet.getPort());


        }
    }

    private void parse(String message, InetAddress address, int port) {
        String[] tokens = message.split(",");
        String type = tokens[0];
        switch (type) {
            case MessageType.CONNECT:
                String player = tokens[2];
                System.out.println("[" + player + " " + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");
                int id = Integer.parseInt(tokens[1]);
                game.handleNewPlayer(player, id);
                break;
            case MessageType.CONNECTION_OK:
                game.start();
                break;
            case MessageType.CONNECTION_DENIED:
                String error = tokens[1];
                System.out.println("[" + address.getHostAddress() + ":" + port + "] "
                        + "got rejected: " + error);
                game.displayError(error);
                break;
            case MessageType.DISCONNECT:
                player = tokens[2];
                System.out.println("[" + player + " " + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");
                id = Integer.parseInt(tokens[1]);
                game.handleDisconnectedPlayer(player, id);
                break;
            case MessageType.READY:
                player = tokens[1];
                System.out.println("[" + player + " " + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                game.handleReadyPlayer(player);
                break;
            case MessageType.MOVE:
                String coordinate = tokens[1];
                game.addCoordinate(coordinate);
                break;
            case MessageType.RESTART:
                game.clearCoordinates();
                break;
            case MessageType.SCORE_UPDATE:
                player = tokens[2];
                id = Integer.parseInt(tokens[1]);
                int score = Integer.parseInt(tokens[3]);
                game.updateScore(player, id, score);
                break;
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void kill() {
        //running = false;
        //socket.close();
    }
}