import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

//split up into more classes - message handler, player handler, etc

public class ServerUDP implements Runnable {


    private static final int DEFAULT_PORT = 2000;

    private final Thread thread;
    private DatagramSocket socket;
    private final int port;
    private boolean running;
    private final GameHandler gameHandler;

    public ServerUDP(int port) {
        this.port = port;
        thread = new Thread(this);
        gameHandler = new GameHandler(this);

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
            parse(message, packet.getAddress(), packet.getPort());

        }
    }

    private void parse(String message, InetAddress address, int port) {
        String[] tokens = message.split(",");
        String type = tokens[0];
        String name = tokens[2];
        switch (type) {
            case MessageType.CONNECT:
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has connected...");
                gameHandler.addPlayer(address, port, name);
                break;
            case MessageType.DISCONNECT:
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " has disconnected...");
                gameHandler.removePlayer(name);
                break;
            case MessageType.READY:
                System.out.println("[User: " + name + ":" + address.getHostAddress() + ":" + port + "] "
                        + " is ready...");
                gameHandler.handleReadyUser(name);
                break;
            case MessageType.MOVE:
                String direction = tokens[1];
                gameHandler.handleMove(name, direction);
                break;
        }
    }

    public void send(byte[] data, InetAddress ipAddress, int port) {

        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

