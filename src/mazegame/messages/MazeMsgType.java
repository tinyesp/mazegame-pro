package mazegame.messages;

import java.util.HashMap;
import java.util.Map;

import mazegame.message.application.GameEndedMsg;
import mazegame.message.application.GameStartedMsg;
import mazegame.message.application.JoinGameMsg;
import mazegame.message.application.JoinGameRes;
import mazegame.message.application.MoveRequestMsg;
import mazegame.message.application.MoveRequestRes;
import mazegame.message.application.QuitGameMsg;
import mazegame.message.system.BackupChangedMsg;
import mazegame.message.system.CreateBackupMsg;
import mazegame.message.system.CreateBackupRes;
import mazegame.message.system.RejoinMsg;
import mazegame.message.system.RejoinRes;
import mazegame.message.system.SyncMsg;

/**
 * Message type map, used to automate decoding process.
 * 
 * @author chenweiming
 *
 */
public enum MazeMsgType {
	
	// Void message, base type
	VOID(0, MazeMessage.class.getName()),
	// Response
	RESPONSE(1, ResponseMsg.class.getName()),
	// Join game message
	JOIN_GAME(2, JoinGameMsg.class.getName()),
	// Join game response
	JOIN_GAME_RESPONSE(3, JoinGameRes.class.getName()),
	// Game started 
	GAME_STARTED(4, GameStartedMsg.class.getName()),
	// Move request
	MOVE_REQUEST(5, MoveRequestMsg.class.getName()),
	// Move request response
	MOVE_REQUEST_RESPONSE(6, MoveRequestRes.class.getName()),
	// Quit game
	QUIT_GAME(7, QuitGameMsg.class.getName()),
	// Game ended
	GAME_ENDED(8, GameEndedMsg.class.getName()),
	// Rejoin message
	REJOIN_MESSAGE(9, RejoinMsg.class.getName()),
	// Rejoin response
	REJOIN_RESPONSE(10, RejoinRes.class.getName()),
	// Create back up message
	CREATE_BACKUP(11, CreateBackupMsg.class.getName()),
	// Create back up response
	CREATE_BACKUP_RESPONSE(12, CreateBackupRes.class.getName()),
	// Back up changed message
	BACKUP_CHANGED(13, BackupChangedMsg.class.getName()),
	// Push primary server state to back server
	SYNC_MESSAGE(14, SyncMsg.class.getName()) 
	;
	
	private int code;
	private String className;
	
	private static Map<Integer, MazeMsgType> typeMap;
	
	static {
		typeMap = new HashMap<>();
		for(MazeMsgType type : MazeMsgType.values()) {
			typeMap.put(type.code, type);
		}
	}
	
	MazeMsgType(int code, String className) {
		this.code = code;
		this.className = className;
	}

	public int getCode() {
		return code;
	}
	
	public String getClassName() {
		return className;
	}

	public static MazeMsgType fromCode(int code) {
		return typeMap.get(code);
	}
	
}
