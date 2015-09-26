package mazegame.server;

import java.util.HashMap;
import java.util.Map;

public enum GameStatus {
	// The game is waiting for the first player to join
	IDLE(0),
	// The game is going to start
	WAITING(1),
	// The game is already running
	RUNNING(2);
	
	private int code;
	
	private static Map<Integer, GameStatus> typeMap;
	
	static {
		typeMap = new HashMap<>();
		for(GameStatus type : GameStatus.values()) {
			typeMap.put(type.code, type);
		}
	}
	
	GameStatus(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
	
	public static GameStatus fromCode(int code) {
		return typeMap.get(code);
	}
}
