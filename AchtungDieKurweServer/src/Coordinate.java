public class Coordinate {


    private static final double SIZE = 0.5;
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

    @Override
    public String toString() {
        return x + ":" + y + ":" + visible + ":" + playerId;
    }



    public boolean hasCollision(Coordinate other) {
        if (other != null && other != this && other.isVisible()) {
            return x < other.getX() + SIZE &&
                    x + SIZE > other.getX() &&
                    y < other.getY() + SIZE &&
                    SIZE + y > other.getY();
        }
        return isOutsideWall();
    }

    private boolean isOutsideWall() {

        return x + SIZE > 600 || x < 0
                || y + SIZE > 800 || y < 0;
    }
}
