package mazegame.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mazegame.player.MazeClient;
import mazegame.server.MazeServer;

public class MazeNode {
	private static Logger logger = LoggerFactory.getLogger(MazeNode.class);

	public static void main(String[] args) throws Exception {

		int code = Integer.parseInt(args[0]);
		MNodeType nodeType = MNodeType.fromCode(code);

		switch (nodeType) {
		case PRIMARY:
			final int mPort = Integer.parseInt(args[1]);
			final int x = Integer.parseInt(args[2]);
			final int y = Integer.parseInt(args[3]);
			final int treasure = Integer.parseInt(args[4]);
			String address = args[5];
			String[] addressArr = address.split(":");
			startServer(mPort, x, y, treasure);

			Thread.sleep(1000);
			startClient(addressArr[0], Integer.parseInt(addressArr[1]), MNodeType.PRIMARY);
			break;
		case CLIENT:
			String address1 = args[1];
			String[] addressArr1 = address1.split(":");
			startClient(addressArr1[0], Integer.parseInt(addressArr1[1]), MNodeType.CLIENT);
			break;
		default:
			System.exit(-1);
			break;
		}
	}

	public static void startServer(final int mPort, final int x, final int y, final int treasure) {
		new Thread() {
			@Override
			public void run() {
				try {
					MazeServer.createServer(x, y, treasure, mPort);
				} catch (Exception ex) {
					logger.error("Error creating server, " + ex);
				}
			}
		}.start();
	}

	public static void startClient(String addressIP, int port, MNodeType type) throws InterruptedException {
		MazeClient.createClient(addressIP, port, type);
	}
}
