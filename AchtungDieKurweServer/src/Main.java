public class Main {
    public static void main(String[] args) {
        ServerUDP serverUDP;
        ServerTCP serverTCP;

        if (args.length > 0) {
            serverUDP = new ServerUDP(Integer.parseInt(args[0]));
            serverTCP = new ServerTCP(Integer.parseInt(args[0]));
        } else {
            serverUDP = new ServerUDP();
            serverTCP = new ServerTCP();
        }

        serverUDP.start();
        serverTCP.start();
    }
}