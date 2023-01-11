import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;


//behöver verkligen vara trådad? kan repainta o skicka i clientens loop? behövs syncronized?
public class Game extends JPanel implements Runnable {

    private static final int MOVE_FORWARD = 0;
    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;


    private final ClientUDP client;
    private GUI gui;

    private final LinkedList<Coordinate> coordinates;
    private final MessageHandler messageHandler;

    private String userName;
    private int command;
    private boolean running;

    public Game(ClientUDP client) {
        this.client = client;
        //gui = new GUI(this);
        coordinates = new LinkedList<>();
        messageHandler = new MessageHandler();
        command = MOVE_FORWARD;
        addKeyListener(new InputHandler());
    }

    public void setGUI(GUI gui){
        this.gui = gui;
    }


    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void connect(){
        String message = messageHandler.createMessage(MessageType.CONNECT, userName);
        client.send(message.getBytes(StandardCharsets.UTF_8));
    }

    public void disconnect(){
        String message = messageHandler.createMessage(MessageType.DISCONNECT, userName);
        client.send(message.getBytes(StandardCharsets.UTF_8));
    }

    public void handleNewPlayer(String player, int id){
        gui.updatePlayerLabel(player + " : " + 0, id);
        gui.appendChat(player + " has joined!");
    }

    public void handleDisconnectedPlayer(String player, int id){
        gui.clearPlayerLabel(id);
        gui.appendChat(player + " has left!");
    }

    public void displayError(String error){
        gui.appendChat(error);
    }

    public void handleReadyPlayer(String player){
        gui.appendChat(player + " is ready!");
    }

    public void updateScore(String player, int id, int score){
        gui.updatePlayerLabel(player + " : " + score, id);
    }

    public void init(String userName) {
        this.userName = userName;
        connect();
    }
    public void setReady(){
        String ready = messageHandler.createMessage(MessageType.READY, userName);
        client.send(ready.getBytes(StandardCharsets.UTF_8));
    }


    @Override
    public void run() {
        System.out.println("Starting Game!");

        while (running) {
            repaint();
            String message = messageHandler.createMessage(MessageType.MOVE, String.valueOf(command), userName);
            client.send(message.getBytes(StandardCharsets.UTF_8));
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

    public synchronized void paintCoordinates(Graphics g) {
        for (Coordinate c : coordinates) {
            if (c.isVisible()) {
                c.paint(g);
            }
        }
    }


    public synchronized void addCoordinate(String data) {
        Coordinate coordinate = parseCoordinate(data);
        coordinates.add(coordinate);
    }

    public synchronized void clearCoordinates() {
        coordinates.clear();
    }

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

    public void stop() {
        this.running = false;
    }

    public class InputHandler implements KeyListener {

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'a':
                case 'j':
                    //System.out.println("LEFT!");
                    command = TURN_LEFT;
                    break;
                case 'd':
                case 'l':
                    //System.out.println("RIGHT!");
                    command = TURN_RIGHT;
                    break;
                case 'r': // ska göras med ready knapp i gui
                    //System.out.println("READY!");
                    setReady();
                    //command = READY;
                    break;
                case 'e': //esc och window closed istället
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