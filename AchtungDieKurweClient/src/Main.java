public class Main {


    //TODO: lägg till syncrozied på alla trådade uppgifter?
    public static void main(String[] args) {



        Client client;

        if (args.length >= 2) {
            client = new Client(args[0], Integer.parseInt(args[1]));
        } else if (args.length == 1) {
            client = new Client(args[0]);
        } else {
            client = new Client();
        }

        client.start();
    }
}