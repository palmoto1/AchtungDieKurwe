import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    private final Game game;

    public GUI(Game game) {
        this.game = game;
        setTitle("Achtung Die Kurwe!");
        setSize(WIDTH, HEIGHT);
        getContentPane().add(game);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        game.setFocusable(true);
    }







}
