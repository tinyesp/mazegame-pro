package mazegame.data;

public class Player implements Cloneable {
	private int id;
	private int xPos;
	private int yPos;
	
	private int treasures;
	
	public Player(int id, int xPos, int yPos, int treasures) {
		this.id = id;
		this.xPos = xPos;
		this.yPos = yPos;
		this.treasures = treasures;
	}
	
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getxPos() {
		return xPos;
	}

	public void setxPos(int xPos) {
		this.xPos = xPos;
	}

	public int getyPos() {
		return yPos;
	}

	public void setyPos(int yPos) {
		this.yPos = yPos;
	}

	public int getTreasures() {
		return treasures;
	}

	public void setTreasures(int treasures) {
		this.treasures = treasures;
	}
	
	public void addTreasures(int treasures) {
		this.treasures += treasures;
	}

}
