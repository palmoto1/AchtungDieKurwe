import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


//TODO: samla paths efter ID
public class Player implements Runnable {


    private static final int TRUE = 1;
    private static final int FALSE = 0;
    private static final int INTERVAL = 50;
    private static final int SIZE = 3;
    private static final int SPEED = 3;
    private static final int DIR_CHANGE = 5;

    private static final Random rnd = new Random();


    private final Thread thread;
    private final Client client;
    private Coordinate head;
    private final HashMap<Integer, ArrayList<Coordinate>> paths;
    private double direction;

    private int intervalCounter;


    private boolean paused;
    private boolean active;


    public Player(Client client) {
        this.client = client;
        thread = new Thread(this);
        paths = new HashMap<>();

    }

    public void start() {
        paused = false;
        active = true;
        intervalCounter = 0;
        setNewPlayer();
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

    public void newGame() {
        setNewPlayer();

    }

    private void setNewPlayer() {
        direction = Math.random() * 360;
        double x = rnd.nextDouble(500);
        double y = rnd.nextDouble(500);
        head = new Coordinate(x, y, TRUE);
    }


    private void move() {
        direction %= 360;
        double x = SPEED * Math.cos(Math.toRadians(direction));
        double y = SPEED * Math.sin(Math.toRadians(direction));
        client.send(head.toString());

        int visible = checkCoordinateVisible();

        head = new Coordinate(head.getX() + x, head.getY() + y, visible);
    }


    public void paintComponent(Graphics g) {

        for (Map.Entry<Integer, ArrayList<Coordinate>> set : paths.entrySet()) {
            ArrayList<Coordinate> path = set.getValue();
            for (Coordinate c : path) {
                if(c.isVisible()) {
                    c.paint(g);
                }
            }
        }
    }

    public void add(String data) {
        String[] tokenizedData = data.split(" ");
        int id = Integer.parseInt(tokenizedData[3]);
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        Coordinate coordinate = new Coordinate(x, y, visible);
        if (!paths.containsKey(id)) {
            paths.put(id, new ArrayList<>());
        }
        paths.get(id).add(coordinate);
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
        int visible = TRUE;

        if (intervalCounter > INTERVAL){
            visible = FALSE;
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

    public void checkCollision() {
        for (Map.Entry<Integer, ArrayList<Coordinate>> set : paths.entrySet()) {
            ArrayList<Coordinate> path = set.getValue();
            for (Coordinate c : path) {
                if(head.hasCollision(c)) {
                    System.out.println("Collision!!!");
                    //pause();
                }
            }
        }
    }
}
