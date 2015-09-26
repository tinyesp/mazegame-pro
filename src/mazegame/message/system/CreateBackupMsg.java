package mazegame.message.system;

import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;

/**
 * Require one player to set up a backup server.
 * 
 * @author WEIMING
 *
 */
public class CreateBackupMsg extends MazeMessage {
	
	public CreateBackupMsg() {
		super(MazeMsgType.CREATE_BACKUP);
	}

}
