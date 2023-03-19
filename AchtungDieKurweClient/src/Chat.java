import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

//chat handler

public class Chat extends JPanel {

    private final ChatClient chatClient;
    private final JTextArea chatArea;
    private final JTextField inputText;

    private final JButton jButton;

    public Chat(ChatClient chatClient){
        this.chatClient = chatClient;
        this.chatClient.setChat(this);
        JLabel inputLabel = new JLabel("Chat:");
        inputText = new JTextField(16);
        chatArea = new JTextArea(30, 16);
        jButton = new JButton("Submit");

        chatArea.setEditable(false);
        chatArea.setBackground(Color.LIGHT_GRAY);

        inputText.setText("Enter your name!");

        add(inputLabel);
        add(inputText);
        add(jButton);
        add(new JScrollPane(chatArea));
    }

    public void setUserName(String userName) {
        chatClient.setUser(userName);
    }

    public void startChatClient(){
        chatClient.start();
    }

    public ChatClient getClientTCP() {
        return chatClient;
    }

    public void setButtonListener(ActionListener actionListener){
        jButton.addActionListener(actionListener);
    }

    public String getInputText(){
        return inputText.getText();
    }

    public void append(String text){
        if (text != null) {
            chatArea.append(text + "\n\n");
        }
    }
}
