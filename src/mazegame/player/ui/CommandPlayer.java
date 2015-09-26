package mazegame.player.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mazegame.data.Player;
import mazegame.data.WorldSnapshot;
import mazegame.message.application.Direction;
import mazegame.message.application.GameEndedMsg;
import mazegame.message.application.GameStartedMsg;
import mazegame.message.application.JoinGameMsg;
import mazegame.message.application.JoinGameRes;
import mazegame.message.application.MoveRequestMsg;
import mazegame.message.application.MoveRequestRes;
import mazegame.message.application.QuitGameMsg;
import mazegame.message.system.RejoinRes;
import mazegame.node.MNodeType;
import mazegame.player.MazeClientHandler;

public class CommandPlayer implements IPlayer {
	// Run next move
	private static ExecutorService executor = Executors.newFixedThreadPool(3);
	private Future<?> future;
	
	private MazeClientHandler handler = MazeClientHandler.instance();
	private PlayerWorld myWorld;
	
	// Prompt user for next move
	private class NextMove implements Callable<Integer> {
		@Override
		public Integer call() throws Exception {
			System.out.println("Please enter a selection: ");
			System.out.println("0. STAY");
	        System.out.println("1. UP");
	        System.out.println("2. DOWN");
	        System.out.println("3. LEFT");
	        System.out.println("4. RIGHT");
	        System.out.println("5. Quit");
	        
	        // Read next move
	        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        int option = -1;
	        
	        do {
	        	while(!br.ready()) {
	        		Thread.sleep(500);
	        	}
	        	
	        	String line = br.readLine();
	        	try {
	        		option = Integer.parseInt(line);
	        	} catch(Exception ex) {
	        		System.out.println("Please enter a valid option!");
	        		continue;
	        	}
	        	
	        	if(option < 0 || option > 5) {
	        		System.out.println("Please enter a valid option!");
	        		continue;
	        	}
	        	
	        	break;
	        } while(option < 0);
	        
	        if(option == 5) {
	        	// Send out quit message
	        	QuitGameMsg quiteMsg = new QuitGameMsg();
	        	handler.writeToChannel(quiteMsg);
	        	Thread.sleep(1000);
	        	
	        	// GG & Exit
	        	System.out.println("Goodbye!");
	    		System.exit(-1);
	        }
	       
	        // Send out move request
	        Direction dir = Direction.fromCode(option);
	        MoveRequestMsg moveRequest = new MoveRequestMsg(dir);
	        handler.writeToChannel(moveRequest);
	        
	        return option;
		}
	}
	
	public CommandPlayer() {
		myWorld = new PlayerWorld();
	}
	
	@Override
	public int getId() {
		return myWorld.getId();
	}
	
	@Override
	public int getLength() {
		return myWorld.getLength();
	}

	@Override
	public int getWidth() {
		return myWorld.getWidth();
	}

	@Override
	public int getTotalTreasures() {
		return myWorld.getTotalTreasures();
	}

	@Override
	public void init() {
		System.out.println("Welcome to the maze game...");
        System.out.println("Please enter a selection: ");
         
	    // Prompt to join or quit game
	    System.out.println("1. Join");
	    System.out.println("2. Quit");
	     
	    // Takes in the entry from the CLI from the user
	    Scanner scan = new Scanner(System.in);
	    int input = scan.nextInt();
	     
	    // Selects the option that the user input
	    switch (input) {
	    	case 1:
	    		System.out.println("Joining the game...");
	    		MNodeType nodeType = MazeClientHandler.instance().getNodeType();
	    		handler.writeToChannel(new JoinGameMsg(nodeType));
	    		break;
	    	case 2:
	    		System.out.println("Goodbye!");
	    		System.exit(-1);
	    		break;
	    	default:
	    		System.out.println("Please enter a valid option.");
	    		init();
	    		break;
	    }
	}

	@Override
	public void onJoinGameReplied(JoinGameRes joinResponse) {
		if(!joinResponse.getStatus()) {
			System.out.println("Fail to join the game: " + joinResponse.getMessage());
			init();
		}
		
		// Update player world
		myWorld.setId(joinResponse.getId());
		myWorld.setLength(joinResponse.getLength());
		myWorld.setWidth(joinResponse.getWidth());
		myWorld.setTotalTreasures(joinResponse.getTotalTreasures());
		
		System.out.println("Join Game Success!");
		System.out.println("Player Id: " + joinResponse.getId());
		System.out.println("Map Size: (" + joinResponse.getLength() + ", " + joinResponse.getWidth() + ")");
		System.out.println("Total Treasures: " + joinResponse.getTotalTreasures());
		System.out.println("Waiting for others...");
	}

	@Override
	public void onGameStarted(GameStartedMsg gameStarted) {
		System.out.println("Game Started...");
		
		// Update client world
		WorldSnapshot snapshot = gameStarted.getSnapshot();
		for(Player player : snapshot.getPlayers()) {
			if(player.getId() == myWorld.getId()) {
				myWorld.setxPos(player.getxPos());
				myWorld.setyPos(player.getyPos());
				myWorld.setTreasures(player.getTreasures());
			}
		}
		
		myWorld.setSnapshot(snapshot);
		
		// Print my world
		printMyWorld();
		// Prompt for the first move
		future = executor.submit(new NextMove());
	}

	@Override
	public void onMoveReplied(MoveRequestRes moveResponse) {
		if(!moveResponse.getStatus()) {
			System.out.println("Move Fail: " + moveResponse.getMessage());
		} else {
			System.out.println("Move Success! New Treasures: " + moveResponse.getNewTreasures());
		}
		
		// Update client world
		WorldSnapshot snapshot = moveResponse.getSnapshot();
		for(Player player : snapshot.getPlayers()) {
			if(player.getId() == myWorld.getId()) {
				myWorld.setxPos(player.getxPos());
				myWorld.setyPos(player.getyPos());
			}
		}
		
		myWorld.addTreasures(moveResponse.getNewTreasures());
		myWorld.setSnapshot(snapshot);
		
		// Print my world
		printMyWorld();
		// Prompt for the next move
		future = executor.submit(new NextMove());
	}

	@Override
	public void onGameEnded(GameEndedMsg gameEnded) {
		System.out.println("Game Ended...");
		if(future != null) {
			future.cancel(true);
		}
		
		// Update client world
		myWorld.setSnapshot(gameEnded.getSnapshot());
		printMyWorld();
	}
	
	@Override
	public void onRejoinReplied(RejoinRes rejoin) {
		if(!rejoin.getStatus()) {
			System.out.println("Fail to rejoin...");
			System.exit(-1);
		}
		
		// Update client world
		WorldSnapshot snapshot = rejoin.getSnapshot();
		for(Player player : snapshot.getPlayers()) {
			if(player.getId() == myWorld.getId()) {
				myWorld.setxPos(player.getxPos());
				myWorld.setyPos(player.getyPos());
			}
		}
		
		myWorld.setSnapshot(snapshot);
		
		// Print my world
		printMyWorld();
		// Prompt for the next move
		future = executor.submit(new NextMove());
	}

	@Override
	public void onError(String message) {
		System.out.println(message);
		if(future != null) {
			future.cancel(true);
		}
	}
	
	/**
	 * Print out my world
	 */
	private void printMyWorld() {
		System.out.println("User Id: " + myWorld.getId() + " Treasures: " + myWorld.getTreasures());
		
		// Render the map
		int[][] map = myWorld.getMap();
		
		for(int i=0;i<2*map.length-1;i++) {
			System.out.print("-");
        }
		
        System.out.println();
        for(int i=0; i<map.length; i++) {
        	System.out.print("|");
        	for(int j=0; j<map[0].length; j++) {
        		if(map[i][j] < 0) {
                    if(map[i][j]==-myWorld.getId())
                        System.out.print("X|");
                    else
                        System.out.print("P|");
                } else {
                    if(map[i][j]<10)
                        System.out.print(map[i][j]+"|");
                    else
                        System.out.print("+|");
                }
        	}
        	System.out.println();
        }
      
        for(int i=0;i<2*map.length-1;i++) {
            System.out.print("-");
        }
        System.out.println();
	}

}
