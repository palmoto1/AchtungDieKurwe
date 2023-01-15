public class AchtungDieKurweClient {

    public static void main(String[] args) {



        GameClient gameClient;
        ChatClient chatClient;

        if (args.length >= 2) {
            gameClient = new GameClient(args[0], Integer.parseInt(args[1]));
            chatClient = new ChatClient(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 1) {
            gameClient = new GameClient(args[0]);
            chatClient = new ChatClient(args[0]);
        } else {
            gameClient = new GameClient();
            chatClient = new ChatClient();
        }


        Game game = new Game(gameClient);
        Chat chat = new Chat(chatClient);

        new GUI(game, chat);


        gameClient.start();
    }
}