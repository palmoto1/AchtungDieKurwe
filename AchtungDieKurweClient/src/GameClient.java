import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class GameClient implements Runnable {

    private static final int DEFAULT_PORT = 8000 ;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private final String host;
    private final int port;
    private final BlockingQueue<Packet> packetQueue;
    private DatagramSocket socket;
    private InetAddress address;
    private Game game;

    private volatile boolean running;


    public GameClient(String host) {
        this(host, DEFAULT_PORT);

    }

    public GameClient() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;

        packetQueue = new LinkedBlockingQueue<>();


    }

    public void setGame(Game gamePanel){
        this.game = gamePanel;
    }


    /**
     * Set up and starting thread
     */
    public void start() {
        try {
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName(host);

            running = true;

            new Thread(this).start();

            new Thread(() -> {
                while (running){
                    try {
                        Packet packet = packetQueue.take();
                        parse(packet);
                    } catch (InterruptedException e){
                        System.out.println(e.getMessage());
                    }
                }
            }).start();


        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }


    /**
     * Sends a packet via the socket
     * @param data the content of the packet as a byte array
     */
    public void send(byte[] data) {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

    }


    /**
     * Continuously listens for packets from the server
     */
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
            try {
                packetQueue.put(new Packet(message, packet.getAddress(), packet.getPort()));
            } catch (InterruptedException e){
                System.out.println(e.getMessage());
            }


        }
    }

    /**
     * Inner class representing a packet received from the server
     */
    private static class Packet{

        String message;
        InetAddress address;
        int port;

        public Packet(String message, InetAddress address, int port) {
            this.message = message;
            this.address = address;
            this.port = port;
        }
    }


    /**
     * Parses a packet and handles according to the message type of the packet.
     *
     * @param packet The packet to be parsed
     */
    private void parse(Packet packet) {
        String[] tokens = packet.message.split(",");
        String type = tokens[0];
        switch (type) {
            case MessageType.CONNECT:
                String player = tokens[2];
                System.out.println("[" + player + " " + packet.address.getHostAddress() + ":" + packet.port + "] "
                        + " has connected...");
                int id = Integer.parseInt(tokens[1]);
                game.handleNewPlayer(player, id);
                break;
            case MessageType.CONNECTION_OK:
                game.start();
                break;
            case MessageType.CONNECTION_DENIED:
                String error = tokens[1];
                System.out.println("[" + packet.address.getHostAddress() + ":" + packet.port + "] "
                        + "got rejected: " + error);
                game.displayError(error);
                break;
            case MessageType.DISCONNECT:
                player = tokens[2];
                System.out.println("[" + player + " " + packet.address.getHostAddress() + ":" + packet.port + "] "
                        + " has disconnected...");
                id = Integer.parseInt(tokens[1]);
                game.handleDisconnectedPlayer(player, id);
                break;
            case MessageType.READY:
                player = tokens[1];
                System.out.println("[" + player + " " + packet.address.getHostAddress() + ":" + packet.port + "] "
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

}