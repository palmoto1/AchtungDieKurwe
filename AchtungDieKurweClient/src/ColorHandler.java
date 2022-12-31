import java.awt.*;

public class ColorHandler {

    private ColorHandler(){}

    private static final Color[] colors = {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.PINK, Color.WHITE};

    public static Color getColor(int colorId){
        return colors[colorId];
    }

}
