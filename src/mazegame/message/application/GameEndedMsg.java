package mazegame.message.application;

import java.io.IOException;

import mazegame.data.WorldSnapshot;
import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;

/**
 * This message is broadcasted to all players when the game is ended.
 * 
 * @author chenweiming
 *
 */
public class GameEndedMsg extends MazeMessage {
	// The current world snapshot
	private WorldSnapshot snapshot;
	
	public GameEndedMsg() {
		this(null);
	}
	
	public GameEndedMsg(WorldSnapshot snapshot) {
		super(MazeMsgType.GAME_ENDED);
		this.snapshot = snapshot;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent
		super.toStream(mdout);
		
		// Write this
		snapshot.toStream(mdout);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent
		super.fromStream(mdin);
		
		// Load this
		snapshot = new WorldSnapshot();
		snapshot.fromStream(mdin);
	}

	public WorldSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
	}

}
