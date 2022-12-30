import java.util.Random;


//TODO: samla paths efter ID
public class Player implements Runnable {


    private static final int VISIBLE = 1;
    private static final int NOT_VISIBLE = 0;
    private static final int INTERVAL = 50;
    private static final int SPEED = 3;
    private static final int DIR_CHANGE = 5;

    private static final Random rnd = new Random();


    private final Thread thread;
    private final Client client;
    private Coordinate head;
    private double direction;

    private int intervalCounter;


    private boolean paused;
    private boolean active;


    public Player(Client client) {
        this.client = client;
        thread = new Thread(this);

    }

    public void start() {
        paused = false;
        active = true;

        direction = Math.random() * 360;

        double x = rnd.nextDouble(500);
        double y = rnd.nextDouble(500);
        head = new Coordinate(x, y, VISIBLE);

        intervalCounter = 0;

        thread.start();
    }

    public void pause() {
        paused = !paused;
    }

    public void setDirection(String command) {
        if (command.equals("left")) {
            direction -= DIR_CHANGE + 360;
        } else {
            direction += DIR_CHANGE;
        }
    }

    private void move() {
        direction %= 360;
        double x = SPEED * Math.cos(Math.toRadians(direction));
        double y = SPEED * Math.sin(Math.toRadians(direction));
        client.send(head.toString());

        int visible = checkCoordinateVisible();

        head = new Coordinate(head.getX() + x, head.getY() + y, visible);
    }



    @Override
    public void run() {
        System.out.println("Starting player!");
        while (active) {
            while (!paused) {
                move();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private int checkCoordinateVisible(){
        int visible = VISIBLE;

        if (intervalCounter > INTERVAL){
            visible = NOT_VISIBLE;
            if (intervalCounter > INTERVAL+2) {
                intervalCounter = 0;
            }
        }
        intervalCounter++;
        return visible;

    }

    public void exit() {
        active = false;
    }

    public boolean hasCollision(Coordinate coordinate) {
        return head.hasCollision(coordinate);
    }
}
