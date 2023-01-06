import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ClientUDP implements Runnable {

    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private final String host;
    private final int port;

    private DatagramSocket socket;
    private InetAddress address;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private String name;
    private Game game;

    private boolean running;


    public ClientUDP(String host, int port) {
        this.host = host;
        this.port = port;
        name = "Olle";
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
            running = true;
            String connect = MessageType.CONNECT + ",connected," + name;
            sendData(connect.getBytes(StandardCharsets.UTF_8));
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
        String name = tokens[2];
        switch (type) {
            case MessageType.CONNECT:
                System.out.println("[" + name + " " + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");

                break;
            case MessageType.DISCONNECT:
                System.out.println("[" + name + " " + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");

                break;
            case MessageType.READY:
                System.out.println("[" + name + " " + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                break;
            case MessageType.MOVE: // move (updatera move och skicka kordinat samt addera den och kolla kollision)
                String coordinate = tokens[1];
                game.addCoordinate(coordinate);
                break;
        }
    }

    public void kill() {
        try {
            running = false;
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("I/O error when disconnecting from server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
}