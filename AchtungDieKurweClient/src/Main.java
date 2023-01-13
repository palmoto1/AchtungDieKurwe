public class Main {

    public static void main(String[] args) {



        ClientUDP clientUDP;
        ClientTCP clientTCP;

        if (args.length >= 2) {
            clientUDP = new ClientUDP(args[0], Integer.parseInt(args[1]));
            clientTCP = new ClientTCP(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 1) {
            clientUDP = new ClientUDP(args[0]);
            clientTCP = new ClientTCP(args[0]);
        } else {
            clientUDP = new ClientUDP();
            clientTCP = new ClientTCP();
        }

        //lite rörigt detta, se om går att göra bättre
        Game game = new Game(clientUDP);
        Chat chat = new Chat(clientTCP);

        new GUI(game, chat);


        clientUDP.start();
    }
}