package student_player.mytools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import boardgame.Board;
import boardgame.Move;
import bohnenspiel.BohnenspielBoardState;
import bohnenspiel.BohnenspielMove;

public class MyTools {
	static long timeCutoffNano = 1000000*685;
    
    //Counts the number of empty pits that a given player has on their side.
    public static int countEmpties (int id, BohnenspielBoardState boardstate) {
    	int[][] pits=boardstate.getPits();
    	int empties=0;
    	for (int i=0;i<6;i++)
    	{
    		if(pits[id][i]==0)
    		{
        		empties++;
    		}
    	}
    	return empties;
    }
    
    //Counts the number of beans on the board for a given player
    public static int countBeans (int id, BohnenspielBoardState boardstate) {
    	int[][] pits=boardstate.getPits();
    	int beans=0;
    	for (int i=0;i<6;i++)
    	{
    		beans+=pits[id][i];
    	}
    	return beans;
    }
    
    //A function for testing with one of the simplest evaluation functions, for benchmarking.
    public static double simpleEvaluate(BohnenspielBoardState boardstate, int pid, int eid) {
    	return (boardstate.getScore(pid));
    }
    
    //A more complex evaluation function, which takes into account the empty spaces on the board
    //In the end, I found that this method was outperformed by the pseudo-greedy action taken by choosemove.
    //The computation time used by this was not worth the results it gave further down the line.
    public static double complexEvaluate1(BohnenspielBoardState boardstate, int pid, int eid) {
    	int eEmpt=countEmpties(eid,boardstate);
    	double score = 2*boardstate.getScore(pid)+eEmpt;
    	double undesirablePits = 2*boardstate.getScore(eid)+countEmpties(pid,boardstate);
    	if(eEmpt==6)
    	{
    		score+=100; //It is extremely desirable if we can empty all of the opponent's pits, because then we capture everything on our side
    	}
    	double eval = score-undesirablePits;
    	return eval;
    }
    
    //A different complex evaluation function, which takes into account the beans each player has
    public static double complexEvaluate2(BohnenspielBoardState boardstate, int pid, int eid) {
    	double score = 2*boardstate.getScore(pid)+countBeans(pid,boardstate);
    	double undesirablePits = 2*boardstate.getScore(eid)+countBeans(eid,boardstate);
    	double eval = score-undesirablePits;
    	return eval;
    }
    
    //A third function that counts empties and beans
    public static double complexEvaluate3(BohnenspielBoardState boardstate, int pid, int eid) {
    	int eEmpt=countEmpties(eid,boardstate);
    	double score = 2*boardstate.getScore(pid)+eEmpt+countBeans(pid,boardstate);
    	double undesirablePits = 2*boardstate.getScore(eid)+countEmpties(pid,boardstate)+countBeans(eid,boardstate);
    	if(eEmpt==6)
    	{
    		score+=100; //It is extremely desirable if we can empty all of the opponent's pits, because then we capture everything on our side
    	}
    	double eval = score-undesirablePits;
    	return eval;
    }
    
    /*This evaluation function uses a heuristic that values moves which choose the pits 0,1, and 2 more than the pits 3,4, or 5 during the opening moves.
    *This had a number of tradeoffs, as it allowed better performance against certain agents which were more complex
    * but also caused worse performance against simpler agents. I felt the tradeoffs weren't worth it, and abandoned this function.
    */
    public static double simpleEvaluate2(BohnenspielBoardState boardstate, int pid, int eid) {
    	double eval=(boardstate.getScore(pid)-boardstate.getScore(eid));
    	int[][] pits = boardstate.getPits();
    	if(boardstate.getTurnNumber()<5)
    	{
    		eval+=0.1*pits[pid][3];
    		eval+=0.1*pits[pid][4];
    		eval+=0.1*pits[pid][5];
    	}
    	return eval;
    }
    
    /*The current evaluation function takes into account the difference between the agent's score and the opponents score, as the most important 
    *factor is having a higher score than the opponent, not just a higher score.
    *Other evaluation functions were included(above), but were not used.
    */
    public static double evaluate(BohnenspielBoardState boardstate, int pid, int eid) {
    	double eval=(boardstate.getScore(pid)-boardstate.getScore(eid));
    	return eval;
    }

    //Checks if the current depth exceeds the depth limit.
    public static boolean cutoff (int depthLimit, int depthNow) {
    	boolean check=depthLimit<=depthNow;
    	return check;
    }
    
    //Takes a move and a state, and applies the move to that state, returning the resulting state. Used in the lambda function for sorting moves.
    public static BohnenspielBoardState moveToState (BohnenspielBoardState stateNow, BohnenspielMove m)
    {
    	BohnenspielBoardState newState=(BohnenspielBoardState) stateNow.clone();
    	newState.move(m);
    	return newState;
    }
    
    //Takes the current state, the moves under consideration, and the pid and eid. Returns the ordered list of states caused by
    //applying the moves, with respect to the evaluation function.
    public static BohnenspielBoardState[] orderStates (BohnenspielBoardState state, BohnenspielMove[] moves,int pid, int eid)
    {
    	BohnenspielBoardState[] states = new BohnenspielBoardState[moves.length];
    	for (int i=0;i<moves.length;i++)
    	{
    		states[i]=(BohnenspielBoardState) state.clone();
    		states[i].move(moves[i]);
    	}
    	Collections.sort(Arrays.asList(states), (a,b) -> Double.compare(evaluate((BohnenspielBoardState) a,pid,eid),evaluate((BohnenspielBoardState) b,pid,eid)));
    	return states;
    }
    
    //This implementation seems to make performance worse -- It takes too much computation time for the benefit it returns, and makes the agent perform worse.
    //Takes the current state, the moves under consideration, and the pid and eid. Returns the ordered list of moves caused by
    //applying the moves to state, with respect to the evaluation function.
    //Due to the lack of benefit, the move-ordering no longer sorts, as the costs outweighed the benefits.
    public static BohnenspielMove[] orderMoves (BohnenspielBoardState state, BohnenspielMove[] moves,int pid, int eid)
    {
    	//Collections.sort(Arrays.asList(moves), (a,b) -> Double.compare(evaluate((BohnenspielBoardState) moveToState(state,a),pid,eid),evaluate((BohnenspielBoardState) moveToState(state,b),pid,eid)));
    	//Collections.reverse(Arrays.asList(moves));
    	return moves;
    }
    
    //Checks if a the current elapsed time is over the cutoff when given the starting time. Returns true if it is over the cutoff, false if time still remains.
    public static boolean timeOut(long time) {
    	return (System.nanoTime()-time)>=timeCutoffNano;
    }
    
    
    //The parent function for alpha-beta pruning search, which searches the game tree up until the depth limit (depthL) is reached, or time runs out.
    public static BohnenspielMove alphaBetaMM (BohnenspielBoardState boardstate, int pid, int eid, int depthL,long startT)
    {
    	//For each move in legal moves, clone the state, and run miniMaxValue on it.
    	if(timeOut(startT))
    	{
    		return null;
    	}
    	ArrayList<BohnenspielMove> moves = boardstate.getLegalMoves();
    	BohnenspielMove[] moveList = new BohnenspielMove[moves.size()];
    	moveList=moves.toArray(moveList);
    	BohnenspielMove[] orderedMoveList= orderMoves(boardstate,moveList,pid,eid);
    	double[] vals = new double[moves.size()];
    	int maxIndex=0;
    	for(int i = 0; i<moves.size(); i++)
    	{
    		BohnenspielBoardState testState = (BohnenspielBoardState) boardstate.clone();
    		testState.move(orderedMoveList[i]);
    		vals[i]=abminiMaxValue(testState,pid,eid,depthL,1,startT,true,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
    		if(vals[i]==-9999)
    		{
    			return null;
    		}
    		if(vals[maxIndex]<vals[i])
    		{
    			maxIndex=i;
    		}
    	}
    	return orderedMoveList[maxIndex];
    }
    
    //Minimax value operation without alpha beta pruning.
    //This was not used in my final agent, but was used for testing against other agents in order to evaluate the use of alpha-beta pruning.
    public static double miniMaxValue (BohnenspielBoardState boardstate, int pid, int eid, int depthL, int depthN, long startT, boolean mm)
    {
    	if(timeOut(startT))
    	{
    		return -9999;
    	}
    	if(cutoff(depthL,depthN) || boardstate.getLegalMoves().size()==0)
    	{
    		return simpleEvaluate(boardstate,pid,eid);
    	}
    	ArrayList<BohnenspielMove> moves = boardstate.getLegalMoves();
    	double[] vals = new double[moves.size()];
    	int i = 0;
    	for(BohnenspielMove m : moves)
    	{
    		BohnenspielBoardState testState = (BohnenspielBoardState) boardstate.clone();
    		testState.move(m);
    		vals[i]=miniMaxValue(testState,pid,eid,depthL,depthN+1,startT,!mm);
    		i++;
    	}
    	Arrays.sort(vals);
    	//mm==true if max player's turn 
    	if(mm)
		{
			return vals[vals.length-1];
		}
		else
		{
			return vals[0];
		}
    }
    
    //testing function for benchmarking against my own agents, not the final function used.
    public static BohnenspielMove alphaBetaMMTEST (BohnenspielBoardState boardstate, int pid, int eid, int depthL,long startT)
    {
    	//For each move in legal moves, clone the state, and run miniMaxValue on it.
    	if(timeOut(startT))
    	{
    		return null;
    	}
    	ArrayList<BohnenspielMove> moves = boardstate.getLegalMoves();
    	BohnenspielMove[] moveList = new BohnenspielMove[moves.size()];
    	moveList=moves.toArray(moveList);
    	double[] vals = new double[moves.size()];
    	int maxIndex=0;
    	for(int i = 0; i<moves.size(); i++)
    	{
    		BohnenspielBoardState testState = (BohnenspielBoardState) boardstate.clone();
    		testState.move(moveList[i]);
    		vals[i]=miniMaxValue(testState,pid,eid,depthL,1,startT,true);//abminiMaxValue(testState,pid,eid,depthL,1,startT,true,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);//
    		if(vals[i]==-9999)
    		{
    			return null;
    		}
    		//System.out.println(vals[i]);
    		if(vals[maxIndex]<vals[i])
    		{
    			//System.out.println(vals[i]);
    			maxIndex=i;
    		}
    	}
    	//System.out.println(Arrays.toString(vals)+depthL);
    	return moveList[maxIndex];
    }
    
    /*Minimax search function(recursive), which uses alpha beta pruning and a move ordering which takes a small amount of extra time, 
    *since the states are generated, then sorted by their evaluation.
    */
    //adapted from R&N
    public static double abminiMaxValue (BohnenspielBoardState boardstate, int pid, int eid, int depthL, int depthN, long startT, boolean mm, double alpha, double beta)
    {
    	if(timeOut(startT))
    	{
    		return -9999;
    	}
    	if(cutoff(depthL,depthN) || boardstate.getLegalMoves().size()==0)
    	{
    		return evaluate(boardstate,pid,eid);
    	}
    	ArrayList<BohnenspielMove> moves = boardstate.getLegalMoves();
    	BohnenspielMove[] moveList = new BohnenspielMove[moves.size()];
    	moveList=moves.toArray(moveList);
    	BohnenspielBoardState[] orderedStateList=orderStates(boardstate,moveList,pid,eid);
    	//mm==true if max player's turn 
    	if(mm) //Max case
    	{
    		double value=Double.NEGATIVE_INFINITY;
        	for(BohnenspielBoardState s: orderedStateList)
    		{
    			value=Math.max(value,abminiMaxValue(s,pid,eid,depthL,depthN+1,startT,!mm,alpha,beta));
    			if(value>beta)
    				return value;
    			alpha=Math.max(alpha,value);
        	}
    		return value;
        }
    	else //Min case
    	{
    		double value=Double.POSITIVE_INFINITY;
    		for(BohnenspielBoardState s: orderedStateList)
    		{
    			value=Math.min(value,abminiMaxValue(s,pid,eid,depthL,depthN+1,startT,!mm,alpha,beta));
        		if(value<alpha)
        			return value;
        		beta=Math.min(beta, value);
        	}
    		return value;
    	}
    }
    
    //This function was implemented for use in a prospective transposition table, but this was not completed.
    //It takes the current board state, alpha, beta, and depth, and returns a string of the format (depth)(alpha,beta)(score0,pits,score1) for hashing.
    public static String transform(BohnenspielBoardState b, double alpha, double beta, int depth)
    {
    	return "("+depth+")"+"("+alpha+","+beta+")"+"("+b.getScore(0)+","+Arrays.toString(b.getPits())+","+b.getScore(1)+")";
    }   
}
