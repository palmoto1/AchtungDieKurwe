import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Game {

    private enum GameStatus {
        IDLE, RUNNING
    }

    private static final int MIN_NUMBER_OF_PLAYERS = 1;
    private static final int MAX_NUMBER_OF_PLAYERS = 6;
    private static int NextID = 0;

    private final GameServer server;
    private final HashMap<String, Player> players;
    private final ArrayList<Coordinate> coordinates;
    private final MessageHandler messageHandler;
    private GameStatus gameStatus;

    public Game(GameServer server) {
        this.server = server;
        players = new HashMap<>();
        coordinates = new ArrayList<>();
        messageHandler = new MessageHandler();
        gameStatus = GameStatus.IDLE;
    }

    public void addPlayer(InetAddress address, int port, String name) {
        if (hasPlayer(name)) {
            String toSend = messageHandler.createMessage(
                    MessageType.CONNECTION_DENIED,
                    "Name " + name + " already taken",
                    name
            );
            server.send(toSend.getBytes(StandardCharsets.UTF_8), address, port);
        } else if (players.size() == MAX_NUMBER_OF_PLAYERS) {
            String toSend = messageHandler.createMessage(
                    MessageType.CONNECTION_DENIED,
                    "Lobby is full",
                    name
            );
            server.send(toSend.getBytes(StandardCharsets.UTF_8), address, port);
        } else {
            String toSend = messageHandler.createMessage(MessageType.CONNECTION_OK);
            server.send(toSend.getBytes(StandardCharsets.UTF_8), address, port);

            Player player = new Player(address, port, name, NextID++);
            players.put(name, player);

            toSend = messageHandler.createMessage(MessageType.CONNECT, String.valueOf(player.getId()), name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

            refreshStartingPoints();
            sendScores();
        }

    }

    public void removePlayer(String name) {
        Player player = findPlayer(name);
        if (player != null) {
            Player r = players.remove(name);

            String toSend = messageHandler.createMessage(MessageType.DISCONNECT, String.valueOf(player.getId()), name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

            reloadGame();
        }
    }

    public void checkGameStatus() {
        if (gameStatus == GameStatus.IDLE && players.size() >= MIN_NUMBER_OF_PLAYERS && allPlayersReady()) {
            startGame();
        }
        if (gameStatus == GameStatus.RUNNING && noPlayersActive()) {
            reloadGame();
        }
    }


    public void handleMove(String name, String direction) {
        Player player = findPlayer(name);
        if (player != null && player.isActive()) {
            player.move(direction);

            Coordinate coordinate = player.getSnake().getHead();
            coordinates.add(coordinate);
            sendCoordinate(coordinate, player);

            if (hasCollision(coordinate)) {
                player.deactivate();
                increaseScores();
            }
        }
    }

    public void handleReadyPlayer(String name) {
        Player player = findPlayer(name);

        if (player != null && !player.isReady()) {
            player.setReady();

            String toSend = messageHandler.createMessage(MessageType.READY, name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
        }
    }

    private void startGame() {
        gameStatus = GameStatus.RUNNING;
        activatePlayers();
    }

    private void reloadGame() {

        gameStatus = GameStatus.IDLE;
        coordinates.clear();

        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            player.initialize();
        }
        String toSend = messageHandler.createMessage(MessageType.RESTART);
        sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

        refreshStartingPoints();
    }

    private void increaseScores() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            if (player.isActive()) {
                player.increaseScore();
            }
        }
        sendScores(); // hitta optimering, blir 0n^2
    }

    private void sendScores() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            String toSend = messageHandler.createMessage(
                    MessageType.SCORE_UPDATE,
                    String.valueOf(player.getId()),
                    player.getName(),
                    String.valueOf(player.getScore())
            );
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
        }
    }

    private Player findPlayer(String name) {
        return players.get(name);
    }

    private boolean hasPlayer(String name) {
        return findPlayer(name) != null;
    }


    private void activatePlayers() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();
            player.activate();
        }
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

    private void refreshStartingPoints() {
        for (Map.Entry<String, Player> set : players.entrySet()) {
            Player player = set.getValue();

            Coordinate startingPoint = player.getSnake().getHead();
            sendCoordinate(startingPoint, player);
        }
    }

    private boolean hasCollision(Coordinate coordinate) {
        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinate.hasCollision(coordinates.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void sendCoordinate(Coordinate coordinate, Player fromPlayer) {
        String toSend = messageHandler.createMessage(
                MessageType.MOVE,
                coordinate.toString(),
                fromPlayer.getName()
        );
        sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
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
}
