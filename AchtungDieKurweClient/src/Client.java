import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

//TODO: förenkla till en klient klass istället

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

    private Player player;
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


    public void start(){
        try{
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            player = new Player(this);
            game = new Game(player);
            running = true;
            loadWindow();
            player.start();
            game.start();
            new Thread(this).start();

        }catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("IO exception while connecting to server: " + ioException.getMessage());
            ioException.printStackTrace();
        }
    }

    public void send(Object packet){
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
                    player.add((String)data);
                } catch (ClassNotFoundException e) {
                    System.err.println("Error when reading data: " + e.getMessage());
                    e.printStackTrace();
                }

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






//TODO: kan nog dela upp i Sender och Receiver som i 2.1

/*public class Client implements Runnable {
    private static final int DEFAULT_PORT = 2000;
    private static final String DEFAULT_HOST = "127.0.0.1";
    private final String host;
    private final int port;
    private final Thread thread;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean running;
    private String user;
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        thread = new Thread(this);
    }
    public Client() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }
    public Client(String host) {
        this(host, DEFAULT_PORT);
    }
    public void connect() {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (UnknownHostException ex) {
            System.err.println("Could not connect to server: " + ex.getMessage());
        } catch (IOException ex) {
            System.err.println("I/O error when connecting to server: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    public void start() {
        running = true;
        thread.start();
    }
    public void kill() {
        running = false;
    }
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("I/O error when disconnecting from server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void send(Object packet){
        try {
            out.writeObject(packet);
        } catch (IOException e) {
            System.err.println("I/O error when sending data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void run() {
        try {
            while (running) {
                try {
                    Object data = in.readObject();
                } catch (ClassNotFoundException e) {
                    System.err.println("Error when reading data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error when reading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
}*/
