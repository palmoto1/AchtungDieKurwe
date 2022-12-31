import java.io.*;
import java.net.Socket;
import java.net.SocketException;


public class PlayerHandler implements Runnable {

    private static int NextID = 0;

    private Player player;

    private final Thread thread;
    private final Socket socket;
    private final Server server;
    private int id;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public PlayerHandler(Socket socket, Server server) {
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

            player = new Player(this, id);
            player.start();

            System.out.println("New user connected with ID: " + id);
            //writeData(id);

            Object data = in.readObject(); // fastnar

            while (data != null) {
                // hämta vilket input som getts från client
                // ändra direction hos spelaren
                // hämta cordinat från spelaren
                // kolla kollision och lägg till kordinat (pausa spelaren om kollision)
                // skicka till clienten kordinat så den kan ritas
                //
                player.setDirection((String) data);
                //player.update();
                Coordinate coordinate = player.getHead();
                if (server.hasCollision(coordinate)){
                    //player.pause(); // pausa spelaren, gör snyggare
                    System.out.println("Collision");
                }
                server.addCoordinate(coordinate);
                server.broadcast(coordinate.toString());
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
            //e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public Coordinate decodeCoordinate (String data){
        String[] tokenizedData = data.split(" ");
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        return new Coordinate(x, y, visible);
    }
}
