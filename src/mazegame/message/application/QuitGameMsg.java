package mazegame.message.application;

import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;

/**
 * Quit game request.
 * 
 * @author chenweiming
 *
 */
public class QuitGameMsg extends MazeMessage {
	
	public QuitGameMsg() {
		super(MazeMsgType.QUIT_GAME);
	}
	
}
