package mazegame.data;

public class Treasure {
	private int xPos;
	private int yPos;
	
	public Treasure(int xPos, int yPos) {
		this.xPos = xPos;
		this.yPos = yPos;
	}

	// No need setter as treasure never move
	public int getxPos() {
		return xPos;
	}

	// No need setter as treasure never move
	public int getyPos() {
		return yPos;
	}

}
