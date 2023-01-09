import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;


//behöver verkligen vara trådad? kan repainta o skicka i clientens loop
public class Game extends JPanel implements Runnable {

    private static final int MOVE_FORWARD = 0;
    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;


    private final ClientUDP client;
    private final LinkedList<Coordinate> coordinates;
    private int command;
    private boolean running;

    public Game(ClientUDP client) {
        addKeyListener(new InputHandler());

        coordinates = new LinkedList<>();
        this.client = client;
        command = MOVE_FORWARD;
    }


    public void start() {
        running = true;

        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Starting Game!");

        while (running) {
            repaint();
            String message = MessageType.MOVE + "," + command + "," + client.getUserName();
            client.sendData(message.getBytes(StandardCharsets.UTF_8));
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

    public class InputHandler implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

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
                case 'r':
                    //System.out.println("READY!");
                    String ready = MessageType.READY + ",," + client.getUserName();
                    client.sendData(ready.getBytes(StandardCharsets.UTF_8));
                    //command = READY;
                    break;
                case 'e':
                    client.createMessage(MessageType.DISCONNECT, client.getUserName());
                    String connect = client.createMessage(MessageType.DISCONNECT, client.getUserName());
                    client.sendData(connect.getBytes(StandardCharsets.UTF_8));
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
    }

}