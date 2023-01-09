import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowListener;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class GUI {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    private final JFrame jFrame;

    private final JLabel[] playerLabels;


    private final Game game;

    public GUI(Game game) {
        this.game = game;
        game.setBackground(Color. black);
        game.setForeground(Color.white); // needed?

        jFrame = new JFrame("Achtung Die Kurwe!");
        jFrame.setSize(WIDTH, HEIGHT);

        playerLabels = new JLabel[4];

        for (int i = 0, x = 100; i < playerLabels.length; i++){
            playerLabels[i] = new JLabel("");
            playerLabels[i].setFont(new Font("Verdana", Font.BOLD,20));
            //jLabel.setForeground(Color.BLUE);
            Dimension size = playerLabels[i].getPreferredSize();
            playerLabels[i].setBounds(x, 0, size.width, size.height);;
            game.add(playerLabels[i]);
            x+=100;
        }

        jFrame.getContentPane().add(game);
        jFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        jFrame.setVisible(true);
        game.setFocusable(true);
    }

    public void updatePlayerLabel(String name, int id, int score){
        JLabel label = playerLabels[id % playerLabels.length];
        label.setText(name + ":" + score);
        label.setForeground(ColorHandler.getColor(id));

    }
}
