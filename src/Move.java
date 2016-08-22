
public class Move {
	private Piece piece;
	private Position currentPosition;
	private Position travelPosition;
	private MoveType type;
	
	public Move(Piece piece, Position currentPosition, Position travelPosition, Board board){
		this.piece = piece;
		this.currentPosition = currentPosition;
		this.travelPosition = travelPosition;
		generateMoveType(board);
	}
	public Move(Piece piece, Position currentPosition, Position travelPosition){
		this.piece = piece;
		this.currentPosition = currentPosition;
		this.travelPosition = travelPosition;
	}
	public Piece getPiece() {
		return piece;
	}

	public Position getCurrentPosition() {
		return currentPosition;
	}

	public Position getTravelPosition() {
		return travelPosition;
	}

	public MoveType getType() {
		return type;
	}
	
	private void generateMoveType(Board board){
		Piece[][] currentBoard = board.moveSinglePiece(currentPosition, travelPosition, board.copyArray(board.getBoard()), piece);
		Position current = piece.getCurrentPosition();
		piece.setCurrentPosition(travelPosition);
		King opposingKing = (King)board.getTeamKing(!piece.isWhite(), currentBoard);
		boolean wasCheck = opposingKing.isCheck();
		
		if(board.isCheck(currentBoard, piece,opposingKing) && board.getAllPossiblePieces(!piece.isWhite()).size() != 0)
			type = MoveType.CHECK;
		else if(board.isCheck(currentBoard, piece,opposingKing) && board.getAllPossiblePieces(!piece.isWhite()).size() == 0)
			type = MoveType.CHECKMATE;
		else if(board.getBoard()[travelPosition.getRank()][travelPosition.getFile()] != null && !board.isCheck(currentBoard, piece,opposingKing))
			type = MoveType.CAPTURE;
		else if(board.isStalemate(piece.isWhite(), currentBoard, opposingKing.isCheck()))
			type = MoveType.STALEMATE;
		else
			type = MoveType.MOVE;
		
		opposingKing.setCheck(wasCheck);
		piece.setCurrentPosition(current);
	}
}
