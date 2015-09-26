package mazegame.message.application;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.ResponseMsg;

/**
 * Response to a join game request message.
 * 
 * @author chenweiming
 *
 */
public class JoinGameRes extends ResponseMsg {
	// Assigned user id
	private int id;
	// The map length, left to right
	private int length;
	// The map width, up to down
	private int width;
	// Total created treasures
	private int totalTreasures;
	
	// The new back up address, maybe not ready
	private String backupAddr;
	// The backup listening port, maybe not ready
	private int backupPort;
	
	public JoinGameRes() {
		super(MazeMsgType.JOIN_GAME_RESPONSE);
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent
		super.toStream(mdout);
		
		// Write this
		mdout.writeInt(id);
		mdout.writeInt(length);
		mdout.writeInt(width);
		mdout.writeInt(totalTreasures);
		
		mdout.writeString(backupAddr);
		mdout.writeInt(backupPort);
	}

	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent
		super.fromStream(mdin);
		
		// Load this
		id = mdin.readInt();
		length = mdin.readInt();
		width = mdin.readInt();
		totalTreasures = mdin.readInt();
		
		backupAddr = mdin.readString();
		backupPort = mdin.readInt();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getTotalTreasures() {
		return totalTreasures;
	}

	public void setTotalTreasures(int totalTreasures) {
		this.totalTreasures = totalTreasures;
	}

	public String getBackupAddr() {
		return backupAddr;
	}

	public void setBackupAddr(String backupAddr) {
		this.backupAddr = backupAddr;
	}

	public int getBackupPort() {
		return backupPort;
	}

	public void setBackupPort(int backupPort) {
		this.backupPort = backupPort;
	}

}
