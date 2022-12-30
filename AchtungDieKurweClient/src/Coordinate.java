import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Coordinate implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final int SIZE = 3;
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
        g.fillOval((int) x, (int) y, 5, 5);
    }

    @Override
    public String toString() {
        return x + " " + y + " " + visible;
    }

    public boolean hasCollision(Coordinate other) {
        if (other != this && isVisible() && other.isVisible()) {
            if (x < other.getX() + SIZE &&
                    x + SIZE > other.getX() &&
                    y < other.getY() + SIZE &&
                    SIZE + y > other.getY()) {
                System.out.println("Collision!!!");
                System.out.println(x + " " + other.getX() + " " + y +" "+other.getY());
                return true;}
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate that)) return false;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}