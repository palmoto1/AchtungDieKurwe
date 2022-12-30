public class Main {
    public static void main(String[] args) {
        Server server;

        if (args.length > 0) {
            server = new Server(Integer.parseInt(args[0]));
        } else {
            server = new Server();
        }

        server.start();
    }
}