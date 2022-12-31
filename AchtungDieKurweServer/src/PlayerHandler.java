import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class PlayerHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private final int id;
    private final int colorId;
    private ObjectOutputStream out;

    public PlayerHandler(int id, int colorId, Socket socket, Server server) {
        this.id = id;
        this.colorId = colorId;
        this.socket = socket;
        this.server = server;
        Thread thread = new Thread(this);
        thread.start();
    }

    // dela upp
    @Override
    public void run() {
        try {

            InputStream inputStream = socket.getInputStream();
            ObjectInputStream in = new ObjectInputStream(inputStream);

            OutputStream outputStream = socket.getOutputStream();
            out = new ObjectOutputStream(outputStream);

            Player player = new Player(colorId);
            player.start();

            System.out.println("New user connected with ID: " + id);

            Object data = in.readObject();

            while (data != null) {

                player.setDirection((int)data);
                Coordinate coordinate = player.getHead();
                if (server.hasCollision(coordinate)){
                    //System.out.println("Collision");
                    player.pause();
                }
                server.addCoordinate(coordinate);
                server.broadcast(coordinate.toString());
                data = in.readObject();
                //Thread.sleep(50);
            }

            in.close();
            socket.close();


        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
            //socketException.printStackTrace();
        } catch (IOException ioException) {
            System.err.println("User error: " + ioException.getMessage());
            ioException.printStackTrace();
        /*} catch (InterruptedException e) {
            Thread.currentThread().interrupt();*/
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
            //e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }
}
