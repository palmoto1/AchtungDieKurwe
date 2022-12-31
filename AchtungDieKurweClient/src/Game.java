import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

public class Game extends JPanel implements Runnable {

    //private final Player player;
    private final Client client;
    //private final HashMap<Integer, ArrayList<Coordinate>> paths;
    private final LinkedList<Coordinate> coordinates;

    private String data;

    public Game(Client client) {
        setBackground(Color.black);
        setForeground(Color.white);
        addKeyListener(new InputHandler());
        //paths = new HashMap<>();
        coordinates = new LinkedList<>();
        //this.player = player;
        this.client = client;
        data = "forward";
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Starting Game!");
        while (true) {
            repaint();
            client.send(data);
            try {
                Thread.sleep(100);
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

    private  void paintCoordinates(Graphics g) {
        for (Coordinate c : coordinates) {
            if (c.isVisible()) {
                c.paint(g);
            }
        }
    }


    public  void addCoordinate(String data) {
        coordinates.add(decodeCoordinate(data));
    }

    private Coordinate decodeCoordinate(String data){
        String[] tokenizedData = data.split(" ");
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        return new Coordinate(x, y, visible);
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
                    data = "left";
                    //player.setDirection("left");
                    //client.send("left");
                    break;
                case 'd':
                case 'l':
                    System.out.println("RIGHT!");
                    data = "right";
                    //client.send("right");
                    //player.setDirection("right");
                    break;
                case 'p':

                    //player.pause();
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
            data = "forward";
        }
    }

}