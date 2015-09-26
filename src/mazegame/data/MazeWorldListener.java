package mazegame.data;

import mazegame.server.GameStatus;

/**
 * Listen for any changes in the world.
 * 
 * @author WEIMING
 *
 */
public interface MazeWorldListener {
	
	void onWorldChanged(GameStatus status, WorldSnapshot snapshot);

}
