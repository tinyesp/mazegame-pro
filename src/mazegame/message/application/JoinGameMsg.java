package mazegame.message.application;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;
import mazegame.node.MNodeType;

/**
 * Join game request message.
 * 
 * @author chenweiming
 *
 */
public class JoinGameMsg extends MazeMessage {
	// Player type, the server will pay special attention 
	// to primary player 
	private MNodeType nodeType;
	
	public JoinGameMsg() {
		this(null);
	}
	
	public JoinGameMsg(MNodeType nodeType) {
		super(MazeMsgType.JOIN_GAME);
		this.nodeType = nodeType;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent
		super.toStream(mdout);
		
		// Write this
		mdout.writeInt(nodeType.getCode());
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent 
		super.fromStream(mdin);
		
		// Load this
		nodeType = MNodeType.fromCode(mdin.readInt());
	}

	public MNodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(MNodeType nodeType) {
		this.nodeType = nodeType;
	}

}
