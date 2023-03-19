import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class handling the game logic
 */
public class Game {

    private enum GameStatus {
        IDLE, RUNNING
    }

    private static final int MIN_NUMBER_OF_PLAYERS = 1;
    private static final int MAX_NUMBER_OF_PLAYERS = 6;
    private static int NextID = 0;

    private final GameServer server;
    private final ConcurrentHashMap<String, Player> players;
    private final ArrayList<Coordinate> coordinates;
    private final MessageHandler messageHandler;
    private volatile GameStatus gameStatus;

    public Game(GameServer server) {
        this.server = server;
        players = new ConcurrentHashMap<>();
        coordinates = new ArrayList<>();
        messageHandler = new MessageHandler();
        gameStatus = GameStatus.IDLE;
    }

    /**
     * Adding a player to the game if the name is avalible and the lobby is not full
     * @param address The inet address of the player
     * @param port the port of the player
     * @param name the name of the player
     */
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
            players.putIfAbsent(name, player);

            toSend = messageHandler.createMessage(MessageType.CONNECT, String.valueOf(player.getId()), name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

            refreshStartingPoints();
            sendScores();
        }

    }

    /**
     * Removes a player if it exist and reloads the game
     * @param name the name of the player
     */
    public void removePlayer(String name) {
        Player player = players.remove(name);
        if (player != null) {
            String toSend = messageHandler.createMessage(MessageType.DISCONNECT, String.valueOf(player.getId()), name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

            reloadGame();
        }
    }

    /**
     * Checks if the game should be started or reloaded
     */
    public void checkGameStatus() {
        if (gameStatus == GameStatus.IDLE && players.size() >= MIN_NUMBER_OF_PLAYERS && allPlayersReady()) {
            startGame();
        }
        if (gameStatus == GameStatus.RUNNING && noPlayersActive()) {
            reloadGame();
        }
    }


    /**
     * Updates the movement of a player based on the received direction
     * Sends the cordinate to all connected players
     * Deactivates the player if a collision occurred.
     * @param name the player name
     * @param direction the direction of the move
     */
    public void handleMove(String name, String direction) {
        Player player = players.get(name);
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

    /**
     * Sets the player as ready and notifies connected clients
     * @param name the player name
     */
    public void handleReadyPlayer(String name) {
        Player player = players.get(name);

        if (player != null && !player.isReady()) {
            player.setReady();

            String toSend = messageHandler.createMessage(MessageType.READY, name);
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Activates all the players and thereby starts the game
     */
    private void startGame() {
        gameStatus = GameStatus.RUNNING;
        activatePlayers();
    }

    /**
     * Reloads the game by setting all players in idle mode with
     * new starting points.
     */
    private void reloadGame() {

        gameStatus = GameStatus.IDLE;
        coordinates.clear();

        players.forEach(1, (name, player) ->
                player.initialize());

        String toSend = messageHandler.createMessage(MessageType.RESTART);
        sendToAll(toSend.getBytes(StandardCharsets.UTF_8));

        refreshStartingPoints();
    }

    /**
     * Increases the scores of active players.
     * Sends updates to all clients
     */
    private void increaseScores() {
        players.forEach(1, (name, player) ->
                {
                    if (player.isActive()) {
                        player.increaseScore();
                    }
                }
        );
        sendScores();

    }

    /**
     * Sends the scores of all players to all clients
     */
    private void sendScores() {
        players.forEach(1, (name, player) ->
        {
            String toSend = messageHandler.createMessage(
                    MessageType.SCORE_UPDATE,
                    String.valueOf(player.getId()),
                    name,
                    String.valueOf(player.getScore())
            );
            sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
        });

    }
    private boolean hasPlayer(String name) {
        return players.get(name) != null;
    }


    private void activatePlayers() {
        players.forEach(1, (name, player) ->
                player.activate());

    }

    /**
     * Checks if all the players are ready
     * @return true if all there are connected players and those players are ready,
     * otherwise false
     */
    private boolean allPlayersReady() {
        AtomicBoolean allReady = new AtomicBoolean(true);
        players.forEach(1, (name, player) ->
        {
            if (!player.isReady()) allReady.set(false);
        });


        return (allReady.get() && !players.isEmpty());
    }

    /**
     * Checks if no players are active in a match
     * @return true if all there are connected players and those players are not active,
     * otherwise false
     */
    private boolean noPlayersActive() {
        AtomicBoolean noActive = new AtomicBoolean(true);
        players.forEach(1, (name, player) ->
        {
            if (player.isActive()) noActive.set(false);
        });

        return noActive.get() && !players.isEmpty();
    }

    /**
     * Sends the new starting points of all players to all clients
     */
    private void refreshStartingPoints() {

        players.forEach(1, (name, player) ->
        {
            Coordinate startingPoint = player.getSnake().getHead();
            sendCoordinate(startingPoint, player);
        });

    }

    /**
     * Checks weather a coordinate has a collision
     * @param coordinate a coordinate
     * @return true if the coordinate has a collision
     */
    private boolean hasCollision(Coordinate coordinate) {
        for (int i = 1; i < coordinates.size(); i++) {
            if (coordinate.hasCollision(coordinates.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sends a coordinate to all clients
     * @param coordinate the coordinate
     * @param fromPlayer the player the coordinate belongs to
     */
    private void sendCoordinate(Coordinate coordinate, Player fromPlayer) {
        String toSend = messageHandler.createMessage(
                MessageType.MOVE,
                coordinate.toString(),
                fromPlayer.getName()
        );
        sendToAll(toSend.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Sends a byte array to all player clients
     * @param data
     */
    private void sendToAll(byte[] data) {
        players.forEach(1, (name, player) ->
        {
            server.send(data, player.getAddress(), player.getPort());
        });

    }
}
