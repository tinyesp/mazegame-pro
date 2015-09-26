package mazegame.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import mazegame.message.application.Direction;
import mazegame.server.GameStatus;
import mazegame.server.exceptions.InvalidMoveException;
import mazegame.server.exceptions.JoinGameFailException;
import mazegame.util.MazeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class only maintains the world.
 *   
 * @author WEIMING
 *
 */
public class MazeWorld {
	private static Logger logger = LoggerFactory.getLogger(MazeWorld.class);
	
	// Singleton, we only need one world
	private static MazeWorld _inst;
	
	// Player id counter
	private AtomicInteger idCounter;
	
	// The current game status
	private GameStatus status;
	// Game start time
	private long startTime;
	
	// Map
	//   ------- x
	//   |
	//   |
	//   y
	
	// The map length, left to right
	private int length;
	// The map width, up to down
	private int width;
	// Total created treasures
	private int totalTreasures;
	
	// Treasures that are still available
	private List<Treasure> treasures;
	// Active players
	private Map<Integer, Player> players;
	// The cells that are occupied by some players
	private Set<Integer> occupied;
	// The current snapshot
	private WorldSnapshot snapshot;
	
	// World change listener
	private MazeWorldListener listener;
	
	private MazeWorld() {
		idCounter = new AtomicInteger(0);
		status = GameStatus.IDLE;
		treasures = new ArrayList<>();
		players = new HashMap<>();
		occupied = new HashSet<>();
	}
	
	public static MazeWorld instance() {
		if(_inst == null) _inst = new MazeWorld();
		
		return _inst;
	}
	
	/**
	 * Configure the world.
	 * @param length
	 * @param width
	 * @param totalTreasures
	 */
	public void config(int length, int width, int totalTreasures) {
		this.length = length;
		this.width = width;
		this.totalTreasures = totalTreasures;
		logger.info("[CONFIG_GAME] Length: {}, Width: {}, Treasures: {}", length, width, totalTreasures);
	}
	
	/**
	 * This happens when game snapshot is pushed from primary server to back up server.
	 * @param snapshot
	 */
	public void loadSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
		
		// Load players and update occupied cells
		players = new HashMap<>();
		occupied.clear();
		for(Player player : snapshot.getPlayers()) {
			players.put(player.getId(), player);
			int position = player.getyPos()*length + player.getxPos();
			occupied.add(position);
		}
		
		// Load treasures
		treasures = new ArrayList<>();
		treasures.addAll(snapshot.getTreasures());
		
		logger.info("[LOAD_SNAPSHOT] Players: {}, Treasures: {}", players.size(), treasures.size());
	}
	
	/**
	 * Check whether one player is sitting on a treasure
	 * @param player
	 * @param treasure
	 * @return
	 */
	private boolean match(Player player, Treasure treasure) {
		return player.getxPos() == treasure.getxPos() &&
				player.getyPos() == treasure.getyPos();
	}
	
	/**
	 * Start the game, generate all the treasures.
	 */
	public synchronized void startGame() {
		// Generate all the treasures
		for(int i=0; i<totalTreasures; i++) {
			int xPos = MazeUtility.randInt(0, length);
			int yPos = MazeUtility.randInt(0, width);
			treasures.add(new Treasure(xPos, yPos));
		}
		
		// Find the lucky players who are born on treasures
		for(Player player : players.values()) {
			int newTreasures = 0;
			Iterator<Treasure> it = treasures.iterator();
			while(it.hasNext()) {
				Treasure treasure = it.next();
				if(match(player, treasure)) {
					newTreasures++;
					it.remove();
				}
			}
			
			player.addTreasures(newTreasures);
		}
		
		status = GameStatus.RUNNING;
		startTime = System.currentTimeMillis();
		
		// Update snapshot
		updateSnapshot();
		if(listener != null) {
			listener.onWorldChanged(status, snapshot);
		}
		
		logger.info("[START_GAME]");
	}
	
	/**
	 * Is the game already finished
	 * @return
	 */
	public boolean isGameOver() {
		return treasures.isEmpty() || players.isEmpty();
	}
	
	/**
	 * End the game, clean up the world.
	 */
	public synchronized void endGame() {
		// Clean up
		treasures.clear();
		players.clear();
		occupied.clear();
		status = GameStatus.IDLE;
		
		// Update snapshot
		updateSnapshot();
		if(listener != null) {
			listener.onWorldChanged(status, snapshot);
		}
		
		logger.info("[END_GAME]");
	}
	
	/**
	 * Generate a snapshot of the current world.
	 * @return
	 */
	private synchronized void updateSnapshot() {
		// Get snapshot of all active players
		List<Player> currentPlayers = new ArrayList<>();
		for(Player player : players.values()) {
			try {
				currentPlayers.add((Player)player.clone());
			} catch(Exception ex) {
				logger.error("Fail to clone player");
			}
		}
		
		// Get snapshot of all available treasures
		List<Treasure> currentTreasures = new ArrayList<>();
		currentTreasures.addAll(treasures);
		
		snapshot = new WorldSnapshot(currentPlayers, currentTreasures);
	}
	
	/**
	 * Add a new player
	 * @return player id
	 */
	public synchronized Integer addPlayer() throws JoinGameFailException {
		if(status != GameStatus.WAITING) {
			throw new JoinGameFailException("Game Already Started!");
		}
		
		int id = idCounter.incrementAndGet();
		int xPos = 0;
		int yPos = 0;
		int birthplace = 0;
		
		// Try maximum 10 times or declare server too crowded
		for(int i=0; i<10; i++) {
			xPos = MazeUtility.randInt(0, length);
			yPos = MazeUtility.randInt(0, width);
			birthplace = yPos*length + xPos;
			
			if(!occupied.contains(birthplace)) {
				break;
			}
		}
		
		if(occupied.contains(birthplace)) {
			throw new JoinGameFailException("Server Too Crowded!");
		} else {
			occupied.add(birthplace);
		}
		
		Player player = new Player(id, xPos, yPos, 0);
		players.put(id, player);
		
		logger.info("[ADD_PLAYER] ID: {}, xPos: {}, yPos: {}", id, xPos, yPos);
		return id;
	}
	
	/**
	 * Remove a player that has quit the game.
	 * @param id
	 * @return
	 */
	public synchronized Player removePlayer(int id) {
		// Free the occupied cell
		Player player = players.remove(id);
		if(player != null) {
			int position = player.getyPos()*length + player.getxPos();
			occupied.remove(position);
			
			// Update snapshot
			updateSnapshot();
			if(listener != null) {
				listener.onWorldChanged(status, snapshot);
			}
			
			logger.info("[REMOVE_PLAYER] ID: {}", id);
		}
		
		return player;
	}
	
	/**
	 * Move a player for one step, return the new treasures collected.
	 * @param id
	 * @param dir
	 * @return
	 * @throws InvalidMoveException
	 */
	public synchronized int movePlayer(int id, Direction dir) throws InvalidMoveException {
		if(status != GameStatus.RUNNING) {
			throw new InvalidMoveException("Game Not Started Yet!");
		}
		
		Player player = players.get(id);
		if(player == null) {
			throw new InvalidMoveException("Player Does Not Exist!");
		}
		
		// No need to do any thing if the player wants to stay
		if(dir == Direction.NO_MOVE) {
			return 0;
		}
		
		int nextXPos = player.getxPos();
		int nextYPos = player.getyPos();
		switch(dir) {
			case UP:
				nextYPos--; break;
			case DOWN:
				nextYPos++; break;
			case LEFT:
				nextXPos--; break;
			case RIGHT:
				nextXPos++; break;
			default:
				break;
		}
		
		// Check if the new position is valid
		if(nextYPos < 0 || nextYPos >= width || nextXPos < 0 || nextXPos >= length) {
			throw new InvalidMoveException("You Cannot Cross The Wall!");
		}
		
		// Check if the new position has been occupied
		int newPosition = nextYPos*length + nextXPos;
		if(occupied.contains(newPosition)) {
			throw new InvalidMoveException("Position Already Occupied!");
		}
		
		// Move success, update position
		int oldPosition = player.getyPos()*length + player.getxPos();
		occupied.remove(oldPosition);
		occupied.add(newPosition);
		
		player.setxPos(nextXPos);
		player.setyPos(nextYPos);
		
		// Time to collect treasures
		int newTreasures = 0;
		Iterator<Treasure> it = treasures.iterator();
		while(it.hasNext()) {
			Treasure treasure = it.next();
			if(treasure.getxPos() == nextXPos && treasure.getyPos() == nextYPos) {
				newTreasures++;
				it.remove();
			}
		}
		
		player.addTreasures(newTreasures);
		
		// Update snapshot
		updateSnapshot();
		if(listener != null) {
			listener.onWorldChanged(status, snapshot);
		}
		
		logger.info("[PLAYER_MOVE] ID: {}, xPos: {}, yPos: {}, Treasure: {}", 
				id, player.getxPos(), player.getyPos(), player.getTreasures());
		return newTreasures;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getTotalTreasures() {
		return totalTreasures;
	}

	public void setTotalTreasures(int totalTreasures) {
		this.totalTreasures = totalTreasures;
	}

	public WorldSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
	}

	public MazeWorldListener getListener() {
		return listener;
	}

	public void setListener(MazeWorldListener listener) {
		this.listener = listener;
	}

}
