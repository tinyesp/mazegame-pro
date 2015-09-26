package mazegame.message.system;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;

/**
 * Backup server changed message. This message is broadcast by server to all connections
 * whenever a new backup server is created.
 * 
 * @author chenweiming
 *
 */
public class BackupChangedMsg extends MazeMessage {
	private String backupAddr;
	private int backupPort;

	public BackupChangedMsg() {
		this(null, 0);
	}
	
	public BackupChangedMsg(String backupAddr, int backupPort) {
		super(MazeMsgType.BACKUP_CHANGED);
		this.backupAddr = backupAddr;
		this.backupPort = backupPort;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent content first
		super.toStream(mdout);
		
		// Write this message content
		mdout.writeString(backupAddr);
		mdout.writeInt(backupPort);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent 
		super.fromStream(mdin);
		
		// Load this message
		backupAddr = mdin.readString();
		backupPort = mdin.readInt();
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
