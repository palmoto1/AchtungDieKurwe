import java.awt.*;

public final class ColorHandler {

    private ColorHandler(){}

    private static final Color[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.PINK, Color.WHITE};

    public static Color getColor(int colorId){
        if (colorId < 0){
            return Color.GRAY;
        }
        return colors[colorId % colors.length];
    }

}
