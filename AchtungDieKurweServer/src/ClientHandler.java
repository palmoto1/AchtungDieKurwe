import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class ClientHandler implements Runnable {

    private static int NextID = 0;

    private final Thread thread;
    private final Socket socket;
    private final Server server;
    private int id;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        thread = new Thread(this);
        thread.start();
    }

    // dela upp
    @Override
    public void run() {
        try {
            id = NextID++;

            InputStream inputStream = socket.getInputStream();
            in = new ObjectInputStream(inputStream);

            OutputStream outputStream = socket.getOutputStream();
            out = new ObjectOutputStream(outputStream);

            System.out.println("New user connected with ID: " + id);
            server.broadcast(id);

            Object data = in.readObject();

            while (data != null) {
                server.broadcast(data + " " + id);
                data = in.readObject();
                Thread.sleep(100);
            }

            in.close();
            socket.close();


        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
            //socketException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("User error: " + ioException.getMessage());
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ClassNotFoundException e) {
            System.err.println("Data of class not found: " + e.getMessage());
            e.printStackTrace();
        }

        server.removeThread(this);
        System.out.println("User with ID: " + id + " has quit!");

    }


    public void writeData(Object data) {
        try {
            out.writeObject(data);
            out.flush();
        } catch (IOException e) {
            System.err.println("IO exception when writing object data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
}
