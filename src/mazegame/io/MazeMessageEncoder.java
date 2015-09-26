package mazegame.io;

import java.io.ByteArrayOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import mazegame.messages.MazeMessage;

/**
 * 
 * @author chenweiming
 *
 */
public class MazeMessageEncoder extends MessageToByteEncoder<MazeMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, MazeMessage msg,
			ByteBuf out) throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		MazeDataOutputStream mdout = new MazeDataOutputStream(bout);
		
		// Encode the message
		msg.toStream(mdout);
		mdout.flush();
		mdout.close();
		
		out.writeBytes(bout.toByteArray());
	}

}
