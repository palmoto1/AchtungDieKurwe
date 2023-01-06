public class Main {
    public static void main(String[] args) {
        ServerUDP server;

        if (args.length > 0) {
            server = new ServerUDP(Integer.parseInt(args[0]));
        } else {
            server = new ServerUDP();
        }

        server.start();
    }
}