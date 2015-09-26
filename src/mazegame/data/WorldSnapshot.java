package mazegame.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;

/**
 * World snapshot。
 * 
 * @author WEIMING
 *
 */
public class WorldSnapshot {
	// Current active players
	private List<Player> players;
	// Current available treasures
	private List<Treasure> treasures;
	
	public WorldSnapshot() {
		players = new ArrayList<>();
		treasures = new ArrayList<>();
	}
	
	public WorldSnapshot(List<Player> players, List<Treasure> treasures) {
		this.players = players;
		this.treasures = treasures;
	}
	
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write out players
		mdout.writeInt(players.size());
		for(Player player : players) {
			writePlayer(mdout, player);
		}
		
		// Write out treasures
		mdout.writeInt(treasures.size());
		for(Treasure treasure : treasures) {
			writeTreasure(mdout, treasure);
		}
	}
	
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load players
		players = new ArrayList<>();
		
		int playerSize = mdin.readInt();
		for(int i=0; i<playerSize; i++) {
			Player player = readPlayer(mdin);
			players.add(player);
		}
		
		// Load treasures
		treasures = new ArrayList<>();
		
		int treasureSize = mdin.readInt();
		for(int i=0; i<treasureSize; i++) {
			Treasure treasure = readTreasure(mdin);
			treasures.add(treasure);
		}
	}
	
	private void writePlayer(MazeDataOutputStream mdout, Player player) throws IOException {
		mdout.writeInt(player.getId());
		mdout.writeInt(player.getxPos());
		mdout.writeInt(player.getyPos());
		mdout.writeInt(player.getTreasures());
	}
	
	private Player readPlayer(MazeDataInputStream mdin) throws IOException {
		int id = mdin.readInt();
		int xPos = mdin.readInt();
		int yPos = mdin.readInt();
		int treasures = mdin.readInt();
		
		return new Player(id, xPos, yPos, treasures);
	}
	
	private void writeTreasure(MazeDataOutputStream mdout, Treasure treasure) throws IOException {
		mdout.writeInt(treasure.getxPos());
		mdout.writeInt(treasure.getyPos());
	}
	
	private Treasure readTreasure(MazeDataInputStream mdin) throws IOException {
		int xPos = mdin.readInt();
		int yPos = mdin.readInt();
		return new Treasure(xPos, yPos);
	}

	public List<Player> getPlayers() {
		return players;
	}

	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	public List<Treasure> getTreasures() {
		return treasures;
	}

	public void setTreasures(List<Treasure> treasures) {
		this.treasures = treasures;
	}

}
