import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Server handling the game state and game logic via UDP-protocol
 */
public class GameServer implements Runnable {


    private static final int DEFAULT_PORT = 8000;

    private final Thread thread;
    private DatagramSocket socket;
    private final int port;
    private volatile boolean running;
    private final BlockingQueue<Packet> packetQueue;

    private final Game game;

    public GameServer(int port) {
        this.port = port;
        thread = new Thread(this);
        game = new Game(this);
        packetQueue = new LinkedBlockingQueue<>();

    }

    public GameServer() {
        this(DEFAULT_PORT);
    }


    /**
     * Creates a socket for the port and starts the thread.
     */
    public void start() {
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        running = true;
        thread.start();

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

        System.out.println("Game server started on port " + port);
    }


    /**
     * Continuously listens for packets to receive.
     * Parses the message packet and checks the game status
     */
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
            try {
                packetQueue.put(new Packet(message, packet.getAddress(), packet.getPort()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        socket.close();

    }

    /**
     * Inner class representing a packet received from a client
     */
    private static class Packet {

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
     * Parses a message and handles according to the message type of the packet.
     *
     * @param packet the packet to be parsed
     */
    private void parse(Packet packet) {
        String[] tokens = packet.message.split(",");
        String type = tokens[0];
        switch (type) {
            case MessageType.CONNECT:
                String name = tokens[1];
                System.out.println("[User: " + name + ":" + packet.address.getHostAddress() + ":" + packet.port + "] "
                        + " has connected...");
                game.addPlayer(packet.address, packet.port, name);
                break;
            case MessageType.DISCONNECT:
                name = tokens[1];
                System.out.println("[User: " + name + ":" + packet.address.getHostAddress() + ":" + packet.port + "] "
                        + " has disconnected...");
                game.removePlayer(name);
                break;
            case MessageType.READY:
                name = tokens[1];
                System.out.println("[User: " + name + ":" + packet.address.getHostAddress() + ":" + packet.port + "] "
                        + " is ready...");
                game.handleReadyPlayer(name);
                break;
            case MessageType.MOVE:
                name = tokens[2];
                String direction = tokens[1];
                game.handleMove(name, direction);
                break;
        }
        // check if the message affected the game status
        game.checkGameStatus();
    }

    /**
     * Sends a packet
     * @param data the data to be sent as a byte array
     * @param ipAddress the host address of where the packet should be sent
     * @param port the port of where the packet should be sent
     */
    public synchronized void send(byte[] data, InetAddress ipAddress, int port) {

        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

