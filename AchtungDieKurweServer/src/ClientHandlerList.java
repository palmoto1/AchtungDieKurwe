import java.util.concurrent.ArrayBlockingQueue;

public class ClientHandlerList {


    private ArrayBlockingQueue<ClientHandler> clientHandlers; //lägg i adapter klass
    private int capacity;

    public ClientHandlerList() {
        this.capacity = 10;
        this.clientHandlers = new ArrayBlockingQueue<>(capacity);
    }

    public synchronized void broadcast(String msg, ClientHandler clientHandler) {
        for (ClientHandler u : clientHandlers) {
            if (!u.equals(clientHandler)) {
                u.printMessage(msg);
            }
        }
    }

    public synchronized void removeThread(ClientHandler clientHandler) {
        if (clientHandlers.remove(clientHandler)) {
            System.out.println("The user " + clientHandler.getName() + " quit");
        }
    }

    public synchronized void addThread(ClientHandler clientHandler) {

        if (!clientHandlers.contains(clientHandler)) {
            if (clientHandlers.size() == capacity) {
                increaseCapacity();
            }
            clientHandlers.add(clientHandler);
        }

    }

    private void increaseCapacity() {
        capacity *= 2;
        ArrayBlockingQueue<ClientHandler> copy = new ArrayBlockingQueue<>(capacity);
        copy.addAll(clientHandlers);
        clientHandlers = copy;
    }
}
