public class Main {

    public static void main(String[] args) {



        ClientUDP client;

        if (args.length >= 2) {
            client = new ClientUDP(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 1) {
            client = new ClientUDP(args[0]);
        } else {
            client = new ClientUDP();
        }

        Game game = new Game(client);
        GUI gui = new GUI(game);
        game.setGUI(gui);
        client.setGame(game);
        client.start();
    }
}