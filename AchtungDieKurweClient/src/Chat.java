import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

//chat handler

public class Chat extends JPanel {

    private final ClientTCP clientTCP;
    private final JTextArea chatArea;
    private final JTextField inputText;

    private final JButton jButton;

    public Chat(ClientTCP clientTCP){
        this.clientTCP = clientTCP;
        this.clientTCP.setChat(this);
        JLabel inputLabel = new JLabel("Chat:");
        inputText = new JTextField(16);
        chatArea = new JTextArea(30, 16);
        jButton = new JButton("Submit");
        //buttonAdd.getModel().addChangeListener(new ButtonListener());

        chatArea.setEditable(false);
        chatArea.setBackground(Color.LIGHT_GRAY);

        inputText.setText("Enter your name!");

        add(inputLabel);
        add(inputText);
        add(jButton);
        add(new JScrollPane(chatArea));
    }

    public void setUserName(String userName) {
        clientTCP.setUser(userName);
    }

    public void startChatClient(){
        clientTCP.start();
    }

    public ClientTCP getClientTCP() {
        return clientTCP;
    }

    public void setButtonListener(ActionListener actionListener){
        jButton.addActionListener(actionListener);
    }

    public String getInputText(){
        return inputText.getText();
    }

    public void append(String text){
        chatArea.append(text + "\n\n");
    }
}
