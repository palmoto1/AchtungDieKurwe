import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameHandler {

    private enum GameStatus{
        IDLE, RUNNING
    }

    private static final int MIN_NUMBER_OF_PLAYERS = 1;
    private static int NextID = 0;

    private final ServerUDP server;
    private final HashMap<String, Player> players;
    private final ArrayList<Coordinate> coordinates;
    private GameStatus gameStatus;

    public GameHandler(ServerUDP server) {
        this.server = server;
        players = new HashMap<>();
        coordinates = new ArrayList<>();
        gameStatus = GameStatus.IDLE;
    }

    public void addPlayer(InetAddress address, int port, String name) {
        if (!hasPlayer(name)) {
            Player player = new Player(address, port, name, NextID++);
            players.put(name, player);

            String toSend = createMessage(MessageType.CONNECT, String.valueOf(player.getId()), name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

            refreshStartingPoints();
            sendScores();
        }

    }

    public void removePlayer(String name) {
        if (hasPlayer(name)) {
            players.remove(name);
            if (players.isEmpty()) {
                coordinates.clear();
            }
            reloadGame();
        }
    }


    public void handleMove(String name, String direction) {
        Player player = findPlayer(name);
        if (player != null && player.isActive()) {
            player.move(direction);

            Coordinate coordinate = player.getHead();
            coordinates.add(coordinate);
            sendCoordinate(coordinate, player);

            if (hasCollision(coordinate)) {
                player.deactivate();
                increaseScores();
            }
        }
    }

    public void checkGameStatus(){
        if (noPlayersActive() && gameStatus == GameStatus.RUNNING){
            reloadGame();
        }
    }

    public void reloadGame(){
        System.out.println("New Game!");

        gameStatus = GameStatus.IDLE;
        coordinates.clear();

        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            player.initialize();
        }
        String toSend = createMessage(MessageType.RESTART);
        sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

        refreshStartingPoints();
    }

    private void startGame(){
        gameStatus = GameStatus.RUNNING;
        activatePlayers();
    }

    private void increaseScores() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            if (player.isActive()) {
                player.increaseScore();
            }
            System.out.println(player.getName() + ": " + player.getScore());
        }
        sendScores(); // hitta optimering, blir 0n^2
        System.out.println();
    }

    private void sendScores() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            String toSend =
                    MessageType.SCORE_UPDATE + "," +
                            player.getId() + "," +
                            player.getName() + "," +
                            player.getScore();

            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
        }
    }


    public void handleReadyUser(String name) {
        Player player = findPlayer(name);

        if (!player.isReady()) {
            player.setReady();

            String toSend = createMessage(MessageType.READY, name);
            sendToAllButOne(toSend.getBytes(StandardCharsets.UTF_8), player);

            if (players.size() >= MIN_NUMBER_OF_PLAYERS && allPlayersReady()) {
                startGame();
            }
        }


    }


    private void activatePlayers() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            player.activate();
        }
    }

    private Player findPlayer(String name) {
        return players.get(name);
    }

    private void refreshStartingPoints() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();

            Coordinate startingPoint = player.getHead();
            sendCoordinate(startingPoint, player);
        }
    }

    private void sendCoordinate(Coordinate coordinate, Player fromPlayer) {
        String toSend = createMessage(MessageType.MOVE, coordinate.toString(), fromPlayer.getName());
        sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
    }

    private boolean hasPlayer(String name) {
        return findPlayer(name) != null;
    }

    private boolean allPlayersReady() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            if (!player.isReady()) {
                return false;
            }
        }
        return !players.isEmpty();
    }

    private boolean noPlayersActive() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            if (player.isActive()) {
                return false;
            }
        }
        return !players.isEmpty();
    }

    private boolean hasCollision(Coordinate coordinate) {
        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinate.hasCollision(coordinates.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void sendToAll(byte[] data) {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            server.send(data, player.getAddress(), player.getPort());
        }
    }

    private void sendToAllButOne(byte[] data, Player excluded) {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            if (!set.getKey().equals(excluded.getName())) {
                server.send(data, player.getAddress(), player.getPort());
            }
        }
    }

    //move to messagehandler
    private String createMessage(String type, String content, String userName) {
        return type + "," + content + "," + userName;
    }

    //move to messagehandler
    private String createMessage(String type, String userName) {
        return createMessage(type, "null", userName);
    }

    private String createMessage(String type) {
        return createMessage(type, "null");
    }
}
