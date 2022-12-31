public class Coordinate {


    private static final double COLLISION_SIZE = 1.5;
    private final double x;
    private final double y;
    private final int visible;

    public Coordinate(double x, double y, int visible) {
        this.x = x;
        this.y = y;
        this.visible = visible;
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
        return x + " " + y + " " + visible;
    }


    //funkar ej perfekt
    public boolean hasCollision(Coordinate other) {
        if (other != this && other.isVisible()) {
            return x < other.getX() + COLLISION_SIZE &&
                    x + COLLISION_SIZE > other.getX() &&
                    y < other.getY() + COLLISION_SIZE &&
                    COLLISION_SIZE + y > other.getY();
        }
        return false;
    }
}
