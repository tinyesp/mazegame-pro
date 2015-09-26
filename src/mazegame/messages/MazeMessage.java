package mazegame.messages;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
 
/**
 * This is the base class for all messages defined in a maze game. This class only contains 
 * a MazeMsgType field which will direct decoding process to select the correct message class. 
 * 
 * @author chenweiming
 *
 */
public class MazeMessage {
	// The message type
	private MazeMsgType type;
	
	public MazeMessage() {
		this(null);
	}
	
	public MazeMessage(MazeMsgType type) {
		this.type = type;
	}
	
	/**
	 * Encode the message content and write it to output stream.
	 * @return
	 */
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		mdout.writeInt(type.getCode());
	}
	
	/**
	 * Decode the message content back from the input stream. Type field is omitted
	 * as it shall be handled by upper layer.
	 */
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Nothing to read back
	}

	public MazeMsgType getType() {
		return type;
	}
	
}
