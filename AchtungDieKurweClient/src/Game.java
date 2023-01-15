import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class Game extends JPanel implements Runnable {

    private static final int MOVE_FORWARD = 0;
    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;


    private final GameClient gameClient;
    private Chat chat;

    private final JLabel[] playerLabels;

    private final LinkedList<Coordinate> coordinates;
    private final MessageHandler messageHandler;

    private String userName;
    private int command;
    private boolean running;

    public Game(GameClient gameClient) {
        this.gameClient = gameClient;
        this.gameClient.setGame(this);
        coordinates = new LinkedList<>();
        messageHandler = new MessageHandler();
        command = MOVE_FORWARD;

        setBackground(Color.black);
        addKeyListener(new InputHandler());

        playerLabels = new JLabel[6];
        Font font = new Font("Verdana", Font.BOLD, 20);

        for (int i = 0, x = 100; i < playerLabels.length; i++) {
            playerLabels[i] = new JLabel("");
            playerLabels[i].setFont(font);
            Dimension size = playerLabels[i].getPreferredSize();
            playerLabels[i].setBounds(x, 0, size.width, size.height);
            add(playerLabels[i]);
            x += 100;
        }
    }

    public void setChat(Chat chat){
        this.chat = chat;
    }


    /**
     * Starts the thread and the chat client
     */
    public void start() {
        running = true;
        chat.startChatClient();
        new Thread(this).start();
    }


    /**
     * Creates a message for connection and sends it to the server
     */
    public void connect(){
        String message = messageHandler.createMessage(MessageType.CONNECT, userName);
        gameClient.send(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a message for disconnection and sends it to the server
     */
    public void disconnect(){
        String message = messageHandler.createMessage(MessageType.DISCONNECT, userName);
        gameClient.send(message.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Makes the game output that a new player has joined
     */
    public void handleNewPlayer(String player, int id){
        updatePlayerLabel(player + " : " + 0, id);
        chat.append(player + " has joined!");
    }

    /**
     * Makes the game output that a player has quit
     */
    public void handleDisconnectedPlayer(String player, int id){
        clearPlayerLabel(id);
        chat.append(player + " has left!");
    }

    /**
     * Creates a message that the player is ready and sends it to the server
     */
    public void setReady(){
        String ready = messageHandler.createMessage(MessageType.READY, userName);
        gameClient.send(ready.getBytes(StandardCharsets.UTF_8));
    }

    public void displayError(String error){
        chat.append(error);
    }

    public void handleReadyPlayer(String player){
        chat.append(player + " is ready!");
    }

    public void updateScore(String player, int id, int score){
        updatePlayerLabel(player + " : " + score, id);
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public void updatePlayerLabel(String text, int id) {
        JLabel label = playerLabels[id % playerLabels.length];
        label.setText(text);
        label.setForeground(ColorHandler.getColor(id));

    }

    public void clearPlayerLabel(int id) {
        JLabel label = playerLabels[id % playerLabels.length];
        label.setText("");

    }


    /**
     * Continuously repaints the game and sends the current move direction
     */
    @Override
    public void run() {
        System.out.println("Starting Game!");

        while (running) {
            repaint();
            String message = messageHandler.createMessage(MessageType.MOVE, String.valueOf(command), userName);
            gameClient.send(message.getBytes(StandardCharsets.UTF_8));
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintCoordinates(g);
    }


    /**
     * Draws all coordinates in the collection of coordinates
     * @param g the graphic
     */
    public synchronized void paintCoordinates(Graphics g) {
        for (Coordinate coordinate : coordinates) {
            if (coordinate.isVisible()) {
                coordinate.paint(g);
            }
        }
    }


    /**
     * adds a new coordinate received from the server
     * @param data
     */
    public synchronized void addCoordinate(String data) {
        Coordinate coordinate = parseCoordinate(data);
        coordinates.add(coordinate);
    }

    public synchronized void clearCoordinates() {
        coordinates.clear();
    }

    /**
     * Parses cordinate data from a string
     * @param coordinate
     * @return a coordinate object
     */
    private Coordinate parseCoordinate(String coordinate) {
        String[] tokenizedData = coordinate.split(":");
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        int colorId = Integer.parseInt(tokenizedData[3]);
        return new Coordinate(x, y, visible, colorId);
    }

    public boolean isRunning() {
        return running;
    }

    public class InputHandler implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'a':
                case 'j':
                    command = TURN_LEFT;
                    break;
                case 'd':
                case 'l':
                    command = TURN_RIGHT;
                    break;
                case 'r':
                    setReady();
                    break;
                case 'e':
                    disconnect();
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            command = MOVE_FORWARD;
        }

        @Override
        public void keyTyped(KeyEvent e) {}
    }


}