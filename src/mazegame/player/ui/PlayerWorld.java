package mazegame.player.ui;

import mazegame.data.Player;
import mazegame.data.Treasure;
import mazegame.data.WorldSnapshot;

public class PlayerWorld {
	// Assigned user id
	private int id;
	// The map length, left to right
	private int length;
	// The map width, up to down
	private int width;
	// Total created treasures
	private int totalTreasures;
	
	// Player status
	private int xPos;
	private int yPos;
	private int treasures;
	
	// World snapshot
	private WorldSnapshot snapshot;
	
	// Map view
	private int[][] map;
	
	public PlayerWorld() {
		
	}
	
	public int[][] getMap() {
		if(map == null) {
			map = new int[width][length];
		}
		
		// Clean up the map
		for(int i=0; i<width; i++) {
			for(int j=0; j<length; j++) {
				map[i][j] = 0;
			}
		}
		
		// Render players
		for(Player player : snapshot.getPlayers()) {
			map[player.getyPos()][player.getxPos()] = - player.getId();
		}
		
		// Render treasures
		for(Treasure treasure : snapshot.getTreasures()) {
			map[treasure.getyPos()][treasure.getxPos()] += 1;
		}
		
		return map;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getxPos() {
		return xPos;
	}

	public void setxPos(int xPos) {
		this.xPos = xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	public int getTreasures() {
		return treasures;
	}

	public void setTreasures(int treasures) {
		this.treasures = treasures;
	}
	
	public void addTreasures(int treasures) {
		this.treasures += treasures;
	}

	public WorldSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
	}
}
