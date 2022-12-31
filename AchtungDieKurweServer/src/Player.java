import java.util.Random;


//TODO: samla paths efter ID
public class Player implements Runnable {


    //FLYTTA HELA TILL SERVER
    private static final int VISIBLE = 1;
    private static final int NOT_VISIBLE = 0;
    private static final int INTERVAL = 50;
    private static final int SPEED = 3;
    private static final int DIR_CHANGE = 5;

    private static final Random rnd = new Random();


    private final Thread thread;
    private final PlayerHandler playerHandler;

    private final int id;
    private Coordinate head;
    private double direction;
    private int intervalCounter;


    private boolean paused;
    private boolean active;


    public Player(PlayerHandler playerHandler, int id) {
        this.playerHandler = playerHandler;
        this.id = id;
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
        //paused = !paused;
        paused = true;
    }

    public void setDirection(String command) {
        if (command.equals("left")) {
            direction -= DIR_CHANGE + 360;
        } else if (command.equals("right")) {
            direction += DIR_CHANGE;
        }
    }

    public void update() {
        direction %= 360;
        double x = SPEED * Math.cos(Math.toRadians(direction));
        double y = SPEED * Math.sin(Math.toRadians(direction));
        //playerHandler.broadcast(head);

        int visible = checkCoordinateVisible();

        head = new Coordinate(head.getX() + x, head.getY() + y, visible);

    }

    public Coordinate getHead() {
        return head;
    }

    //@Override
    public void run() {
        System.out.println("Starting player!");
        while (active) {
            while (!paused) {
                update();
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

}
