package mazegame.message.system;

import java.io.IOException;

import mazegame.data.WorldSnapshot;
import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;
import mazegame.server.GameStatus;

/**
 * This message to sent from primary server to backup server whenever the game state
 * changed.
 * 
 * @author chenweiming
 *
 */
public class SyncMsg extends MazeMessage {
	// The primary player id, when primary server down, backup server 
	// can remove this player
	private int primaryId;
	// The game status
	private GameStatus gameStatus;
	// The current world snapshot
	private WorldSnapshot snapshot;
	
	public SyncMsg() {
		this(-1, null, null);
	}
	
	public SyncMsg(int primaryId, GameStatus gameStatus, WorldSnapshot snapshot) {
		super(MazeMsgType.SYNC_MESSAGE);
		this.primaryId = primaryId;
		this.gameStatus = gameStatus;
		this.snapshot = snapshot;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent
		super.toStream(mdout);
		
		// Write this
		mdout.writeInt(primaryId);
		mdout.writeInt(gameStatus.getCode());
		snapshot.toStream(mdout);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent
		super.fromStream(mdin);
		
		// Load this
		primaryId = mdin.readInt();
		gameStatus = GameStatus.fromCode(mdin.readInt());
		snapshot = new WorldSnapshot();
		snapshot.fromStream(mdin);
	}

	public int getPrimaryId() {
		return primaryId;
	}

	public void setPrimaryId(int primaryId) {
		this.primaryId = primaryId;
	}

	public GameStatus getGameStatus() {
		return gameStatus;
	}

	public void setGameStatus(GameStatus gameStatus) {
		this.gameStatus = gameStatus;
	}

	public WorldSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
	}

}
