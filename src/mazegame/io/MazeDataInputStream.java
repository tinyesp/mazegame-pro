package mazegame.io;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class extends DataInputStream and provides a few customized methods
 * to help decode message from a byte array.
 * 
 * @author chenweiming
 *
 */
public class MazeDataInputStream extends DataInputStream {
	
	/**
	 * Constructor from a InputStream
	 * @param in
	 */
	public MazeDataInputStream(InputStream in) {
		super(in);
	}
	
	/**
	 * Constructor from a byte array
	 * @param buf
	 */
	public MazeDataInputStream(byte[] buf) {
		this(new ByteArrayInputStream(buf));
	}
	
	/**
	 * Read a string from the input stream, use the first integer as the string
	 * length.
	 * @return
	 * @throws IOException
	 */
	public String readString() throws IOException {
		int length = super.readInt();
		return readString(length);
	}
	
	private String readString(int length) throws IOException {
		byte[] bytes = new byte[length];
		super.readFully(bytes);
		
		return new String(bytes);
	}
	
}
