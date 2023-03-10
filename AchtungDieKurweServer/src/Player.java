import java.net.InetAddress;

public class Player {

    private final InetAddress address;
    private final int port;
    private final String name;
    private final int id;
    private Snake snake;
    private int score;
    private boolean ready;
    private boolean active;


    public Player(InetAddress address, int port, String name, int id) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.id = id;
        score = 0;
        initialize();
    }

    /**
     * Initializes the player and creates a new snake
     */
    public void initialize() {
        ready = false;
        active = false;
        snake = new Snake(id);
    }

    /**
     * Makes a call to update snake position and move the snake
     * @param data data containing the command
     */
    public void move(String data) {
        int command = Integer.parseInt(data);
        snake.setDirection(command);
        snake.move();
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

    public Snake getSnake() {
        return snake;
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
