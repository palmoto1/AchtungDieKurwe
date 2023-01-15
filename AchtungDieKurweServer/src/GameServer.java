import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;


/**
 * Server handling the game state and game logic via UDP-protocol
 */
public class GameServer implements Runnable {


    private static final int DEFAULT_PORT = 2000;

    private final Thread thread;
    private DatagramSocket socket;
    private final int port;
    private boolean running;
    private final Game game;

    public GameServer(int port) {
        this.port = port;
        thread = new Thread(this);
        game = new Game(this);

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
            parse(message, packet.getAddress(), packet.getPort());
            game.checkGameStatus();
        }
    }

    /**
     * Parses a message and handles according to the message type of the packet.
     *
     * @param message the message to be parsed
     * @param address the host address from where the packet was sent
     * @param port the port from where the packet was sent
     */
    private void parse(String message, InetAddress address, int port) {
        String[] tokens = message.split(",");
        String type = tokens[0];
        switch (type) {
            case MessageType.CONNECT:
                String name = tokens[1];
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");
                game.addPlayer(address, port, name);
                break;
            case MessageType.DISCONNECT:
                name = tokens[1];
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");
                game.removePlayer(name);
                break;
            case MessageType.READY:
                name = tokens[1];
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                game.handleReadyPlayer(name);
                break;
            case MessageType.MOVE:
                name = tokens[2];
                String direction = tokens[1];
                game.handleMove(name, direction);
                break;
        }
    }

    /**
     * Sends a packet
     * @param data the data to be sent as a byte array
     * @param ipAddress the host address of where the packet should be sent
     * @param port the port of where the packet should be sent
     */
    public void send(byte[] data, InetAddress ipAddress, int port) {

        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

