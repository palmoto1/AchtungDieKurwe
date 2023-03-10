import java.util.Random;

public class Snake {

    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;
    private static final int VISIBLE = 1;
    private static final int NOT_VISIBLE = 0;
    private static final int INTERVAL = 100;
    private static final int DIR_CHANGE = 2;

    private static final Random RANDOM = new Random();

    private final int playerId;

    private Coordinate head;
    private double direction;
    private int intervalCounter;

    public Snake(int playerId){
        this.playerId = playerId;
        direction = Math.random() * 360;

        double x = RANDOM.nextDouble(500);
        double y = RANDOM.nextDouble(500);
        head = new Coordinate(x, y, VISIBLE, playerId);

        intervalCounter = 0;
    }

    /**
     * Sets the direction of the snake based on direction commands
     * @param command the direction command
     */
    public void setDirection(int command) {
        if (command == TURN_LEFT) {
            direction -= DIR_CHANGE + 360;
        } else if (command == TURN_RIGHT) {
            direction += DIR_CHANGE;
        }
    }

    /**
     * Updates the snake so that it moves forward point by point.
     * Makes so that holes appears in the snake every time the interval count is reached
     */
    public void move() {
        direction %= 360;
        double x = Math.cos(Math.toRadians(direction));
        double y = Math.sin(Math.toRadians(direction));

        int visible = VISIBLE;

        if (intervalCounter > INTERVAL) {
            visible = NOT_VISIBLE;
            if (intervalCounter > INTERVAL + 10) {
                intervalCounter = 0;
            }
        }
        intervalCounter++;

        head = new Coordinate(
                head.getX() + x,
                head.getY() + y,
                visible,
                playerId
        );

    }
    public Coordinate getHead() {
        return head;
    }
}
