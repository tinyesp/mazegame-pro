package mazegame.message.system;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;
import mazegame.node.MNodeType;

/**
 * Message sent by a player to backup server to rejoin game after the primary server is
 * down.
 * 
 * @author chenweiming
 *
 */
public class RejoinMsg extends MazeMessage {
	// Player type
	private MNodeType nodeType;
	// Player id
	private int id;

	public RejoinMsg() {
		this(null, -1);
	}
	
	public RejoinMsg(MNodeType nodeType, int id) {
		super(MazeMsgType.REJOIN_MESSAGE);
		this.nodeType = nodeType;
		this.id = id;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent first
		super.toStream(mdout);
		
		// Write this message
		mdout.writeInt(nodeType.getCode());
		mdout.writeInt(id);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent first
		super.fromStream(mdin);
		
		// Load this
		nodeType = MNodeType.fromCode(mdin.readInt());
		id = mdin.readInt();
	}

	public MNodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(MNodeType nodeType) {
		this.nodeType = nodeType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
