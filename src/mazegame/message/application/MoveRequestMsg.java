package mazegame.message.application;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;

/**
 * Move request message.
 * 
 * @author chenweiming
 *
 */
public class MoveRequestMsg extends MazeMessage {
	// Move direction
	private Direction dir;
	
	public MoveRequestMsg() {
		this(null);
	}
	
	public MoveRequestMsg(Direction dir) {
		super(MazeMsgType.MOVE_REQUEST);
		this.dir = dir;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent
		super.toStream(mdout);
		
		// Write this
		mdout.writeInt(dir.getCode());
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent
		super.fromStream(mdin);
		
		// Load this
		dir = Direction.fromCode(mdin.readInt());
	}

	public Direction getDir() {
		return dir;
	}

	public void setDir(Direction dir) {
		this.dir = dir;
	}

}
