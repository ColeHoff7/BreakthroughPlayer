/* Written by Cole Hoffbauer, Harrison Roth, and Matt DePero */

package breakthrough;

import game.GameMove;
import game.GamePlayer;
import game.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Random;


class DecisionThread implements Runnable{
	
	static PriorityQueue<ScoredMove> results = new PriorityQueue<ScoredMove>();
	
	BreakthroughState board;
	BreakthroughMove move;
	boolean toMaximize;
	
	public DecisionThread(BreakthroughState board, BreakthroughMove move, boolean toMaximize){
		this.board = board;
		this.move = move;
		this.toMaximize = toMaximize;
	}
	
	public void run(){
		
		//System.out.println("Starting a thread...");
		
		double val = OurBreakthroughPlayer.getMoveValue(board, move);
		results.add(new ScoredMove(move, val, toMaximize));
		
		//System.out.println("A thread ended...");
	}
	
}

class ScoredMove implements Comparable<ScoredMove>{
	
	BreakthroughMove move;
	double score;
	boolean toMaximize;
	
	public ScoredMove(BreakthroughMove mv, double score, boolean toMaximize){
		this.move = mv;
		this.score = score;
		this.toMaximize = toMaximize;
	}

	@Override
	public int compareTo(ScoredMove o) {
		
		if(this.score > o.score) return toMaximize ? -1 : 1;
		else if(this.score < o.score) return toMaximize ? 1 : -1;
		else return 0;
	}
	
	public String toString(){
		return move+": "+score;
	}
}


public class OurBreakthroughPlayer extends GamePlayer {

	public static int depthLimit = 6;

	public static float COUNT_FACTOR = 0.5f;
	public static float JEP_FACTOR = 0.1f;
	public static float MAX_DIST_FACTOR = 0.2f;
	public static float AVG_DIST_FACTOR = 0.1f;
	
	private ArrayList<Thread> decisionThreads = new ArrayList<Thread>();
	
	public int MAX_MOVE_TIME;
	public int GAME_TIME;
	private double estTimeRemaining;

	public OurBreakthroughPlayer(String n) {
		super(n, "Breakthrough");

	}

	// constructor for getting ideal eval function
	public OurBreakthroughPlayer(String n, float count, float jep, float max,
			float avg) {
		super(n, "Breakthrough");

		COUNT_FACTOR = count;
		JEP_FACTOR = jep;
		MAX_DIST_FACTOR = max;
		AVG_DIST_FACTOR = avg;
		
		
		MAX_MOVE_TIME = this.tournamentParams.integer("MAXMOVETIME");
		GAME_TIME = this.tournamentParams.integer("GAMETIME");
		estTimeRemaining = GAME_TIME;
	}
<<<<<<< HEAD
	
	public GameMove getMove(GameState brd, String lastMove)
	{
		int depth = depthLimit;
		long start = System.currentTimeMillis();
	    double bestScore = (brd.getWho() == GameState.Who.HOME) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
	    depthLimit = 1;
		while((System.currentTimeMillis()-start)< 500){// && moves[0].score != bestScore){
			if(depthLimit >= depth){
				moves = new ScoredBreakthroughMove[depthLimit+1];
				for(int i = 0; i < moves.length; i++){
					moves[i] = new ScoredBreakthroughMove(0,0,0,0,0);
				}	
=======

	public GameMove getMove(GameState brd, String lastMove) {
		
		BreakthroughState board = (BreakthroughState)brd;

		ArrayList<BreakthroughMove> poss = getPossibleMoves(board);
		shuffle(poss);

		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);

		decisionThreads.clear();
		
		for (BreakthroughMove mv : poss) {

			Thread dt = new Thread(new DecisionThread((BreakthroughState)board.clone(), mv, toMaximize));
			decisionThreads.add(dt);
			dt.start();

		}
		
		int i = 0;
		int n = decisionThreads.size();
		
		
		// ***** Need to find a better way to do this, with join?
		while(i < n){
			if(decisionThreads.get(i).isAlive()){
				try{
					decisionThreads.get(i).join();
				}catch(Exception e){}
>>>>>>> 1efba13d58e21f20c2adb61bdae2a6500fc4da63
			}
			
			i++;
		}
		
		BreakthroughMove bestMove = DecisionThread.results.peek().move;
		
		DecisionThread.results.clear();

		return bestMove;

		/*
		 * FROM HARRISON, I (MATT) WILL EVENTUALLY INTEGRATE THIS INTO THE ABOVE
		 * THREADING STUFF
		 * 
		 * int depth = depthLimit; long start = System.currentTimeMillis();
		 * double bestScore = (brd.getWho() == GameState.Who.HOME) ?
		 * Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY; depthLimit = 1;
		 * while((System.currentTimeMillis()-start)< 50){// && moves[0].score !=
		 * bestScore){ if(depthLimit >= depth){ moves = new
		 * ScoredBreakthroughMove[depthLimit+1]; for(int i = 0; i <
		 * moves.length; i++){ moves[i] = new ScoredBreakthroughMove(0,0,0,0,0);
		 * } } alphabeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY,
		 * Double.POSITIVE_INFINITY); depthLimit ++;
		 * System.out.println(depthLimit); } return moves[0];
		 */
	}

<<<<<<< HEAD
  protected boolean terminalValue(GameState brd, ScoredBreakthroughMove mv){
    GameState.Status status = brd.getStatus();
    boolean isTerminal = true;

    if(status == GameState.Status.HOME_WIN){
      mv.set(0,0,0,0,Double.POSITIVE_INFINITY);
    }else if (status == GameState.Status.AWAY_WIN){
      mv.set(0,0,0,0,Double.NEGATIVE_INFINITY);
    }else if(status == GameState.Status.DRAW){
      mv.set(0,0,0,0,0);
    }
    else {
      isTerminal = false;
    }
    return isTerminal;
  }

  private void alphabeta(BreakthroughState brd, int currDepth, double alpha, double beta){
	boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
	boolean toMinimize = !toMaximize;
    
    boolean isTerminal = terminalValue(brd, moves[currDepth]);
    //System.out.println("-----------STARTING NEW-------------");
    double bestScore = (brd.getWho() == GameState.Who.HOME) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    if(isTerminal){
    	moves[currDepth].set(0,0,0,0, bestScore);
    }else if(currDepth == depthLimit){
      moves[currDepth].set(0,0,0,0,evalBoard(brd));
    }else {
      ScoredBreakthroughMove temp = new ScoredBreakthroughMove(0,0,0,0,0);
      

      ScoredBreakthroughMove bestMove = moves[currDepth];
      ScoredBreakthroughMove nextMove = moves[currDepth+1];

      //bestMove.set(0,0,0,0, bestScore);
      bestMove.setScore(bestScore);
      
      GameState.Who currTurn = brd.getWho();

      ArrayList<BreakthroughMove> poss = getPossibleMoves(brd);
      shuffle(poss);

      for(BreakthroughMove mv : poss){
        temp.set(mv);
        char ch = brd.board[temp.endingRow][temp.endingCol];
     	char pl = brd.board[temp.startRow][temp.startCol];
        brd.makeMove(temp);
        alphabeta(brd, currDepth+1, alpha, beta);
        
        //System.out.println(gameState.toString());
        
        //reverting move
        brd.board[temp.startRow][temp.startCol] = pl;//PLAYER;
  		brd.board[temp.endingRow][temp.endingCol] = ch;
        brd.numMoves--;
        brd.status = GameState.Status.GAME_ON;
        brd.who = currTurn;

        if(toMaximize && nextMove.score > bestMove.score){
          bestMove.set(mv, nextMove.score);
        }else if(!toMaximize && nextMove.score < bestMove.score){
          bestMove.set(mv, nextMove.score);
        }
        
     // Update alpha and beta. Perform pruning, if possible.
        if (toMinimize) {
        	beta = Math.min(bestMove.score, beta);
  		if (bestMove.score <= alpha) {
  			return;
  		}
        } else {
        	alpha = Math.max(bestMove.score, alpha);
	  		if (bestMove.score >= beta) {
	  			return;
	  		}
        }
      }
      
 
    }
}
=======
	public static double getMoveValue(BreakthroughState board, BreakthroughMove move) {
>>>>>>> 1efba13d58e21f20c2adb61bdae2a6500fc4da63

		GameState.Who currTurn = board.getWho();

		char ch = board.board[move.endingRow][move.endingCol];
		char pl = board.board[move.startRow][move.startCol];
		board.makeMove(move);

		double val = alphabeta(board, 1, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);

		// System.out.println(gameState.toString());

		// reverting move
		board.board[move.startRow][move.startCol] = pl;// PLAYER;
		board.board[move.endingRow][move.endingCol] = ch;
		board.numMoves--;
		board.status = GameState.Status.GAME_ON;
		board.who = currTurn;

		return val;

	}

	private static double alphabeta(BreakthroughState brd, int currDepth,
			double alpha, double beta) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean toMinimize = !toMaximize;

		boolean isTerminal = brd.status != GameState.Status.GAME_ON;
		// System.out.println("-----------STARTING NEW-------------");
		double bestScore = (brd.getWho() == GameState.Who.HOME) ? Double.NEGATIVE_INFINITY
				: Double.POSITIVE_INFINITY;
		if (isTerminal) {
			return bestScore;
		} else if (currDepth == depthLimit) {
			return evalBoard(brd);
		}

		GameState.Who currTurn = brd.getWho();

		ArrayList<BreakthroughMove> poss = getPossibleMoves(brd);
		shuffle(poss);

		for (BreakthroughMove mv : poss) {
			char ch = brd.board[mv.endingRow][mv.endingCol];
			char pl = brd.board[mv.startRow][mv.startCol];
			brd.makeMove(mv);

			double temp = alphabeta(brd, currDepth + 1, alpha, beta);

			// System.out.println(gameState.toString());

			// reverting move
			brd.board[mv.startRow][mv.startCol] = pl;// PLAYER;
			brd.board[mv.endingRow][mv.endingCol] = ch;
			brd.numMoves--;
			brd.status = GameState.Status.GAME_ON;
			brd.who = currTurn;

			if (toMaximize && temp > bestScore) {
				bestScore = temp;
			} else if (!toMaximize && temp < bestScore) {
				bestScore = temp;
			}

			// Update alpha and beta. Perform pruning, if possible.
			if (toMinimize) {
				beta = Math.min(bestScore, beta);
				if (bestScore <= alpha || bestScore == Double.NEGATIVE_INFINITY) {
					return bestScore;
				}
			} else {
				alpha = Math.max(bestScore, alpha);
				if (bestScore >= beta || bestScore == Double.POSITIVE_INFINITY) {
					return bestScore;
				}
			}
		}

		return bestScore;

	}

	private static double evalBoard(BreakthroughState brd) {
		int awayCount = 0;
		int homeCount = 0;
		int numJepA = 0; // number of pieces in jeopardy for away
		int numJepH = 0;
		int maxDistH = 0;
		int maxDistA = 0;
		int totalDistanceA = 0;
		int totalDistanceH = 0;

		for (int i = 0; i < BreakthroughState.N; i++) {
			for (int j = 0; j < BreakthroughState.N; j++) {
				char c = brd.board[i][j];
				if (c == BreakthroughState.homeSym) {
					homeCount++;
					totalDistanceH += i;
					if (i > maxDistH)
						maxDistH = i;
					if (j != 0) {
						if (brd.board[i + 1][j - 1] == BreakthroughState.awaySym)
							numJepH++;
					}
					if (j != (BreakthroughState.N - 1)) {
						if (brd.board[i + 1][j + 1] == BreakthroughState.awaySym)
							numJepH++;
					}
					if (brd.board[i + 1][j] == BreakthroughState.awaySym)
						numJepH++;
				} else if (c == BreakthroughState.awaySym) {
					awayCount++;
					totalDistanceA += (BreakthroughState.N - 1 - i);
					if ((BreakthroughState.N - 1 - i) > maxDistA)
						maxDistA = (BreakthroughState.N - 1 - i);
					if (j != 0) {
						if (brd.board[i - 1][j - 1] == BreakthroughState.homeSym)
							numJepA++;
					}
					if (j != (BreakthroughState.N - 1)) {
						if (brd.board[i - 1][j + 1] == BreakthroughState.homeSym)
							numJepA++;
					}
					if (brd.board[i - 1][j] == BreakthroughState.homeSym)
						numJepA++;
				} else {
					// square was blank
				}
			}

		}
		float avgDistanceA = (float) totalDistanceA / awayCount;
		float avgDistanceH = (float) totalDistanceH / homeCount;

		float score = COUNT_FACTOR * (homeCount - awayCount) - JEP_FACTOR
				* (numJepH - numJepA) + MAX_DIST_FACTOR * (maxDistH - maxDistA)
				+ AVG_DIST_FACTOR * (avgDistanceH - avgDistanceA);
		return score;
	}

	private static ArrayList<BreakthroughMove> getPossibleMoves(
			BreakthroughState brd) {

		ArrayList<BreakthroughMove> poss = new ArrayList<>();
		GameState.Who currTurn = brd.who;
		char curry = (currTurn == GameState.Who.HOME) ? BreakthroughState.homeSym
				: BreakthroughState.awaySym;
		int dir = (currTurn == GameState.Who.HOME) ? 1 : -1;

		for (int i = 0; i < BreakthroughState.N; i++) {
			for (int j = 0; j < BreakthroughState.N; j++) {
				if (i + dir != -1 && i + dir != 8) {
					if (brd.board[i][j] == curry) {
						if (BreakthroughMove.indexOK(j + 1)) {
							if (brd.board[i + dir][j + 1] != curry) {
								BreakthroughMove mv = new BreakthroughMove(i,
										j, i + dir, j + 1);
								if (brd.moveOK(mv)) {
									poss.add(mv);
								}
							}
						}
						if (BreakthroughMove.indexOK(j - 1)) {
							if (brd.board[i + dir][j - 1] != curry) {
								BreakthroughMove mv3 = new BreakthroughMove(i,
										j, i + dir, j - 1);
								if (brd.moveOK(mv3)) {
									poss.add(mv3);
								}
							}
						}
						if (brd.board[i + dir][j] != curry) {
							BreakthroughMove mv2 = new BreakthroughMove(i, j, i
									+ dir, j);

							if (brd.moveOK(mv2)) {
								poss.add(mv2);
							}
						}
					}
				}
			}
		}

		return poss;
	}

	private static void shuffle(ArrayList<BreakthroughMove> poss) {
		long seed = System.nanoTime();
		Collections.shuffle(poss, new Random(seed));
	}

	public static void main(String[] args) {
		GamePlayer p = new OurBreakthroughPlayer("COLEMATTHARRISONPOWERHOUR");
<<<<<<< HEAD
		//p.compete(args);
		p.solvePuzzles(new String [] {"BTPuzzle1", "BTPuzzle2"});
=======

		p.compete(args);
		// p.solvePuzzles(new String [] {"BTPuzzle1", "BTPuzzle2"});
>>>>>>> 1efba13d58e21f20c2adb61bdae2a6500fc4da63
	}
}
