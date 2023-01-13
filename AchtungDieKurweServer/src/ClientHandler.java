import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class ClientHandler implements Runnable {


    private final Thread thread;
    private final Socket socket;
    private final ServerTCP server;
    private String name;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, ServerTCP server) {
        this.socket = socket;
        this.server = server;
        thread = new Thread(this);
        thread.start();
    }

    // dela upp
    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            in = new BufferedReader(inputStreamReader);

            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1);
            out = new PrintWriter(outputStreamWriter, true);
            name = in.readLine();

            String clientMessage = "";

            while (clientMessage != null) {
                clientMessage = in.readLine();
                server.broadcast(name + ": " + clientMessage, this);
                Thread.sleep(100);
            }

            out.close();
            in.close();
            socket.close();


        } catch (SocketException socketException) {
            System.out.println("User connection lost!");
        } catch (IOException ioException) {
            System.err.println("User error: " + ioException.getMessage());
            ioException.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        server.removeThread(this);

    }

    public String getName() {
        return name;
    }

    public void printMessage(String msg) {
        out.println(msg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientHandler other)) {
            return false;
         }
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
