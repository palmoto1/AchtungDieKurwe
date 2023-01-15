import java.util.concurrent.ArrayBlockingQueue;

public class ClientHandlerList {


    private ArrayBlockingQueue<ClientHandler> clientHandlers; //lägg i adapter klass
    private int capacity;

    public ClientHandlerList() {
        this.capacity = 10;
        this.clientHandlers = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Sends a message to all clients
     * @param message the message to be sent
     */
    public synchronized void sendToAll(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.printMessage(message);

        }
    }

    public synchronized void remove(ClientHandler clientHandler) {
        if (clientHandlers.remove(clientHandler)) {
            System.out.println("The user " + clientHandler.getName() + " quit");
        }
    }

    public synchronized void add(ClientHandler clientHandler) {

        if (!clientHandlers.contains(clientHandler)) {
            if (clientHandlers.size() == capacity) {
                increaseCapacity();
            }
            clientHandlers.add(clientHandler);
        }

    }

    /**
     * Doubles the capacity of the queue
     */
    private void increaseCapacity() {
        capacity *= 2;
        ArrayBlockingQueue<ClientHandler> copy = new ArrayBlockingQueue<>(capacity);
        copy.addAll(clientHandlers);
        clientHandlers = copy;
    }
}
