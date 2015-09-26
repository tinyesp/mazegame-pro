package mazegame.message.application;

import java.io.IOException;

import mazegame.data.WorldSnapshot;
import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.ResponseMsg;

/**
 * Response to move request.
 * 
 * @author chenweiming
 *
 */
public class MoveRequestRes extends ResponseMsg {
	// New collected treasures
	private int newTreasures;
	// Current world snapshot
	private WorldSnapshot snapshot;

	public MoveRequestRes() {
		super(MazeMsgType.MOVE_REQUEST_RESPONSE);
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent
		super.toStream(mdout);
		
		// Write this
		mdout.writeInt(newTreasures);
		snapshot.toStream(mdout);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent
		super.fromStream(mdin);
		
		// Load this
		newTreasures = mdin.readInt();
		snapshot = new WorldSnapshot();
		snapshot.fromStream(mdin);
	}

	public int getNewTreasures() {
		return newTreasures;
	}

	public void setNewTreasures(int newTreasures) {
		this.newTreasures = newTreasures;
	}

	public WorldSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
	}

}
