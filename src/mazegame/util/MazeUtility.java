package mazegame.util;

import java.util.Random;

public class MazeUtility {
	private static Random rd = new Random();
	
	/**
	 * Generate a random number in the specified range
	 * @param min	inclusive
	 * @param max 	exclusive
	 * @return
	 */
	public static int randInt(int min, int max) {
		return rd.nextInt(max - min) + min;
	}

}
