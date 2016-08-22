import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Translator {
	LogWriter writer;
	OutputFormatter format;
	DirectiveFinder finder;
	Board board;
	DirectiveHandler handler;
	boolean beginWithInteractionMode = false;
	BufferedReader file = null;
	int turn;

	public Translator(LogWriter writer, OutputFormatter format, DirectiveFinder finder, Board board,
			DirectiveHandler handler) {
		this.writer = writer;
		this.format = format;
		this.board = board;
		this.handler = handler;
		this.finder = finder;
	}

	public boolean translateFile(Processor process, String fileName) {
		initializeReader(fileName);
		if (!beginWithInteractionMode) {
			try {
				while (file.ready() && !board.isCheckmate() && !board.isInvalidCheckMove()) {
					processCurrentLine(process);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		shutdown();
		return beginWithInteractionMode;
	}

	private void processCurrentLine(Processor process) {
		boolean wasMove = false;
		String currentLine = getCurrentLine().trim();
		if (finder.containsComment(currentLine)) {
			currentLine = finder.removeComment(currentLine).trim();
		}
		if (currentLine.trim().length() > 0) {
			turn = 0;
			if (finder.isPlacement(currentLine)) {
				process.processPlacement(currentLine);
			} else if (finder.isMovement(currentLine, board, handler)) {
				ArrayList<String> movements = finder.getMovementDirectives(currentLine);
				if (!board.isCheckmate() && !board.isInvalidCheckMove()) {
					process.processMovement(movements.get(0), true);
					turn = 1;
				}
				if (movements.size() > 1 && !board.isCheckmate() && !board.isInvalidCheckMove()) {
					process.processMovement(movements.get(1), false);
					turn = 0;
				}
				wasMove = true;
			} else if (finder.containsCastle(currentLine) && !board.isCheckmate()) {
				process.processCastling(currentLine);
				wasMove = true;

			} else {
				writer.writeToFile(format.getIncorrect(currentLine));
			}
		}
		if (wasMove) {
			board.setPostMoveChecks();
		}
	}

	private void initializeReader(String fileName) {
		FileInputStream inputStream;
		try {
			inputStream = new FileInputStream(fileName);
			file = new BufferedReader(new InputStreamReader(inputStream));
			writer.writeToFile("Process: Sucessfully opened file [" + fileName + "]");
		} catch (FileNotFoundException e) {
			writer.writeToFile("Error: There was a problem with the file you entered. Reverting to Interaction Mode.");
			beginWithInteractionMode = true;
		}
	}

	private String getCurrentLine() {
		String currentLine = null;
		try {
			currentLine = file.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return currentLine;
	}

	private void shutdown() {
		try {
			writer.writeToFile("Process: Closing Translation File.");
			if (file != null)
				file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
