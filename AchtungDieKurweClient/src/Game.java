import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;

public class Game extends JPanel implements Runnable {

    private static final int MOVE_FORWARD = 0;
    private static final int TURN_LEFT = 1;
    private static final int TURN_RIGHT = 2;


    private final Client client;
    private final LinkedList<Coordinate> coordinates;
    private int command;

    public Game(Client client) {
        setBackground(Color.black);
        setForeground(Color.white);
        addKeyListener(new InputHandler());
        coordinates = new LinkedList<>();
        this.client = client;
        command = MOVE_FORWARD;
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Starting Game!");
        while (true) {
            repaint();
            client.send(command);
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

    private synchronized void paintCoordinates(Graphics g) {
        for (Coordinate c : coordinates) {
            if (c.isVisible()) {
                c.paint(g);
            }
        }
    }


    public synchronized void addCoordinate(String data) {
        Coordinate coordinate = decodeCoordinate(data);
        coordinates.add(coordinate);
    }

    private Coordinate decodeCoordinate(String data){
        String[] tokenizedData = data.split(" ");
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        int colorId = Integer.parseInt(tokenizedData[3]);
        return new Coordinate(x, y, visible, colorId);
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
                case 'e':
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