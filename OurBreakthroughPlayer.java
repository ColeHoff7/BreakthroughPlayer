
/* Written by Cole Hoffbauer, Harrison Roth, and Matt DePero */

package breakthrough;
import game.GameMove;
import game.GamePlayer;
import game.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class OurBreakthroughPlayer extends GamePlayer {

	public static int depthLimit = 4;
	

	public static float COUNT_FACTOR = 0.5f;
	public static float JEP_FACTOR  = 0.1f;
	public static float MAX_DIST_FACTOR = 0.2f;
	public static float AVG_DIST_FACTOR = 0.1f;
	

	
	public OurBreakthroughPlayer(String n)
	{
		super(n, "Breakthrough");
		
	}
	
	//constructor for getting ideal eval function
	public OurBreakthroughPlayer(String n, float count, float jep, float max, float avg)
	{
		super(n, "Breakthrough");
		
		COUNT_FACTOR = count;
		JEP_FACTOR = jep;
		MAX_DIST_FACTOR = max;
		AVG_DIST_FACTOR = avg;
	}
	
	public GameMove getMove(GameState brd, String lastMove)
	{
		
		BreakthroughState board = (BreakthroughState)brd;
		ArrayList<BreakthroughMove> poss = getPossibleMoves(board);
	      shuffle(poss);
	      
	      GameState.Who currTurn = brd.getWho();
	      boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
	      
	      BreakthroughMove bestMove = new BreakthroughMove();
	      double bestVal = (brd.getWho() == GameState.Who.HOME) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
	      
	      for(BreakthroughMove mv : poss){
	        char ch = board.board[mv.endingRow][mv.endingCol];
	     	char pl = board.board[mv.startRow][mv.startCol];
	        brd.makeMove(mv);
	        
	        
	        // ****** THIS IS WHERE THREADING WILL TAKE PLACE ************
	        
	        double temp = alphabeta(board, 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	        
	        if(toMaximize && temp > bestVal){
	        	bestMove = mv;
	        	bestVal = temp;
	        }else if(!toMaximize && temp < bestVal){
	        	bestMove = mv;
	        	bestVal = temp;
	        }
	        
	        //System.out.println(gameState.toString());
	        
	        //reverting move
	        board.board[mv.startRow][mv.startCol] = pl;//PLAYER;
	        board.board[mv.endingRow][mv.endingCol] = ch;
	        board.numMoves--;
	        board.status = GameState.Status.GAME_ON;
	        board.who = currTurn;
	      }
		
		return bestMove;

	}

  private double alphabeta(BreakthroughState brd, int currDepth, double alpha, double beta){
	boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
	boolean toMinimize = !toMaximize;
    
    boolean isTerminal = brd.status != GameState.Status.GAME_ON;
    //System.out.println("-----------STARTING NEW-------------");
    double bestScore = (brd.getWho() == GameState.Who.HOME) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
    if(isTerminal){
    	return bestScore;
    }else if(currDepth == depthLimit){
    	return evalBoard(brd);
    }
      
      GameState.Who currTurn = brd.getWho();

      ArrayList<BreakthroughMove> poss = getPossibleMoves(brd);
      shuffle(poss);
      
      for(BreakthroughMove mv : poss){
        char ch = brd.board[mv.endingRow][mv.endingCol];
     	char pl = brd.board[mv.startRow][mv.startCol];
        brd.makeMove(mv);
        
        
        double temp = alphabeta(brd, currDepth+1, alpha, beta);
        
        //System.out.println(gameState.toString());
        
        //reverting move
        brd.board[mv.startRow][mv.startCol] = pl;//PLAYER;
  		brd.board[mv.endingRow][mv.endingCol] = ch;
        brd.numMoves--;
        brd.status = GameState.Status.GAME_ON;
        brd.who = currTurn;

        if(toMaximize && temp > bestScore){
          bestScore = temp;
        }else if(!toMaximize && temp < bestScore){
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

private double evalBoard(BreakthroughState brd) {
	int awayCount = 0;
	int homeCount = 0;
	int numJepA = 0; // number of pieces in jeopardy for away
	int numJepH = 0;
	int maxDistH = 0;
	int maxDistA = 0;
	int totalDistanceA = 0;
	int totalDistanceH = 0;
	
	for(int i = 0; i < BreakthroughState.N; i++){
		for(int j = 0; j < BreakthroughState.N; j++){
			char c = brd.board[i][j];
			if(c == BreakthroughState.homeSym){
				homeCount++;
				totalDistanceH += i;
				if(i > maxDistH) maxDistH = i;
				if(j != 0){
					if(brd.board[i+1][j-1] == BreakthroughState.awaySym) numJepH++;
				}
				if(j != (BreakthroughState.N-1) ){
					if(brd.board[i+1][j+1] == BreakthroughState.awaySym) numJepH++;
				}
				if(brd.board[i+1][j] == BreakthroughState.awaySym) numJepH++;
			}else if(c == BreakthroughState.awaySym){
				awayCount++;
				totalDistanceA += (BreakthroughState.N-1-i);
				if((BreakthroughState.N-1-i) > maxDistA) maxDistA = (BreakthroughState.N-1-i);
				if(j != 0){
					if(brd.board[i-1][j-1] == BreakthroughState.homeSym) numJepA++;
				}
				if(j != (BreakthroughState.N-1) ){
					if(brd.board[i-1][j+1] == BreakthroughState.homeSym) numJepA++;
				}
				if(brd.board[i-1][j] == BreakthroughState.homeSym) numJepA++;
			}else{
				// square was blank
			}
		}
	
	}
	float avgDistanceA = (float)totalDistanceA/awayCount;
	float avgDistanceH = (float)totalDistanceH/homeCount;
	
	float score = COUNT_FACTOR*(homeCount-awayCount) - JEP_FACTOR*(numJepH - numJepA) + 
			MAX_DIST_FACTOR*(maxDistH - maxDistA) + AVG_DIST_FACTOR*(avgDistanceH-avgDistanceA);
	return score;
}
private static ArrayList<BreakthroughMove> getPossibleMoves(BreakthroughState brd){
	
	ArrayList<BreakthroughMove> poss = new ArrayList<>();
	GameState.Who currTurn = brd.who;
	char curry = (currTurn == GameState.Who.HOME)?BreakthroughState.homeSym:BreakthroughState.awaySym;
	int dir = (currTurn == GameState.Who.HOME)?1:-1;
	
	for(int i = 0; i < BreakthroughState.N; i++){
		for(int j = 0; j < BreakthroughState.N; j++){
			if(i+dir!=-1 && i+dir != 8){
			if(brd.board[i][j] == curry){
				if(BreakthroughMove.indexOK(j+1)){
					if(brd.board[i+dir][j+1] != curry){
						BreakthroughMove mv = new BreakthroughMove(i, j, i+dir, j+1);
						if(brd.moveOK(mv)){
							poss.add(mv);
						}
					}
				}
				if(BreakthroughMove.indexOK(j-1)){
					if(brd.board[i+dir][j-1] != curry){
						BreakthroughMove mv3 = new BreakthroughMove(i, j, i+dir, j-1);
						if(brd.moveOK(mv3)){
							poss.add(mv3);
						}
					}
				}
				if(brd.board[i+dir][j] != curry){
					BreakthroughMove mv2 = new BreakthroughMove(i, j, i+dir, j);
		
					if(brd.moveOK(mv2)){
						poss.add(mv2);
					}
				}
			}
			}
		}
	}
	
	return poss;
  }

  private static void shuffle(ArrayList<BreakthroughMove> poss){
	  long seed = System.nanoTime();
	  Collections.shuffle(poss, new Random(seed));
  }

	public static void main(String [] args)
	{
		GamePlayer p = new OurBreakthroughPlayer("COLEMATTHARRISONPOWERHOUR");
		
		p.compete(args);
		//p.solvePuzzles(new String [] {"BTPuzzle1", "BTPuzzle2"});
	}
}
