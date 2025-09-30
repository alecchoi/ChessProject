package chess;

import java.util.ArrayList;

public class Chess {

        enum Player { white, black }
        private enum Color { WHITE, BLACK }
    	private enum PType { P, R, N, B, Q, K }


		// Directions for sliders/leapers
		private static final int[][] ORTHO      = {{1,0},{-1,0},{0,1},{0,-1}};
		private static final int[][] DIAG       = {{1,1},{1,-1},{-1,1},{-1,-1}};
		private static final int[][] ORTHO_DIAG = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
		private static final int[][] KNIGHT     = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}};
		private static final int[][] KING_1     = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};

		    // Coordinate helpers: file=a..h -> col=0..7, rank=1..8 -> row=7..0
		private static int fileToCol(char f){ return f - 'a'; }
		private static int rankToRow(char r){ return 7 - (r - '1'); }
		private static char colToFile(int c){ return (char)('a' + c); }
		private static int  rowToRank(int r){ return 8 - r; }
		private static boolean onBoard(int r,int c){ return r>=0 && r<8 && c>=0 && c<8; }

		    // Base piece types 
		private static abstract class Piece {
			final Color color;
			final PType type;
			Piece(PType t, Color c)
			{ 
				type=t; 
				color=c; 
			}
			abstract boolean canReachGeom(GameState g, int fr, int fc, int tr, int tc);
		}


		private static abstract class SlidingPiece extends Piece {
			final int[][] dirs;
			SlidingPiece(PType t, Color c, int[][] dirs){
				super(t,c); 
				this.dirs = dirs; 
				}
			@Override
			boolean canReachGeom(GameState g, int fr, int fc, int tr, int tc) {
				for (int[] d: dirs){
					int r=fr+d[0], c=fc+d[1];
					while (onBoard(r,c)){
						if (r==tr && c==tc) return true;
						if (g.board[r][c] != null) break; // blocked
						r+=d[0]; c+=d[1];
					}
				}
				return false;
			}
		}

		private static abstract class LeaperPiece extends Piece {
			final int[][] deltas;
			LeaperPiece(PType t, Color c, int[][] deltas){
				 super(t,c); 
				 this.deltas = deltas; 
				}
			@Override
			boolean canReachGeom(GameState g, int fr, int fc, int tr, int tc) {
				for (int[] d: deltas){
					if (fr + d[0] == tr && fc + d[1] == tc) return true;
				}
				return false;
			}
		}


		private static final class Rook  extends SlidingPiece { 
			Rook(Color c){ 
				super(PType.R,c,ORTHO); 
			} 
		}
		private static final class Bishop extends SlidingPiece { Bishop(Color c){ super(PType.B,c,DIAG); } }
		private static final class Queen  extends SlidingPiece { Queen(Color c){ super(PType.Q,c,ORTHO_DIAG); } }
		private static final class Knight extends LeaperPiece   { Knight(Color c){ super(PType.N,c,KNIGHT); } }
		private static final class King   extends LeaperPiece   { King(Color c){ super(PType.K,c,KING_1); } }


		private static final class Pawn extends Piece {
				Pawn(Color c){ super(PType.P,c); }
				@Override
				boolean canReachGeom(GameState g, int fr,int fc,int tr,int tc){
					// Geometry only; occupancy/en-passant handled later
					int dir = (color == Color.WHITE) ? -1 : +1;
					// single push
					if (tc == fc && tr == fr + dir) return true;
					// double push from start (rank 2 for white -> row 6; rank 7 for black -> row 1)
					int startRow = (color == Color.WHITE) ? rankToRow('2') : rankToRow('7');
					if (fr == startRow && tc == fc && tr == fr + 2*dir) return true;
					// diagonal capture
					if (tr == fr + dir && Math.abs(tc - fc) == 1) return true;
					return false;
				}
			}

		private static final class GameState {
			Piece[][] board = new Piece[8][8];
			Player sideToMove = Player.white;

			// Castling/en passant (we’ll wire these into rules later)
			boolean wCastleK = true, wCastleQ = true, bCastleK = true, bCastleQ = true;
			Integer enPassantFile = null; // 0..7 for one ply after a double step
		}

		private static GameState STATE = new GameState();


	/**
	 * Plays the next move for whichever player has the turn.
	 * 
	 * @param move String for next move, e.g. "a2 a3"
	 * 
	 * @return A ReturnPlay instance that contains the result of the move.
	 *         See the section "The Chess class" in the assignment description for details of
	 *         the contents of the returned ReturnPlay instance.
	 */
	public static ReturnPlay play(String move) {

		/* FILL IN THIS METHOD */
		
		/* FOLLOWING LINE IS A PLACEHOLDER TO MAKE COMPILER HAPPY */
		/* WHEN YOU FILL IN THIS METHOD, YOU NEED TO RETURN A ReturnPlay OBJECT */
       	ReturnPlay out = new ReturnPlay();
        out.piecesOnBoard = toReturnPieces(STATE);
        out.message = null;
        return out;	}


	private static void setupInitialPosition(GameState g){
        // Clear
        for (int r=0;r<8;r++) for (int c=0;c<8;c++) g.board[r][c]=null;

        // Back ranks
        int wr = rankToRow('1'), br = rankToRow('8');
        g.board[wr][0]=new Rook(Color.WHITE);   g.board[br][0]=new Rook(Color.BLACK);
        g.board[wr][1]=new Knight(Color.WHITE); g.board[br][1]=new Knight(Color.BLACK);
        g.board[wr][2]=new Bishop(Color.WHITE); g.board[br][2]=new Bishop(Color.BLACK);
        g.board[wr][3]=new Queen(Color.WHITE);  g.board[br][3]=new Queen(Color.BLACK);
        g.board[wr][4]=new King(Color.WHITE);   g.board[br][4]=new King(Color.BLACK);
        g.board[wr][5]=new Bishop(Color.WHITE); g.board[br][5]=new Bishop(Color.BLACK);
        g.board[wr][6]=new Knight(Color.WHITE); g.board[br][6]=new Knight(Color.BLACK);
        g.board[wr][7]=new Rook(Color.WHITE);   g.board[br][7]=new Rook(Color.BLACK);

        // Pawns
        int wpr = rankToRow('2'), bpr = rankToRow('7');
        for (int c=0;c<8;c++){
            g.board[wpr][c]=new Pawn(Color.WHITE);
            g.board[bpr][c]=new Pawn(Color.BLACK);
        }

        g.sideToMove = Player.white;
        g.wCastleK=g.wCastleQ=g.bCastleK=g.bCastleQ=true;
        g.enPassantFile=null;
    }
	 private static ArrayList<ReturnPiece> toReturnPieces(GameState g){
        ArrayList<ReturnPiece> list = new ArrayList<>();
        for (int row=0; row<8; row++){
            for (int col=0; col<8; col++){
                Piece p = g.board[row][col];
                if (p == null) continue;

                ReturnPiece rp = new ReturnPiece();
                rp.pieceFile = ReturnPiece.PieceFile.valueOf(String.valueOf(colToFile(col)));
                rp.pieceRank = rowToRank(row);
                rp.pieceType = mapToReturnPieceType(p);
                list.add(rp);
            }
        }
        return list;
    }

	    private static ReturnPiece.PieceType mapToReturnPieceType(Piece p){
        boolean w = (p.color == Color.WHITE);
        switch (p.type){
            case P: return w ? ReturnPiece.PieceType.WP : ReturnPiece.PieceType.BP;
            case R: return w ? ReturnPiece.PieceType.WR : ReturnPiece.PieceType.BR;
            case N: return w ? ReturnPiece.PieceType.WN : ReturnPiece.PieceType.BN;
            case B: return w ? ReturnPiece.PieceType.WB : ReturnPiece.PieceType.BB;
            case Q: return w ? ReturnPiece.PieceType.WQ : ReturnPiece.PieceType.BQ;
            case K: return w ? ReturnPiece.PieceType.WK : ReturnPiece.PieceType.BK;
            default: throw new IllegalStateException("Unknown piece");
        	}
		}
	/**
	 * This method should reset the game, and start from scratch.
	 */
	public static void start() {
		/* FILL IN THIS METHOD */
		STATE = new GameState();
        setupInitialPosition(STATE);
	}
}
