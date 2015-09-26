package mazegame.player.ui;

import mazegame.message.application.GameEndedMsg;
import mazegame.message.application.GameStartedMsg;
import mazegame.message.application.JoinGameRes;
import mazegame.message.application.MoveRequestRes;
import mazegame.message.system.RejoinRes;

/**
 * 
 * @author chenweiming
 * @author gejun
 *
 */
public interface IPlayer {
	
	/**
	 * Initialize the UI
	 */
    public void init();
    
    /**
     * Get player id.
     * @return
     */
    int getId();
    
    /**
     * Get map length, from left to right
     * @return
     */
    int getLength();
    
    /**
     * Get map width, from up to down
     * @return
     */
    int getWidth();
    
    /**
     * Get total available treasures
     * @return
     */
    int getTotalTreasures();
    
    /**
     * Notified when server has replied the join game request.
     */
    void onJoinGameReplied(JoinGameRes joinResponse);
    
    /**
     * Notified when the game has started.
     */
    void onGameStarted(GameStartedMsg gameStarted);
    
    /**
     * Notified when the server has replied previous move request.
     */
    void onMoveReplied(MoveRequestRes moveResponse);
    
    /**
     * Notified when the game has ended.
     */
    void onGameEnded(GameEndedMsg gameEnded);
    
    /**
     * Notified when rejoin replied.
     * @param rejoin
     */
    void onRejoinReplied(RejoinRes rejoin);
    
    /**
     * Notified when the system get errors
     */
    void onError(String message);
    
}
