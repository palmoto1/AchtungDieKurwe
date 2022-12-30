import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game extends JPanel implements Runnable {

    private final Player player;
    private final HashMap<Integer, ArrayList<Coordinate>> paths;

    public Game(Player player) {
        setBackground(Color.black);
        setForeground(Color.white);
        addKeyListener(new InputHandler());
        paths = new HashMap<>();
        this.player = player;
    }

    public void start(){
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.out.println("Starting Game!");
        while (true) {
            repaint();
            checkCollision();
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
        for (Map.Entry<Integer, ArrayList<Coordinate>> set : paths.entrySet()) {
            ArrayList<Coordinate> path = set.getValue();
            for (Coordinate c : path) {
                if(c.isVisible()) {
                    c.paint(g);
                }
            }
        }
    }

    public void checkCollision() {
        for (Map.Entry<Integer, ArrayList<Coordinate>> set : paths.entrySet()) {
            ArrayList<Coordinate> path = set.getValue();
            for (Coordinate c : path) {
                if(player.hasCollision(c)) {
                    //pause();
                }
            }
        }
    }

    public void addCoordinate(String data) {
        String[] tokenizedData = data.split(" ");
        int id = Integer.parseInt(tokenizedData[3]);
        double x = Double.parseDouble(tokenizedData[0]);
        double y = Double.parseDouble(tokenizedData[1]);
        int visible = Integer.parseInt(tokenizedData[2]);
        Coordinate coordinate = new Coordinate(x, y, visible);
        if (!paths.containsKey(id)) {
            paths.put(id, new ArrayList<>());
        }
        paths.get(id).add(coordinate);
    }

    private class InputHandler extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            //System.out.println(e.getKeyChar());
            switch (e.getKeyChar()) {
                case 'a':
                    player.setDirection("left");
                    break;
                case 'd':
                    player.setDirection("right");
                    break;
                case 'p':

                    player.pause();
                    break;
                case 'e':
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }

}