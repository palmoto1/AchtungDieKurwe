import java.net.InetAddress;

public class Player {

    private final InetAddress address;
    private final int port;
    private final String name;
    private final int id;
    private Snake snake;
    private boolean ready;
    private boolean active;
    private int score;

    public Player(InetAddress address, int port, String name, int id) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.id = id;

        score = 0;

        initialize();
    }

    public void initialize() {
        ready = false;
        active = false;

        snake = new Snake(id);
    }




    public void move(String data) {
        int command = Integer.parseInt(data);
        snake.setDirection(command);
        snake.update();
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Coordinate getHead() {
        return snake.getHead();
    }

    public boolean isReady() {
        return ready;
    }

    public boolean isActive() {
        return active;
    }

    public void activate(){
        active = true;
    }
    public void deactivate(){
        active = false;
    }

    public void setReady(){
        ready = true;
    }

    public int getScore() {
        return score;
    }

    public void increaseScore(){
        score++;
    }


}
