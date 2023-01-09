import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientUDP implements Runnable {

    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private final String host;
    private final int port;

    private DatagramSocket socket;
    private InetAddress address;

    private String userName;
    private Game game;
    private GUI gui;

    private boolean running;


    public ClientUDP(String host, int port) {
        this.host = host;
        this.port = port;
        userName = "August"; // should come from GUI
    }

    public ClientUDP() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ClientUDP(String host) {
        this(host, DEFAULT_PORT);
    }


    public void start() {
        try {
            this.socket = new DatagramSocket();
            this.address = InetAddress.getByName(host);

            game = new Game(this);
            gui = new GUI(game);

            String message = createMessage(MessageType.CONNECT, userName);
            sendData(message.getBytes(StandardCharsets.UTF_8));

            running = true;
            game.start();

            new Thread(this).start();

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    public void sendData(byte[] data) {
            DatagramPacket packet = new DatagramPacket(data, data.length, address, 2000);
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
            parsePacket(message, packet.getAddress(), packet.getPort());


        }
    }

    private void parsePacket(String message, InetAddress address, int port) {
        String[] tokens = message.split(",");
        String type = tokens[0];
        String player = tokens[2];
        switch (type) {
            case MessageType.CONNECT:
                System.out.println("[" + player + " " + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");
                int id = Integer.parseInt(tokens[1]);
                gui.updatePlayerLabel(player, id, 0);

                break;
            case MessageType.DISCONNECT:
                System.out.println("[" + player + " " + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");

                break;
            case MessageType.READY:
                System.out.println("[" + player + " " + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                break;
            case MessageType.MOVE:
                String coordinate = tokens[1];
                game.addCoordinate(coordinate);
                break;
            case MessageType.RESTART:
                game.clearCoordinates();
                break;
            case MessageType.SCORE_UPDATE:
                id = Integer.parseInt(tokens[1]);
                int score = Integer.parseInt(tokens[3]);
                gui.updatePlayerLabel(player, id, score);
                break;
        }
    }

    //move to message handler
    public String createMessage(String type, String content, String userName){
        return type + "," + content + "," + userName;
    }
    //move to message handler
    public String createMessage(String type, String userName){
        return createMessage(type, "", userName);
    }

    public void kill() {
        running = false;
        socket.close();
    }

    public String getUserName() {
        return userName;
    }
}