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
    private static final int READY = 10;


    private final ClientUDP client;
    private final GUI gui;
    private final LinkedList<Coordinate> coordinates;
    private int command;
    private boolean running;
    private boolean active;

    public Game(ClientUDP client) {
        setBackground(Color.black);
        setForeground(Color.white);
        addKeyListener(new InputHandler());
        gui = new GUI(this);

        coordinates = new LinkedList<>();
        this.client = client;
        command = MOVE_FORWARD;
    }


    public void start() {
        running = false;
        active = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Starting Game!");
        while (active) {
            while (running) {
                repaint();
                String msg = MessageType.MOVE + "," + command + "," + client.getName();
                client.sendData(msg.getBytes(StandardCharsets.UTF_8));
                //client.send(command);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(25);
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
        Coordinate coordinate = parseData(data);
        coordinates.add(coordinate);
    }

    private Coordinate parseData(String data) {
        String[] tokenizedData = data.split(" ");
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        int colorId = Integer.parseInt(tokenizedData[3]);
        return new Coordinate(x, y, visible, colorId);
    }

    public void setCommand(int command) {
        this.command = command;
    }

    private class InputHandler implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'a':
                case 'j':
                    System.out.println("LEFT!");
                    command = TURN_LEFT;
                    break;
                case 'd':
                case 'l':
                    System.out.println("RIGHT!");
                    command = TURN_RIGHT;
                    break;
                case 'r':
                    System.out.println("READY!");
                    running = true;
                    //command = READY;
                    break;
                case 'e':
                    //skicka disconnect;
                    String connect = MessageType.DISCONNECT + ",disconnected," + client.getName();
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