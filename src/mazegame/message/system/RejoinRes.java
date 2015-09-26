package mazegame.message.system;

import java.io.IOException;

import mazegame.data.WorldSnapshot;
import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.ResponseMsg;

/**
 * Response for a rejoin game message. Send from backup server to player.
 * 
 * @author chenweiming
 *
 */
public class RejoinRes extends ResponseMsg {
	// The new back up address, maybe not ready
	private String backupAddr;
	// The backup listening port, maybe not ready
	private int backupPort;
	// The current world snapshot
	private WorldSnapshot snapshot;
	
	public RejoinRes() {
		super(MazeMsgType.REJOIN_RESPONSE);
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent first
		super.toStream(mdout);
		
		// Write this message
		mdout.writeString(backupAddr);
		mdout.writeInt(backupPort);
		snapshot.toStream(mdout);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent
		super.fromStream(mdin);
		
		// Load this
		backupAddr = mdin.readString();
		backupPort = mdin.readInt();
		snapshot = new WorldSnapshot();
		snapshot.fromStream(mdin);
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

	public WorldSnapshot getSnapshot() {
		return snapshot;
	}

	public void setSnapshot(WorldSnapshot snapshot) {
		this.snapshot = snapshot;
	}

}
