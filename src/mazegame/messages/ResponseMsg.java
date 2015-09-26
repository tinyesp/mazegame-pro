package mazegame.messages;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;

/**
 * This is the base class for all response message.
 * 
 * @author WEIMING
 *
 */
public class ResponseMsg extends MazeMessage {
	// The action status
	private boolean status;
	// Short message
	private String message;

	public ResponseMsg() {
		this(MazeMsgType.RESPONSE);
	}
	
	public ResponseMsg(MazeMsgType type) {
		super(type);
		message = "";
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent content first
		super.toStream(mdout);
		
		// Write this message content
		mdout.writeBoolean(status);
		mdout.writeString(message);
	}

	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent message
		super.fromStream(mdin);
		
		// Load this message
		status = mdin.readBoolean();
		message = mdin.readString();
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
