package mazegame.node;



/**
 *
 * @author gejun
 * 
 * TODO:
 */
public class MazeNode {

	/*
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
				startServer(mPort, x, y, treasure, false);
	
				Thread.sleep(1000);
				startClient(addressArr[0], Integer.parseInt(addressArr[1]), MNodeType.MASTER);
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

	public static void startServer(final int mPort, final int x, final int y, final int treasure,
			final boolean isSlave) {
		new Thread() {
			@Override
			public void run() {
				try {
					GameServer.init(mPort, x, y, treasure, isSlave);
				} catch (Exception ex) {
					Logger.getLogger(MazeNode.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}.start();
	}

	public static void startClient(String addressIP, int port, String type) throws InterruptedException {
		GameClient.init(addressIP, port, type);
	}*/
}
