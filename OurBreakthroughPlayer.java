
/* Written by Cole Hoffbauer, Harrison Roth, and Matt DePero */

package breakthrough;
import game.*;
import java.util.*;

class ScoredBreakthroughMove extends BreakthroughMove {
  public double score;
  public ScoredBreakthroughMove(int r, int c, int r2, int c2, double s){
    super(r,c,r2,c2);
    score = s;
  }
  public void set(BreakthroughMove mv){
    startRow = mv.startRow;
    startCol = mv.startCol;
    endingRow = mv.endingRow;
    endingCol = mv.endingCol;
  }
  public void set(BreakthroughMove mv, double s){
    startRow = mv.startRow;
    startCol = mv.startCol;
    endingRow = mv.endingRow;
    endingCol = mv.endingCol;
    score = s;
  }
  public void set(int r, int c, int r2, int c2, double s){
    startRow = r;
    startCol = c;
    endingRow = r2;
    endingCol = c2;
    score = s;
  }
  public void setScore(double s){
	  score = s;
  }
}

public class OurBreakthroughPlayer extends GamePlayer {
	public int depthLimit = 5;
	public final int MAX_SCORE = 1000; // Fix this later
	
	public static final float COUNT_FACTOR = 0.5f;
	public static final float JEP_FACTOR  = 0.0f;
	public static final float MAX_DIST_FACTOR = 0.2f;
	public static final float AVG_DIST_FACTOR = 0.1f;

	protected ScoredBreakthroughMove [] moves;
	
	public OurBreakthroughPlayer(String n)
	{
		super(n, "Breakthrough");
		moves = new ScoredBreakthroughMove[depthLimit+1];
		for(int i = 0; i < moves.length; i++){
			moves[i] = new ScoredBreakthroughMove(0,0,0,0,0);
		}
		
	}
	public GameMove getMove(GameState brd, String lastMove)
	{
//		int i = 1;
//		int depth = depthLimit;
//		long start = System.currentTimeMillis();
//	    double bestScore = (brd.getWho() == GameState.Who.HOME) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
//	    depthLimit = i;
//		while(i < depth && (start-System.currentTimeMillis())< 400 && moves[0].score!=bestScore){
			alphabeta((BreakthroughState)brd, 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//			i++;
//			depthLimit = i;
//		}
		return moves[0];
		
	}

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
    	moves[currDepth].set(0,0,0,0, bestScore );
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
  		if (bestMove.score <= alpha || bestMove.score == -MAX_SCORE) {
  			return;
  		}
        } else {
        	alpha = Math.max(bestMove.score, alpha);
	  		if (bestMove.score >= beta || bestMove.score == MAX_SCORE) {
	  			return;
	  		}
        }
      }
      
 
    }
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
	
	for(int i = 0; i < brd.N; i++){
		for(int j = 0; j < brd.N; j++){
			char c = brd.board[i][j];
			if(c == BreakthroughState.homeSym){
				homeCount++;
				totalDistanceH += i;
				if(i > maxDistH) maxDistH = i;
				/*if(j != 0){
					if(brd.board[i+1][j-1] == BreakthroughState.awaySym) numJepH++;
				}
				if(j != (brd.N-1) ){
					if(brd.board[i+1][j+1] == BreakthroughState.awaySym) numJepH++;
				}
				if(brd.board[i+1][j] == BreakthroughState.awaySym) numJepH++;*/
			}else if(c == BreakthroughState.awaySym){
				awayCount++;
				totalDistanceA += (brd.N-1-i);
				if((brd.N-1-i) > maxDistA) maxDistA = (brd.N-1-i);
				/*if(j != 0){
					if(brd.board[i-1][j-1] == BreakthroughState.homeSym) numJepA++;
				}
				if(j != (brd.N-1) ){
					if(brd.board[i-1][j+1] == BreakthroughState.homeSym) numJepA++;
				}
				if(brd.board[i-1][j] == BreakthroughState.homeSym) numJepA++;*/
			}else{
				// square was blank
			}
			/*char c = brd.board[i][j];
			if(c == BreakthroughState.homeSym){
				homeCount++;
			}else if(c == BreakthroughState.awaySym){
				awayCount++;
			}else{
				
			}*/
		}
	
	}
	float avgDistanceA = (float)totalDistanceA/awayCount;
	float avgDistanceH = (float)totalDistanceH/homeCount;
	
	float score = COUNT_FACTOR*(homeCount-awayCount) - JEP_FACTOR*(numJepH - numJepA) + 
			MAX_DIST_FACTOR*(maxDistH - maxDistA) + AVG_DIST_FACTOR*(avgDistanceH-avgDistanceA);
	return score;
	//return homeCount-awayCount;
}
private static ArrayList<BreakthroughMove> getPossibleMoves(BreakthroughState brd){
	
	ArrayList<BreakthroughMove> poss = new ArrayList<>();
	GameState.Who currTurn = brd.who;
	char curry = (currTurn == GameState.Who.HOME)?BreakthroughState.homeSym:BreakthroughState.awaySym;
	int dir = (currTurn == GameState.Who.HOME)?1:-1;
	
	for(int i = 0; i < brd.N; i++){
		for(int j = 0; j < brd.N; j++){
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
