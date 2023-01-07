import java.net.InetAddress;
import java.util.Random;

//TODO: fixa GUIn så man ser om spelare är ready osv
// och bestäm hur du vill ha spelflödet rent generellt (inspo, kansk en separat chatt)
// samt testa att det funkar korrekt över flera datorer (gäller alla uppgifter)

public class Player {

    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;
    private static final int VISIBLE = 1;
    private static final int NOT_VISIBLE = 0;
    private static final int INTERVAL = 100;
    private static final double SPEED = 1;
    private static final int DIR_CHANGE = 2;

    private static final Random RANDOM = new Random();

    private final InetAddress address;
    private final int port;
    private final String name;

    private Coordinate head;
    private double direction;
    private int intervalCounter;
    private final int colorId;
    private boolean ready;
    private boolean active;

    public Player(InetAddress address, int port, String name, int colorId) {
        this.address = address;
        this.port = port;
        this.name = name;
        this.colorId = colorId;
        initialize();
    }

    private void initialize() {
        ready = false;
        active = false;
        direction = Math.random() * 360;
        double x = RANDOM.nextDouble(500);
        double y = RANDOM.nextDouble(500);
        head = new Coordinate(x, y, VISIBLE, colorId);
        intervalCounter = 0;
    }




    public void move(String data) {
        int command = Integer.parseInt(data);
        setDirection(command);
        update();
    }

    public Coordinate getHead() {
        return head;
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

    public void setDirection(int command) {
        //int command = Integer.parseInt(data);
        if (command == TURN_LEFT) {
            direction -= DIR_CHANGE + 360;
        } else if (command == TURN_RIGHT) {
            direction += DIR_CHANGE;
        }
    }

    private void update() {
        direction %= 360;
        double x = SPEED * Math.cos(Math.toRadians(direction));
        double y = SPEED * Math.sin(Math.toRadians(direction));

        int visible = VISIBLE;

        if (intervalCounter > INTERVAL) {
            visible = NOT_VISIBLE;
            if (intervalCounter > INTERVAL + 10) {
                intervalCounter = 0;
            }
        }
        intervalCounter++;

        head = new Coordinate(head.getX() + x, head.getY() + y, visible, colorId);

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
}
