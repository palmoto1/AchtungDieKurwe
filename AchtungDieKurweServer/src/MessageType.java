import java.awt.*;

public final class MessageType {
    private MessageType(){}

    public static final String CONNECT = "connect";
    public static final String MOVE = "move";
    public static final String DISCONNECT = "disconnect";
    public static final String READY = "ready";
    public static final String RESTART = "restart";
    public static final String SCORE_UPDATE = "score_update";
}
