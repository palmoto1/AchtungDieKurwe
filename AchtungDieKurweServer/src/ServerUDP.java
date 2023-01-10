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
    private final Game game;

    public ServerUDP(int port) {
        this.port = port;
        thread = new Thread(this);
        game = new Game(this);

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
            game.checkGameStatus();

        }
    }

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

    public void send(byte[] data, InetAddress ipAddress, int port) {

        DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
        try {
            this.socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

