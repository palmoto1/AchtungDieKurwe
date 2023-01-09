import java.awt.*;

public class Coordinate {


    private static final int SIZE = 5;
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

    public boolean isVisible(){
        return visible != 0;
    }

    public void paint(Graphics g){
        g.setColor(ColorHandler.getColor(colorId));
        g.fillOval((int) x, (int) y, SIZE, SIZE);
    }

    @Override
    public String toString() {
        return x + ":" + y + ":" + visible;
    }

}