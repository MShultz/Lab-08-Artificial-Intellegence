import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class InteractiveHandler {
	Board board;
	LogWriter writer;
	DirectiveHandler handler;
	UserInterface ui;
	OutputFormatter format;
	Processor process;
	boolean quit = false;

	public InteractiveHandler(Board board, LogWriter writer, DirectiveHandler handler, OutputFormatter format,
			Processor process, UserInterface ui) {
		this.board = board;
		this.writer = writer;
		this.handler = handler;
		this.format = format;
		this.process = process;
		this.ui = ui;
	}

	private void setUpBoard() {
		BufferedReader initializer;
		try {
			FileInputStream inputStream = new FileInputStream("src/BoardInitialization.chess");
			initializer = new BufferedReader(new InputStreamReader(inputStream));
			while (initializer.ready()) {
				process.processPlacement(initializer.readLine().trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void initiateInteractionMode(boolean beganWithNewBoard, int whiteTurn) {
		if (!board.isCheckmate() && !board.isInvalidCheckMove() && !board.isStalemate()) {
			writer.writeToFile("----------------------------------");
			writer.writeToFile("Process: Interactive Mode enabled.");
			writer.writeToFile("----------------------------------");
			interactionMode(beganWithNewBoard, whiteTurn);
		}
	}

	private void interactionMode(boolean beganWithNewBoard, int whiteTurn) {
		if (beganWithNewBoard) {
			setUpBoard();
		}
		if(ui.informOfAIMode() == 1)
			aiMode();
		else
			pvp(whiteTurn);
		
		board.printBoardToConsole();
	}
	private void aiMode(){
		System.out.println("Initializing AI mode.");
		int count = 1;
		boolean isWhite = true;
		while (!quit && board.isPlayable() && !board.isStalemate() && !board.isCheckmate()) {
			isWhite = (count % 2 != 0);
			if(isWhite)
				playerMove(isWhite);
			else{
				Node n = new Node(board, this);
				process.processMovement(getCompleteMovement(n.getChoice()), false);
			}
			++count;
			board.setPostMoveChecks();
		}
	}
	private void pvp(int whiteTurn){
		int count = 1 + whiteTurn;
		board.writeBoard();
		while (!quit && board.isPlayable() && !board.isStalemate() && !board.isCheckmate()) {
			boolean isWhite = (count % 2 != 0);
			playerMove(isWhite);
			++count;
			board.setPostMoveChecks();
		}
	}
	
	private void playerMove(boolean isWhite){
		int piece;
		boolean pieceChosen = true;
		
		ArrayList<Piece> pieces = board.getAllPossiblePieces(isWhite);
		King currentPlayerKing = (King) board.getTeamKing(isWhite, board.getBoard());
		if (pieces.size() == 0 && !currentPlayerKing.isCheck()) {
			board.setStalemate(true);
		} else if (pieces.size() == 0 && currentPlayerKing.isCheck()) {
			board.setCheckmate(true);
			board.setWinner(!isWhite);
		}
		if (!board.isStalemate() && !board.isCheckmate()) {
			ui.inform(isWhite);
			do {
				board.printBoardToConsole();
				piece = ui.determinePiece(pieces);
				quit = isQuit(piece);
				if (!quit) {
					ArrayList<Move> possibleMoves = generateMovement(getAllMovesForPiece(pieces, piece, isWhite),
							pieces.get(piece - 1));
					board.printBoardToConsole();
					int move = ui.determineMove(possibleMoves);
					quit = isQuit(move);
					pieceChosen = !(move == 1);
					if (pieceChosen && !quit)
						getCompleteMovementAndProcess(pieces, piece, possibleMoves, move, isWhite);
				}
			} while (!pieceChosen);
		}
	}

	private void getCompleteMovementAndProcess(ArrayList<Piece> pieces, int piece, ArrayList<Move> possibleMoves,
			int move, boolean isWhite) {
		String movement = getCompleteMovement(possibleMoves.get(move - 2));
		if (movement.contains("O")) {
			board.castle(isWhite, movement);
			writer.writeToFile(format.formatCastle(movement, isWhite));
		} else
			process.processMovement(movement, isWhite);
	}

	private boolean isQuit(int choice) {
		return choice == 0;
	}

	private ArrayList<Position> getAllMovesForPiece(ArrayList<Piece> pieces, int piece, boolean isWhite) {
		Piece current = pieces.get(piece - 1);
		ArrayList<Position> possibleMoves = current.getMovement(board.getBoard(),
				(current.getType() == PieceType.PAWN ? false : true));
		possibleMoves = board.getNonCheckMovements(possibleMoves, current,
				(King) board.getTeamKing(current.isWhite(), board.getBoard()));
		if (current.getType() == PieceType.KING || current.getType() == PieceType.ROOK) {
			if (board.isValidCastle("O-O-O", isWhite)
					&& current.getCurrentPosition().equals(board.getRookPosition(isWhite, false)))
				possibleMoves.add(new Position(-1, -1));
			if (board.isValidCastle("O-O", isWhite)
					&& current.getCurrentPosition().equals(board.getRookPosition(isWhite, true)))
				possibleMoves.add(new Position(8, 8));
		}
		return possibleMoves;
	}
	
	public ArrayList<Position> getAllMovesForPiece(Piece p) {
		ArrayList<Position> possibleMoves = p.getMovement(board.getBoard(),
				(p.getType() == PieceType.PAWN ? false : true));
		possibleMoves = board.getNonCheckMovements(possibleMoves, p,
				(King) board.getTeamKing(p.isWhite(), board.getBoard()));
		if (p.getType() == PieceType.KING || p.getType() == PieceType.ROOK) {
			if (board.isValidCastle("O-O-O", p.isWhite())
					&& p.getCurrentPosition().equals(board.getRookPosition(p.isWhite(), false)))
				possibleMoves.add(new Position(-1, -1));
			if (board.isValidCastle("O-O", p.isWhite())
					&&p.getCurrentPosition().equals(board.getRookPosition(p.isWhite(), true)))
				possibleMoves.add(new Position(8, 8));
		}
		return possibleMoves;
	}

	private String getCompleteMovement(Move move) {
		String movement;
		if (move.getTravelPosition().file == -1)
			movement = "O-O-O";
		else if (move.getTravelPosition().file == 8)
			movement = "O-O";
		else {
			Piece[][] currentBoard = board.getBoard();
			Piece piece = move.getPiece();
			Position currentPosition = move.getCurrentPosition();
			Position travelPostition = move.getTravelPosition();
			movement = "" + (piece.getType() == PieceType.PAWN ? "" : piece.getType().getWhiteType())
					+ Character.toLowerCase(ui.getFileLetter(currentPosition.getFile()))
					+ (currentPosition.getRank() + 1);
			movement += (currentBoard[travelPostition.getRank()][travelPostition.getFile()] == null ? "-" : "x");
			movement += Character.toLowerCase(ui.getFileLetter(travelPostition.getFile()));
			movement += (move.getTravelPosition().getRank() + 1);
			piece.setCurrentPosition(travelPostition);
			if (board.isCheck(
					board.moveSinglePiece(currentPosition, travelPostition, board.copyArray(board.getBoard()), piece),
					piece, (King) board.getTeamKing(!piece.isWhite(), currentBoard))) {
				Piece[][] checkBoard = board.moveSinglePiece(currentPosition, travelPostition,
						board.copyArray(currentBoard), piece);
				if (board.isCheckmate(!piece.isWhite(), checkBoard, false)) {
					movement += "#";
				} else {
					movement += "+";
				}
			}
			piece.setCurrentPosition(currentPosition);
		}
		return movement;
	}

	public ArrayList<Move> generateMovement(ArrayList<Position> possibleMoves, Piece piece) {
		ArrayList<Move> moves = new ArrayList<>();
		for (Position pos : possibleMoves) {
			if (pos.getRank() != -1 || pos.getRank() != 8)
				moves.add(new Move(piece, piece.getCurrentPosition(), pos, board));
			else
				moves.add(new Move(piece, piece.getCurrentPosition(), pos));
		}
		return moves;
	}

}
