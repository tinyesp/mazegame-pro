package mazegame.node;

import java.util.HashMap;
import java.util.Map;

public enum MNodeType {
	// Player runs primary server
	PRIMARY(0, "PRIMARY"),
	// Player runs backup server
	BACKUP(1, "BACKUP"),
	// Normal player who only play
	CLIENT(2, "PLAYER");
	
	private int code;
	private String name;
	
	private static Map<Integer, MNodeType> typeMap;
	
	static {
		typeMap = new HashMap<>();
		for(MNodeType type : MNodeType.values()) {
			typeMap.put(type.code, type);
		}
	}
	
	MNodeType(int code, String name) {
		this.code = code;
		this.name = name;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static MNodeType fromCode(int code) {
		return typeMap.get(code);
	}

}
