import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client extends JFrame implements Runnable {

    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";

    private static final int WIDTH = 500;

    private static final int HEIGHT = 500;

    private final String host;
    private final int port;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Game game;

    private boolean running;


    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Client() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public Client(String host) {
        this(host, DEFAULT_PORT);
    }


    public void start() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            game = new Game(this);
            running = true;
            loadWindow();

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

    public void send(Object packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            System.err.println("I/O error when sending data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        System.out.println("Client is listening");
        try {
            while (running) {
                try {
                    Object data = in.readObject();

                    game.addCoordinate((String) data);

                    //Thread.sleep(50);
                } catch (ClassNotFoundException e) {
                    System.err.println("Error when reading data: " + e.getMessage());
                    e.printStackTrace();
                } /*catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }*/

            }
        } catch (IOException e) {
            System.err.println("I/O error when reading data: " + e.getMessage());
            e.printStackTrace();


        }
    }


    public void loadWindow() {
        getContentPane().add(game);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);
        setVisible(true);
        game.setFocusable(true);
    }

    public void kill() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("I/O error when disconnecting from server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}