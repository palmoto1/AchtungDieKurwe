import java.awt.*;
import java.util.Objects;

public class Coordinate {


    private static final int LINE_SIZE = 5;
    private static final int COLLISION_SIZE = 3;
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

    public boolean isVisible(){
        return visible != 0;
    }

    public void paint(Graphics g){
        g.setColor(Color.BLUE);
        g.fillOval((int) x, (int) y, LINE_SIZE, LINE_SIZE);
    }

    @Override
    public String toString() {
        return x + " " + y + " " + visible;
    }

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