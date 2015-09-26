package mazegame.message.system;

import java.io.IOException;

import mazegame.io.MazeDataInputStream;
import mazegame.io.MazeDataOutputStream;
import mazegame.messages.MazeMsgType;
import mazegame.messages.ResponseMsg;

/**
 * Message returned back to server after the backup is created.
 * 
 * @author chenweiming
 *
 */
public class CreateBackupRes extends ResponseMsg {
	// The backup listening port
	private int backupPort;
	
	public CreateBackupRes() {
		this("", -1);
	}
	
	public CreateBackupRes(String backupAddr, int backupPort) {
		super(MazeMsgType.CREATE_BACKUP_RESPONSE);
		this.backupPort = backupPort;
	}
	
	@Override
	public void toStream(MazeDataOutputStream mdout) throws IOException {
		// Write parent first
		super.toStream(mdout);
		
		// Write this message
		mdout.writeInt(backupPort);
	}
	
	@Override
	public void fromStream(MazeDataInputStream mdin) throws IOException {
		// Load parent 
		super.fromStream(mdin);
		
		// Load this message
		backupPort = mdin.readInt();
	}

	public int getBackupPort() {
		return backupPort;
	}

	public void setBackupPort(int backupPort) {
		this.backupPort = backupPort;
	}
	
}
