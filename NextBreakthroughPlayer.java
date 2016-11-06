/* Copyright (C) Mike Zmuda - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Mike Zmuda <zmudam@miamioh.edu>, 2010-2015
 */

package Breakthrough;

import game.*;

public class NextBreakthroughPlayer extends GamePlayer {
	public final int MAX_DEPTH = 50;
	public int depthLimit;

	// mvStack is where the search procedure places it's move recommendation.
	// If the search is at depth, d, the move is stored on mvStack[d].
	// This was done to help efficiency (i.e., reduce number constructor calls)
	// (Not sure how much it improves things.)
	protected ScoredBreakthroughMove[] mvStack;

	// A BreakthroughMove with a scored (how well it evaluates)
	protected class ScoredBreakthroughMove extends BreakthroughMove {
		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2, double s) {
			super(r1,c1,r2,c2);
			score = s;
		}
		public Object clone()
    	{ return new BreakthroughMove(startRow, startCol, endingRow, endingCol, score); }
		public void set(int r1, int c1, int r2, int c2, double s{
			startRow = r1; startCol = c1; endingRow = r2; endingCol = c2;
			if (!indexOK(startRow) || !indexOK(startCol) ||
			!indexOK(endingRow) || !indexOK(endingCol)) {
				System.err.println("problem in Breakthrough ctor");
				score = -MAX_SCORE;
			}else{	
				score = s;
			}
		}
		public double score;
	}

	public NextBreakthroughPlayer(String nname, int d) {
		super(nname, "Breakthrough");
		depthLimit = d;
	}

	protected static void shuffle(int[] ary) {
		int len = ary.length;
		for (int i = 0; i < len; i++) {
			int spot = Util.randInt(i, len - 1);
			int tmp = ary[i];
			ary[i] = ary[spot];
			ary[spot] = tmp;
		}
	}

	/**
	 * Initializes the stack of Moves.
	 */
	public void init() {
		mvStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			mvStack[i] = new ScoredBreakthroughMove(0, 0, 0, 0, 0);
		}
	}

	/**
	 * Determines if a board represents a completed game. If it is, the
	 * evaluation values for these boards is recorded (i.e., 0 for a draw +X,
	 * for a HOME win and -X for an AWAY win.
	 * 
	 * @param brd
	 *            Breakthrough board to be examined
	 * @param mv
	 *            where to place the score information; column is irrelevant
	 * @return true if the brd is a terminal state
	 */
	protected boolean terminalValue(GameState brd, ScoredBreakthroughMove mv) {
		GameState.Status status = brd.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			mv.set(0,0,0,0, MAX_SCORE);
		} else if (status == GameState.Status.AWAY_WIN) {
			mv.set(0,0,0,0, -MAX_SCORE);
		} else if (status == GameState.Status.DRAW) {
			mv.set(0,0,0,0, 0);
		} else {
			isTerminal = false;
		}
		return isTerminal;
	}

	/**
	 * Performs the a depth limited minimax algorithm. It leaves it's move
	 * recommendation at mvStack[currDepth].
	 * 
	 * @param brd
	 *            current board state
	 * @param currDepth
	 *            current depth in the search
	 */
	private void minimax(BreakthroughState brd, int currDepth) {
		boolean toMaximize = (brd.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(brd, mvStack[currDepth]);

		if (isTerminal) {
			;
		} else if (currDepth == depthLimit) {
			mvStack[currDepth].set(0,,0,0,0 evalBoard(brd));
		} else {
			ScoredBreakthroughMove tempMv = new ScoredBreakthroughMove(0,0,0,0, 0);

			double bestScore = (brd.getWho() == GameState.Who.HOME ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);
			ScoredBreakthroughMove bestMove = mvStack[currDepth];
			ScoredBreakthroughMove nextMove = mvStack[currDepth + 1];

			bestMove.set(0,0,0,0, bestScore);
			GameState.Who currTurn = brd.getWho();

			ArrayList<BreakthroughMove> mvs = getAllPossibleMoves();

			for (BreakthroughMove a : mvs) {			
				// Make move on board
				tempMv.set(a);
				char r = brd.board[tempMv.startRow][]
				brd.makeMove(tempMv);

				// Check out worth of this move
				minimax(brd, currDepth + 1);

				// Undo the move
				brd.board;
				int row = brd.numInCol[c];
				brd.board[row][c] = BreakthroughState.emptySym;
				brd.numMoves--;
				brd.status = GameState.Status.GAME_ON;
				brd.who = currTurn;

				// Check out the results, relative to what we've seen before
				if (toMaximize && nextMove.score > bestMove.score) {
					bestMove.set(c, nextMove.score);
				} else if (!toMaximize && nextMove.score < bestMove.score) {
					bestMove.set(c, nextMove.score);
				}
				
			}
		}
	}

	public GameMove getMove(GameState brd, String lastMove) {
		minimax((BreakthroughState) brd, 0);
		return mvStack[0];
	}

	private ArrayList<BreakthroughMove> getAllPossibleMoves(BreakthroughState board) {
		ArrayList<BreakthroughMove> list = new ArrayList<BreakthroughMove>(); 
		BreakthroughMove mv = new BreakthroughMove();
		int dir = board.who == GameState.Who.HOME ? +1 : -1;
		for (int r=0; r<BreakthroughState.N; r++) {
			for (int c=0; c<BreakthroughState.N; c++) {
				mv.startRow = r;
				mv.startCol = c;
				mv.endingRow = r+dir; mv.endingCol = c;
				if (board.moveOK(mv)) {
					list.add((BreakthroughMove)mv.clone());
				}
				mv.endingRow = r+dir; mv.endingCol = c+1;
				if (board.moveOK(mv)) {
					list.add((BreakthroughMove)mv.clone());
				}
				mv.endingRow = r+dir; mv.endingCol = c-1;
				if (board.moveOK(mv)) {
					list.add((BreakthroughMove)mv.clone());
				}
			}
		}
		Collections.shuffle(list);
		return list;
	}

	public static void main(String[] args) {
		int depth = 6;
		GamePlayer p = new MiniMaxBreakthroughPlayer("C4 MM F1 " + depth, depth);
		p.compete(args);
	}

}
