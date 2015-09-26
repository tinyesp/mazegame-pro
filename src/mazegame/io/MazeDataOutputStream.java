package mazegame.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *  This class extends DataOutputStream and provides a few customized methods
 *  help encode message to a byte array.
 * 
 * @author chenweiming
 *
 */
public class MazeDataOutputStream extends DataOutputStream {

	/**
	 * Constructor from a OutputStream
	 * @param out
	 */
	public MazeDataOutputStream(OutputStream out) {
		super(out);
	}
	
	/**
	 * Write a string to the output stream, append a integer as the string
	 * length.
	 * @param msg
	 * @throws IOException
	 */
	public void writeString(String msg) throws IOException {
		if(msg == null) {
			msg = "";
		}
		
		byte[] bytes = msg.getBytes();
		super.writeInt(bytes.length);
		super.write(bytes);
	}

}
