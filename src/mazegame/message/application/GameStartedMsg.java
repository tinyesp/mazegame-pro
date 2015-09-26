package mazegame.message.application;

import java.io.IOException;

import mazegame.data.WorldSnapshot;
import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;

/**
 * This message is broadcasted to all players when game is started.
 * 
 * @author chenweiming
 *
 */
public class GameStartedMsg extends MazeMessage {
	// The current world snapshot
	private WorldSnapshot snapshot;
	
	public GameStartedMsg() {
		this(null);
	}
	
	public GameStartedMsg(WorldSnapshot snapshot) {
		super(MazeMsgType.GAME_STARTED);
		this.snapshot = snapshot;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write out parent message
		super.toStream(mdout);
		
		// Write out snapshot
		snapshot.toStream(mdout);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent message
		super.fromStream(mdin);
		
		// Load snapshot
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
