package mazegame.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mazegame.io.MazeMessageDecoder;
import mazegame.io.MazeMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class MazeClient {
	private static Logger logger = LoggerFactory.getLogger(MazeClient.class);
	
	// The initial primary server address and port
	public static final String host = System.getProperty("host", "127.0.0.1");
	public static final int port = Integer.parseInt(System.getProperty("port", "9001"));
	
    public static void main(String[] args) throws Exception {
        try {
        	connect(host, port); 
        } catch(Exception ex) {
        	System.out.println("Fail to connect primary server...");
        	System.exit(-1);
        }
    }
    
    /**
     * Set up the connection to maze server. This method is called when player is started or
     * master is crashed.
     * @param host
     * @param port
     * @throws InterruptedException 
     */
    public static void connect(String host, int port) throws InterruptedException {
    	logger.info("[CONNECT_SERVER] {}:{}...", host, port);
    	
    	EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup); 
            b.channel(NioSocketChannel.class); 
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                	ChannelPipeline pipe = ch.pipeline();
                    
                    // Decoders
                    pipe.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
                    pipe.addLast("bytesDecoder", new ByteArrayDecoder());
                    pipe.addLast("messageDecoder", new MazeMessageDecoder());

                    // Encoders
                    pipe.addLast("frameEncoder", new LengthFieldPrepender(4));
                    pipe.addLast("bytesEncoder", new ByteArrayEncoder());
                    pipe.addLast("messageEncoder", new MazeMessageEncoder());
                	
                    // Add player handler, singleton
                    pipe.addLast(MazeClientHandler.instance());
                }
            });

            // Connect
            ChannelFuture f = b.connect(host, port).sync();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
