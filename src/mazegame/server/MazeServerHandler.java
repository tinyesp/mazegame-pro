package mazegame.server;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mazegame.data.MazeWorld;
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
import mazegame.messages.MazeMessage;
import mazegame.node.MNodeType;
import mazegame.server.exceptions.InvalidMoveException;
import mazegame.server.exceptions.JoinGameFailException;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * Handles a server-side channel for each player.
 */
public class MazeServerHandler extends SimpleChannelInboundHandler<MazeMessage> {
	private static Logger logger = LoggerFactory.getLogger(MazeServerHandler.class);
	
	// The waiting time before game start after the first player joins
	public static final long START_GAME_WAIT = 60*1000L;	// TODO: Change back to 20s
	
	// Whether is backup
	public static boolean isBackup;
	// The primary player id, used to remove this player when primary server down,
	// available on both primary and backup server
	public static int primaryId = -1;
	// The backup player id, only available on primary server to detect backup down
	public static int backupId = -1;
	// The backup server address
	public static String backupAddr = "";
	// The backup server listening port
	public static int backupPort = MazeServer.port;
	
	// All connected and active channels
	private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	// The joined player channel map
	private static Map<Integer, ChannelHandlerContext> ctxMap = new ConcurrentHashMap<>();
	// The shared world
	private static MazeWorld mazeWorld = MazeWorld.instance();
	
	// This client channel connection
	private ChannelHandlerContext ctx;
	// This player assigned id
	private int id = -1;
	
	/**
     * Put the game to waiting status and schedule to start in 20 seconds.
     */
    public synchronized static void boostrapGame() {
    	// Not the first player
    	if(mazeWorld.getStatus() != GameStatus.IDLE) {
    		return;
    	}
    	
    	// Schedule to start the game
    	TimerTask startTask = new TimerTask() {
			@Override
			public void run() {
				mazeWorld.startGame();
				
				// Broadcast game started message
				GameStartedMsg gameStarted = new GameStartedMsg(mazeWorld.getSnapshot());
				broadCastPlayers(gameStarted);
				logger.info("[GAME_STARTED] Players: {}", ctxMap.size());
			}
		};
		
		Timer timer = new Timer();
		timer.schedule(startTask, START_GAME_WAIT);
		mazeWorld.setStatus(GameStatus.WAITING);
		
		// Looking for a backup server
		lookforBackup();
		
		logger.info("[SCHEDULE_START_GAME]");
    }
    
    /**
     * Check whether the game is over and terminate it.
     */
    public synchronized static void checkGameFinish() {
    	if(mazeWorld.getStatus() != GameStatus.RUNNING) {
    		return;
    	}
    	
    	if(mazeWorld.isGameOver()) {
    		mazeWorld.endGame();
    		
    		// Broadcast game ended message
    		GameEndedMsg gameEnded = new GameEndedMsg(mazeWorld.getSnapshot());
    		broadCastPlayers(gameEnded);
    		logger.info("[GAME_ENDED] Players: {}", ctxMap.size());
    	}
    }
    
    /**
     * Promote back up server to primary sever, this happens when the primary sever is 
     * crashed and triggered by first rejoin message.
     */
    public synchronized static void promote() {
    	if(!isBackup) {
    		return;
    	}
    	
    	logger.info("[SERVER_TYPE_CHANGE] Promote Backup Server To Primary");
    	isBackup = false;
    	
    	// Remove primary player and wait for the new promoted primary player to join
    	mazeWorld.removePlayer(primaryId);
    	primaryId = -1;
    	
    	// Create another backup
    	lookforBackup();
    }
    
    /**
     * Look for a backup server from the active players
     */
    public static void lookforBackup() {
    	Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// Wait for at least 2 player joins
				while(ctxMap.size() < 2) {
					try {
						Thread.sleep(100);
					} catch(Exception ex) {
						logger.error("Create backup waiting thread interrupted");
					}
				}
				
				// Select any joined players besides the primary
				for(Integer id : ctxMap.keySet()) {
					if(id != primaryId) {
						// Create the backup server
						CreateBackupMsg createBackup = new CreateBackupMsg();
						ctxMap.get(id).writeAndFlush(createBackup);
						logger.info("[SEND_CREATE_BACKUP] ID: {}", id);
						break;
					}
				}
			}
    	});
    	
    	thread.start();
    	logger.info("[LOOKFOR_BACKUP_SERVER] Looking for a backup server...");
    }
    
    /**
     * Broadcast a message to all joined players.
     * @param msg
     */
    public static void broadCastPlayers(MazeMessage msg) {
    	logger.info("[BROADCAST_PLAYERS] {}", msg.getType());
    	for(ChannelHandlerContext ctx : ctxMap.values()) {
    		ctx.writeAndFlush(msg);
    	}
    }
    
    /**
     * Broadcast a message to connections. Used for system messages, e.g. update
     * backup server information.
     * @param msg
     */
    public static void broadCastConnections(MazeMessage msg) {
    	logger.info("[BROADCAST_CONNECTIONS] {}", msg.getType());
    	for(Channel channel : channels) {
    		channel.writeAndFlush(msg);
    	}
    }
	
    /**
     * A new player is connected.
     * @param ctx
     * @throws Exception
     */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
		channels.add(ctx.channel());
		String address = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostName();
		logger.info("[NEW_PLAYER_CONNECTED] {}", address);
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("[PLAYER_DISCONNECTED] {}", id);
		
		// Remove this player
		mazeWorld.removePlayer(id);
		ctxMap.remove(id);
		checkGameFinish();
		
		// Check whether it is backup
		if(id == backupId) {
			logger.error("[BACKUP_DISCONNECTED] {}", id);
			mazeWorld.setListener(null);
			lookforBackup();
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MazeMessage msg)
			throws Exception {
		logger.info("[RECEIVE_MESSAGE]: {}", msg.getType());
		
		switch(msg.getType()) {
			case JOIN_GAME:
				processJoinGameRequest((JoinGameMsg)msg);
				break;
			case MOVE_REQUEST:
				processMoveRequest((MoveRequestMsg)msg);
				break;
			case QUIT_GAME:
				processQuitGameRequest((QuitGameMsg)msg);
				break;
			case CREATE_BACKUP_RESPONSE:
				processCreateBackupRes((CreateBackupRes)msg);
				break;
			case REJOIN_MESSAGE:
				processRejoinRequest((RejoinMsg)msg);
				break;
			default:
				logger.error("Unknown message received: {}", msg.getType());
				break;
		}
	}
	
	/**
	 * Process join game request.
	 * @param joinRequest
	 */
	private void processJoinGameRequest(JoinGameMsg joinRequest) {
		// Schedule start the game if this is the first player
		boostrapGame();
		
		JoinGameRes response = new JoinGameRes();
		response.setBackupAddr(backupAddr);
		response.setBackupPort(backupPort);
		
		try {
			id = mazeWorld.addPlayer();
			ctxMap.put(id, ctx);
			
			if(joinRequest.getNodeType() == MNodeType.PRIMARY) {
				primaryId = id;
			}
			
			// Join game success
			response.setStatus(true);
			response.setId(id);
			response.setLength(mazeWorld.getLength());
			response.setWidth(mazeWorld.getWidth());
			response.setTotalTreasures(mazeWorld.getTotalTreasures());
		} catch(JoinGameFailException ex) {
			logger.error("Player fail to join game", ex);
			response.setStatus(false);
			response.setMessage(ex.getMessage());
		}
		
		// Send back reply
		writeToChannel(response);
		logger.info("[PLAYER_JOINED] ID: {}", id);
	}
	
	/**
	 * Process move request.
	 * @param moveRequest
	 */
	private void processMoveRequest(MoveRequestMsg moveRequest) {
		MoveRequestRes response = new MoveRequestRes();
		try {
			int newTreasures = mazeWorld.movePlayer(id, moveRequest.getDir());
			
			// Move success
			response.setStatus(true);
			response.setNewTreasures(newTreasures);
			response.setSnapshot(mazeWorld.getSnapshot());
		} catch(InvalidMoveException ex) {
			logger.error("Player fail to move： {}", ex.getMessage());
			response.setStatus(false);
			response.setMessage(ex.getMessage());
			response.setSnapshot(mazeWorld.getSnapshot());
		}
		
		// Send out the response
		ctx.writeAndFlush(response);
		logger.info("[PLAYER_MOVED] {}-{}", id, moveRequest.getDir());
		
		// Check whether game is over
		checkGameFinish();
	}
	
	/**
	 * Process quit game message.
	 * @param ctx
	 * @param moveRequest
	 */
	private void processQuitGameRequest(QuitGameMsg quitRequest) {
		// Remove the player
		mazeWorld.removePlayer(id);
		ctxMap.remove(id);
		logger.info("[PLAYER_QUIT] {}", id);
		
		// Check whether game is over
		checkGameFinish();
	}
	
	/**
	 * Process create backup response message.
	 * @param backupResponse
	 */
	private void processCreateBackupRes(CreateBackupRes backupResponse) {
		if(!backupResponse.getStatus()) {
			logger.info("[CREATE_BACKUP_FAIL]: {}", id);
			lookforBackup();
			return;
		}
		
		// Update backup address and broadcast
		backupId = id;
		backupAddr = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostName();;
		backupPort = backupResponse.getBackupPort();
		logger.info("[CREATE_BACKUP_SUCCESS]: {}:{}", backupAddr, backupPort);
		
		// Send the first sync message
		SyncMsg sync = new SyncMsg();
		sync.setPrimaryId(primaryId);
		sync.setGameStatus(mazeWorld.getStatus());
		sync.setSnapshot(mazeWorld.getSnapshot());
		writeToChannel(sync);
		
		// Start sending sync message to this player for future world change
		BackupSynchronizer synchronizer = new BackupSynchronizer(ctx);
		mazeWorld.setListener(synchronizer);
		
		// Broadcast backup update message to all connections
		BackupChangedMsg backupChanged = new BackupChangedMsg(backupAddr, backupPort);
		broadCastConnections(backupChanged);
	}
	
	private void processRejoinRequest(RejoinMsg rejoin) {
		logger.info("[PLAYER_REJOIN] ID: {}, NodeType: {}", rejoin.getId(), rejoin.getNodeType());
		
		// Master is crashed, promote
		promote();
		
		// Restore the player
		id = rejoin.getId();
		ctxMap.put(id, ctx);
		
		// Update primary id (new promoted player)
		if(rejoin.getNodeType() == MNodeType.PRIMARY) {
			primaryId = id;
		}
		
		// Send back response
		RejoinRes response = new RejoinRes();
		response.setStatus(true);
		response.setBackupAddr(backupAddr);
		response.setBackupPort(backupPort);
		response.setSnapshot(mazeWorld.getSnapshot());
		writeToChannel(response);
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error("[CHANNEL_EXCEPTION] ID: {}, {}", id, cause.getMessage());
        // Close the connection
        ctx.close();
    }
    
    /**
     * Send out a message.
     * @param msg
     * @return
     * @throws Exception
     */
    public ChannelFuture writeToChannel(MazeMessage msg) {
    	logger.info("[SEND_MESSAGE] {}", msg.getType());
    	return ctx.writeAndFlush(msg);
    }
    
}
