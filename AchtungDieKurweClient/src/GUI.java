import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 800;

    private final JFrame jFrame;

    private final JPanel chat;

    private final JLabel[] playerLabels;
    private final Font font;

    private JTextArea chatArea;
    private JTextField inputText;


    private final Game game;

    public GUI(Game game) {
        this.game = game;
        this.game.setBackground(Color.black);
        //game.setForeground(Color.black); // needed?


        jFrame = new JFrame("Achtung Die Kurwe!");
        jFrame.setSize(WIDTH, HEIGHT);

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

        chat = new JPanel(); // maybe should be own class


        JLabel inputLabel = new JLabel("Chat:");
        inputText = new JTextField(16);
        chatArea = new JTextArea(30, 16);
        JButton buttonAdd = new JButton("Submit");
        buttonAdd.addActionListener(new ButtonListener());
        //buttonAdd.getModel().addChangeListener(new ButtonListener());

        chatArea.setEditable(false);
        chatArea.setBackground(Color.LIGHT_GRAY);

        inputText.setText("Enter your name!");

        chat.add(inputLabel);
        chat.add(inputText);
        chat.add(buttonAdd);
        chat.add(new JScrollPane(chatArea));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.game, chat);
        splitPane.setDividerLocation(600);

        jFrame.add(splitPane);
        jFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                game.disconnect();
                System.exit(0);
            }
        });

        jFrame.setVisible(true);
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

    public void appendChat(String text){
        chatArea.append(text);
    }

    private class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) { // fixa detta så username måste godkännas innan start
            if (!game.isRunning() && !inputText.getText().isBlank()){
                game.init(inputText.getText());
            }
            game.requestFocus();
        }
    }

}
