import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI extends JFrame {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;
    private final Game game;
    private final Chat chat;

    private final JLabel[] playerLabels;
    private final Font font;

    public GUI(Game game, Chat chat) {
        this.game = game;

        setTitle("Achtung Die Kurwe!");
        setSize(WIDTH, HEIGHT);

        playerLabels = new JLabel[6];
        font = new Font("Verdana", Font.BOLD, 20);

        for (int i = 0, x = 100; i < playerLabels.length; i++) {
            playerLabels[i] = new JLabel("");
            playerLabels[i].setFont(font);
            Dimension size = playerLabels[i].getPreferredSize();
            playerLabels[i].setBounds(x, 0, size.width, size.height);
            this.game.add(playerLabels[i]);
            x += 100;
        }

        this.chat = chat; // maybe should be own class
        chat.setButtonListener(new ButtonListener());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.game, chat);
        splitPane.setDividerLocation(600);

        add(splitPane);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                game.disconnect();
                System.exit(0);
            }
        });

        setVisible(true);
        chat.setFocusable(true);
        this.game.setFocusable(true);
    }


    public void updatePlayerLabel(String text, int id) {
        JLabel label = playerLabels[id % playerLabels.length];
        label.setText(text);
        label.setForeground(ColorHandler.getColor(id));

    }

    public void clearPlayerLabel(int id) {
        JLabel label = playerLabels[id % playerLabels.length];
        label.setText("");

    }

    public void startChat(){
        chat.startChatClient();
    }

    public void appendChat(String text){
        chat.append(text);
    }

    public class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) { // fixa detta så username måste godkännas innan start
            if (!chat.getInputText().isBlank()) {
                if (!game.isRunning()) {
                    game.setUserName(chat.getInputText().trim());
                    chat.setUserName(chat.getInputText().trim()); // KAN FIXAS I GAME
                    game.connect();
                } else {
                    chat.getClientTCP().write(chat.getInputText());
                }
            }
            game.requestFocus();
        }
    }

}
