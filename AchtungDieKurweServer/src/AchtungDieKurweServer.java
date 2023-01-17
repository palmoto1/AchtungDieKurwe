public class AchtungDieKurweServer {

    public static void main(String[] args) {
        GameServer gameServer;
        ChatServer chatServer;

        if (args.length > 0) {
            gameServer = new GameServer(Integer.parseInt(args[0]));
            chatServer = new ChatServer(Integer.parseInt(args[0]));
        } else {
            gameServer = new GameServer();
            chatServer = new ChatServer();
        }

        gameServer.start();
        chatServer.start();
    }
}