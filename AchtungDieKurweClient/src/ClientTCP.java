import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTCP {

    private static final int DEFAULT_PORT = 2001;
    private static final String DEFAULT_HOST = "127.0.0.1";

    private final String host;
    private final int port;

    private String user;


    public ClientTCP(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public ClientTCP() {
        this(DEFAULT_HOST, DEFAULT_PORT);
    }

    public ClientTCP(String host) {
        this(host, DEFAULT_PORT);
    }

    public static void main(String[] args) {

        ClientTCP clientTCP;

        if (args.length >= 2) {
            clientTCP = new ClientTCP(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 1) {
            clientTCP = new ClientTCP(args[0]);
        } else {
            clientTCP = new ClientTCP();
        }

        clientTCP.run();
    }




    public void run() {

        try {
            Socket socket = new Socket(host, port);

            new ClientReader(this, socket);
            new ClientWriter(this, socket);


        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O Error: " + ex.getMessage());
        }


    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}



