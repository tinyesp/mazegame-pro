package mazegame.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import mazegame.data.MazeWorldListener;
import mazegame.data.WorldSnapshot;
import mazegame.message.system.SyncMsg;

public class BackupSynchronizer implements MazeWorldListener {
	private static Logger logger = LoggerFactory.getLogger(BackupSynchronizer.class);
	
	// Backup client socket channel
	private ChannelHandlerContext ctx;
	
	public BackupSynchronizer(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void onWorldChanged(GameStatus status, WorldSnapshot snapshot) {
		SyncMsg sync = new SyncMsg();
		sync.setPrimaryId(MazeServerHandler.primaryId);
		sync.setGameStatus(status);
		sync.setSnapshot(snapshot);
		
		if(ctx.channel().isActive()) {
			ctx.writeAndFlush(sync);
			logger.info("[SEND_SYNC_MESSAGE]");
		}
	}
    
}
