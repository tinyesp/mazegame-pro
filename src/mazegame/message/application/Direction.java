package mazegame.message.application;

import java.util.HashMap;
import java.util.Map;

public enum Direction {
	NO_MOVE(0, "STAY"),
	UP(1, "MOVE UP"),
	DOWN(2, "MOVE DOWN"),
	LEFT(3, "MOVE LEFT"),
	RIGHT(4, "MOVE RIGHT");
	
	private int code;
	private String message;
	
	private static Map<Integer, Direction> dirMap;
	
	static {
		dirMap = new HashMap<>();
		
		for(Direction dir : Direction.values()) {
			dirMap.put(dir.code, dir);
		}
	}
	
	Direction(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	public static Direction fromCode(int code) {
		return dirMap.get(code);
	}

}
