import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//game

public class GUI extends JFrame {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Game game;
    private final Chat chat;

    public GUI(Game game, Chat chat) {
        this.game = game;
        this.chat = chat;

        setTitle("Achtung Die Kurwe!");
        setSize(WIDTH, HEIGHT);

        this.chat.setButtonListener(new ButtonListener());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.game, this.chat);
        splitPane.setDividerLocation(600);

        add(splitPane);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                game.disconnect();
                System.exit(0);
            }
        });

        setVisible(true);
        this.chat.setFocusable(true);
        this.game.setFocusable(true);
        this.game.setChat(this.chat);
    }

    public class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!chat.getInputText().isBlank()) {
                if (!game.isRunning()) {
                    game.setUserName(chat.getInputText().trim());
                    chat.setUserName(chat.getInputText().trim());
                    game.connect();
                } else {
                    chat.getClientTCP().write(chat.getInputText());
                }
            }
            game.requestFocus();
        }
    }

}
