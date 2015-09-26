package mazegame.player;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import mazegame.data.MazeWorld;
import mazegame.message.application.GameEndedMsg;
import mazegame.message.application.GameStartedMsg;
import mazegame.message.application.JoinGameRes;
import mazegame.message.application.MoveRequestRes;
import mazegame.message.system.BackupChangedMsg;
import mazegame.message.system.CreateBackupMsg;
import mazegame.message.system.CreateBackupRes;
import mazegame.message.system.RejoinMsg;
import mazegame.message.system.RejoinRes;
import mazegame.message.system.SyncMsg;
import mazegame.messages.MazeMessage;
import mazegame.node.MNodeType;
import mazegame.player.ui.CommandPlayer;
import mazegame.player.ui.IPlayer;
import mazegame.server.MazeServer;
import mazegame.server.MazeServerHandler;

@Sharable
public class MazeClientHandler extends SimpleChannelInboundHandler<MazeMessage> {
	private static Logger logger = LoggerFactory.getLogger(MazeClientHandler.class);
	
	// Singleton
	private static MazeClientHandler _inst;
	
	// Player type
	private MNodeType nodeType = MNodeType.CLIENT;
	// Client socket channel
	private ChannelHandlerContext ctx;
	// Player UI
	private IPlayer player;
	
	// Primary port, +1 will be used to create backup
	private int primaryPort = MazeClient.port;
	// Back up server address
	private String backupAddr = "";
	// Back up server listening port
	private int backupPort = MazeClient.port;
	
	public static MazeClientHandler instance() {
		if(_inst == null) _inst = new MazeClientHandler();
		
		return _inst;
	}
	
	public MNodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(MNodeType nodeType) {
		this.nodeType = nodeType;
	}

	/**
	 * Server is connected.
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
		
		String serverAddr = ((InetSocketAddress) ctx.channel().remoteAddress()).toString();
		logger.info("[SERVER_CONNECTED] {}", serverAddr);
		
		if(player == null) {	// Set up player UI, first connect
			player = new CommandPlayer();
			player.init();
		} else {	// Reconnect
			RejoinMsg rejoin = new RejoinMsg(nodeType, player.getId());
			writeToChannel(rejoin);
		}
	}
	
	/**
	 * Server is disconnected.
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		logger.error("[SERVER_DISCONNECTED] Use Backup {}:{}", backupAddr, backupPort);
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// Interrupt player
				player.onError("Server lost, reconnecting...");
				primaryPort = backupPort;
		    	
		    	// Promote backup player to primary
		    	if(nodeType == MNodeType.BACKUP) {
		    		nodeType = MNodeType.PRIMARY;
		    		logger.info("[NODE_TYPE_CHANGE]: Promote Backup Player To Primary");
		    	}
		    	
		    	// Reconnect
		        try {
		        	MazeClient.connect(backupAddr, backupPort);
		        } catch(Exception ex) {
		        	logger.error("Fail to connect backup server", ex);
		        	player.onError("Fail to reconnect...");
		        }
			}
		});
		
		thread.start();
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, MazeMessage msg) throws Exception {
		logger.info("[RECEIVE_MESSAGE]: {}", msg.getType());
		
		switch(msg.getType()) {
			case JOIN_GAME_RESPONSE:
				player.onJoinGameReplied((JoinGameRes)msg);
				break;
			case GAME_STARTED:
				player.onGameStarted((GameStartedMsg)msg);
				break;
			case MOVE_REQUEST_RESPONSE:
				player.onMoveReplied((MoveRequestRes)msg);
				break;
			case GAME_ENDED:
				player.onGameEnded((GameEndedMsg)msg);
				break;
			case BACKUP_CHANGED:
				handleBackupChangedMsg((BackupChangedMsg)msg);
				break;
			case REJOIN_RESPONSE:
				handleRejoinRes((RejoinRes)msg);
				break;
			case CREATE_BACKUP:
				handleCreateBackupMessage((CreateBackupMsg)msg);
				break;
			case SYNC_MESSAGE:
				handleSyncMessage((SyncMsg)msg);
				break;
		 	default:
		 		logger.error("Unknown message received: {}", msg.getType());
		 		break;
		}
	}
	
	private void handleBackupChangedMsg(BackupChangedMsg backupChanged) {
		backupAddr = backupChanged.getBackupAddr();
		backupPort = backupChanged.getBackupPort();
		logger.info("[UPDATE_BACKUP]: {}:{}", backupAddr, backupPort);
	}
	
	private void handleRejoinRes(RejoinRes rejoin) {
		if(rejoin.getStatus()) {
			backupAddr = rejoin.getBackupAddr();
			backupPort = rejoin.getBackupPort();
			logger.info("[UPDATE_BACKUP]: {}:{}", backupAddr, backupPort);
		}
		
		// Hand over to player
		player.onRejoinReplied(rejoin);
	}
	
	private void handleCreateBackupMessage(CreateBackupMsg createBackup) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// Set a different backup port to enable testing on same machine
				int port = primaryPort + 1;
				logger.info("[CREATE_BACKUP] Listening Port: {}", port);
				
				// Promote player
				nodeType = MNodeType.BACKUP;
				logger.info("[NODE_TYPE_CHANGE]: Promote Client Player To Backup");
				
				// Configure map
				MazeWorld.instance().config(player.getLength(), player.getWidth(), player.getTotalTreasures());
				
				// Send response
				CreateBackupRes response = new CreateBackupRes();
				response.setStatus(true);
				response.setBackupPort(port);
				writeToChannel(response);
				
				try {
					// Block
					MazeServer.init(port, true);
				} catch(Exception ex) {
					logger.error("[BACKUP_DOWN]", ex);
				}
			}
		});
		
		thread.start();
	}
	
	private void handleSyncMessage(SyncMsg sync) {
		// Update backup server
		MazeServerHandler.primaryId = sync.getPrimaryId();
		MazeWorld.instance().setStatus(sync.getGameStatus());
		MazeWorld.instance().loadSnapshot(sync.getSnapshot());
		logger.info("[SYNC_BACKUP] Primay ID: {}, Status: {}", sync.getPrimaryId(), sync.getGameStatus());
	}

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    	logger.error("[CHANNEL_EXCEPTION] {}", cause.getMessage());
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
