package mazegame.io;

import java.io.ByteArrayInputStream;
import java.util.List;

import mazegame.messages.MazeMsgType;
import mazegame.messages.MazeMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class MazeMessageDecoder extends MessageToMessageDecoder<byte[]> {

	@Override
	protected void decode(ChannelHandlerContext ctx, byte[] bytes,
			List<Object> out) throws Exception {
		MazeDataInputStream mdin = new MazeDataInputStream(new ByteArrayInputStream((bytes)));
		
		// Read the type
		int code = mdin.readInt();
		MazeMsgType type = MazeMsgType.fromCode(code);
		MazeMessage msg = (MazeMessage)Class.forName(type.getClassName()).newInstance();
		
    	// Read the message
		msg.fromStream(mdin);
		mdin.close();
		
		out.add(msg);
	}

}
