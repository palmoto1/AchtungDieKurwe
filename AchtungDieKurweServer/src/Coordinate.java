public class Coordinate {

    /**
     * Representing a coordinate making up the snakes body
     */


    private static final double SIZE = 0.5;
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 800;
    private final double x;
    private final double y;
    private final int visible;
    private final int playerId;

    public Coordinate(double x, double y, int visible, int playerId) {
        this.x = x;
        this.y = y;
        this.visible = visible;
        this.playerId = playerId;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public boolean isVisible() {
        return visible != 0;
    }


    /**
     * Checks if the coordinate has collided with another coordinate or the borders of the game
     * @param other the other coordinate
     * @return true or false
     */
    public boolean hasCollision(Coordinate other) {
        if (other != null && other != this && other.isVisible()) {
            return x < other.getX() + SIZE &&
                    x + SIZE > other.getX() &&
                    y < other.getY() + SIZE &&
                    SIZE + y > other.getY();
        }
        return isOutsideWall();
    }


    /**
     * Checks if the coordinate is outside the boundary of the game
     * @return true or false
     */
    private boolean isOutsideWall() {
        return x + SIZE > WINDOW_WIDTH || x < 0
                || y + SIZE > WINDOW_HEIGHT || y < 0;
    }

    @Override
    public String toString() {
        return x + ":" + y + ":" + visible + ":" + playerId;
    }
}
