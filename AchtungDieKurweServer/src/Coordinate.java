public class Coordinate {


    private static final double SIZE = 1.5;
    private final double x;
    private final double y;
    private final int visible;

    private final int colorId;

    public Coordinate(double x, double y, int visible, int colorId) {
        this.x = x;
        this.y = y;
        this.visible = visible;
        this.colorId = colorId;
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
        return x + " " + y + " " + visible + " " + colorId;
    }


    //funkar ej perfekt men OK
    public boolean hasCollision(Coordinate other) {
        if (other != this && other.isVisible()) {
            return x < other.getX() + SIZE &&
                    x + SIZE > other.getX() &&
                    y < other.getY() + SIZE &&
                    SIZE + y > other.getY();
        }
        return false;
    }
}
