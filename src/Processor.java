import java.util.ArrayList;

public class Processor {
	Board board;
	LogWriter writer;
	DirectiveFinder finder;
	OutputFormatter format;
	DirectiveHandler handler;
	boolean movementBegun = false;
	
	public Processor(Board board, LogWriter writer, DirectiveFinder finder, OutputFormatter format, DirectiveHandler handler){
		this.board = board;
		this.writer = writer;
		this.finder = finder;
		this.format = format;
		this.handler = handler;
	}
	public void processPlacement(String currentLine) {
		if (movementBegun) {
			writer.writeToFile("Warning: Skipping [" + currentLine + "]. Movement has begun.");
		} else {
			String placement = finder.getPlacementDirective(currentLine);
			board.addNewPiece(placement);
			String placement1 = "Placement: Adding [" + placement + "] " + format.formatPlacement(placement);
			writer.writeToFile(placement1);
		}
	}

	public void processMovement(String currentMovement, boolean isFirstMovement) {
		if (!movementBegun) {
			movementBegun = true;
		}
		boolean movementValid = board.movePiece(currentMovement, isFirstMovement);
		if (movementValid) {
			writer.writeToFile(format.formatMovement(currentMovement, isFirstMovement));
			board.writeBoard();
		} else {
			writer.writeMovementError(currentMovement, isFirstMovement, handler, format, board);
		}
	}

	public void processCastling(String currentLine){
		ArrayList<String> lineAction = finder.getLineAction(currentLine);
		if (lineAction.get(0) != null && lineAction.get(1) != null) {
			if (finder.containsSingleMovement(currentLine)) {
				if (lineAction.size() == 2) {
					if (finder.isCastle(lineAction.get(0))) {
						if (board.isValidCastle(lineAction.get(0), true)) {
							board.castle(true, lineAction.get(0));
							writer.writeToFile(format.formatCastle(lineAction.get(0), true));
						} else {
							writer.writeToFile("This castle is impossible at this time.");
						}
					} else {
						if (board.movePiece(lineAction.get(0), true)) {
							writer.writeToFile(format.formatMovement(lineAction.get(0), true));
						} else {
							writer.writeMovementError(lineAction.get(0), true, handler, format, board);
						}
					}
					if (finder.isCastle(lineAction.get(1))) {
						if (board.isValidCastle(lineAction.get(1), false)) {
							board.castle(false, lineAction.get(1));
							writer.writeToFile(format.formatCastle(lineAction.get(1), false));
						} else {
							writer.writeToFile("This castle is impossible at this time.");
						}
					} else {
						if (board.movePiece(lineAction.get(1), false)) {
							writer.writeToFile(format.formatMovement(lineAction.get(1), false));
						} else {
							writer.writeMovementError(lineAction.get(1), false, handler, format, board);
						}
					}
				}
			} else {
				if (board.isValidCastle(lineAction.get(0), true)) {
					board.castle(true, lineAction.get(0));
					writer.writeToFile(format.formatCastle(lineAction.get(0), true));
				} else {
					writer.writeToFile("This castle is impossible at this time.");
				}
				if (board.isValidCastle(lineAction.get(1), false)) {
					board.castle(false, lineAction.get(1));
					writer.writeToFile(format.formatCastle(lineAction.get(1), false));
				} else {
					writer.writeToFile("This castle is impossible at this time.");
				}
			}
		} else {
			writer.writeToFile(format.getIncorrect(currentLine));
		}
	}
}
