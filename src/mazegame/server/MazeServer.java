package mazegame.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import mazegame.data.MazeWorld;
import mazegame.io.MazeMessageDecoder;
import mazegame.io.MazeMessageEncoder;

public class MazeServer {
	private static Logger logger = LoggerFactory.getLogger(MazeServer.class);

	// Server listening port
	public static final int port = Integer.parseInt(System.getProperty("port", "9001"));

	public static void main(String[] args) {
		// Set up world
		MazeWorld.instance().config(5, 5, 3);

		try {
			init(port, false);
		} catch (Exception ex) {
			System.out.println("Create primary server fail...");
			System.exit(-1);
		}
	}

	public static void createServer(int length, int width, int treasures, int port) {
		logger.info("[START_NEW_SERVER] length: {}, width: {}, treasures: {}, port: {}...", length, width, treasures,
				port);
		// Set up world
		MazeWorld.instance().config(length, width, treasures);

		try {
			init(port, false);
		} catch (Exception ex) {
			System.out.println("Create primary server fail...");
			System.exit(-1);
		}
	}

	/**
	 * Set up a primary server or back up server.
	 * 
	 * @param port
	 * @param isSlave
	 * @throws InterruptedException
	 */
	public static void init(int port, boolean isBackup) throws InterruptedException {
		MazeServerHandler.isBackup = isBackup;

		if (isBackup) {
			logger.info("[CREATE_SERVER_BACKUP]: {}", port);
		} else {
			logger.info("[CREATE_SERVER_PRIMARY]: {}", port);
		}

		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							ChannelPipeline pipe = ch.pipeline();

							// Decoders
							pipe.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
							pipe.addLast("bytesDecoder", new ByteArrayDecoder());
							pipe.addLast("messageDecoder", new MazeMessageDecoder());

							// Encoder
							pipe.addLast("frameEncoder", new LengthFieldPrepender(4));
							pipe.addLast("bytesEncoder", new ByteArrayEncoder());
							pipe.addLast("messageEncoder", new MazeMessageEncoder());

							// Add Server handler
							pipe.addLast(new MazeServerHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

			// Bind and start to accept incoming connections.
			ChannelFuture f = b.bind(port).sync();
			// Wait until the server socket is closed.
			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

}
