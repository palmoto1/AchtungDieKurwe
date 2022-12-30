import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Game extends JPanel implements Runnable {

    private final Player player;

    public Game(Player player) {
        setBackground(Color.black);
        setForeground(Color.white);
        addKeyListener(new InputHandler());
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
            player.checkCollision();
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
        player.paintComponent(g);
    }

    private class InputHandler extends KeyAdapter {

        public InputHandler() {
            System.out.println("New key listener");
        }

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