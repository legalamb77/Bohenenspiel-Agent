package student_player;

import java.util.ArrayList;
import java.util.Random;

import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;
import bohnenspiel.BohnenspielPlayer;
import bohnenspiel.BohnenspielMove.MoveType;
import student_player.mytools.MyTools;

/** A Hus player submitted by a student. */
public class StudentPlayer extends BohnenspielPlayer {
	//Random rand = new Random();
    /** You must modify this constructor to return your student number.
     * This is important, because this is what the code that runs the
     * competition uses to associate you with your agent.
     * The constructor should do nothing else. */
    public StudentPlayer() { super("260621657"); }
    
    public boolean isFirst=true;

    /** This is the primary method that you need to implement.
     * The ``board_state`` object contains the current state of the game,
     * which your agent can use to make decisions. See the class
bohnenspiel.RandomPlayer
     * for another example agent. */
    public BohnenspielMove chooseMove(BohnenspielBoardState board_state)
    {
    	BohnenspielMove choice=(BohnenspielMove) board_state.getRandomMove();
    	//An opening move drawn from the footage of the expert players. Originally it was only going to be the opening move if my agent went first,
    	//But it performed fairly well as a first move for going second as well, so I kept it.
    	BohnenspielMove openingMove = new BohnenspielMove(4);
    	if (isFirst)
    	{
    		isFirst=false;
    		return openingMove;
    	}
    	long startTime = System.nanoTime();
    	//This section is a pseudo-greedy action which checks if there are any winning moves present, and takes them immediately if there are.
    	//After all, there's not much that could be better than a guaranteed win.
    	for(BohnenspielMove m : board_state.getLegalMoves())
    	{
    		BohnenspielBoardState t = (BohnenspielBoardState) board_state.clone();
    		t.move(m);
    		if(t.getScore(player_id)>36)
    		{
    			return m;
    		}
    	}
    	int depth=0;
    	//This is an iterative deepening search, which increases depth over time, and calls minimax with alpha beta pruning.
    	//If time is exceeded, the last successful computation(choice) is returned.
    	while(!MyTools.timeOut(startTime))
    	{
    		BohnenspielMove result = MyTools.alphaBetaMM(board_state, player_id, opponent_id, depth,startTime);
    		if (result==null)
    		{
    			return choice;
    		}
    		choice=result;
    		depth++;
    	}
    	return choice;
    }
}