import java.io.IOException;

public class Game {
	boolean containedFile;
	String fileName;
	LogWriter writer;
	Board board;
	UserInterface ui;
	DirectiveHandler handler;
	OutputFormatter format;
	DirectiveFinder finder;
	Processor process;
	Translator translationHandler;
	InteractiveHandler interactionHandler;
	public Game(String fileName, boolean containedFile){
		this.fileName = fileName;
		this.containedFile = containedFile;
		writer = new LogWriter();
		ui = new UserInterface();
		handler = new DirectiveHandler();
		format = new OutputFormatter();
		finder = new DirectiveFinder();
		board = new Board(writer);
		translationHandler = new Translator(writer, format, finder, board, handler);
		process = new Processor(board, writer, finder, format, handler);
		interactionHandler = new InteractiveHandler(board, writer, handler, format, process, ui);	
	}
	public void play(){
		if (containedFile) {
			boolean beginWithInteraction = translationHandler.translateFile(process, fileName);
			interactionHandler.initiateInteractionMode(beginWithInteraction, translationHandler.turn);
		} else {
			writer.writeToFile("Process: You entered no filepath. The program will now revert to Interaction Mode.");
			interactionHandler.initiateInteractionMode(true, 0);
		}
		endGame();	
	}
	private void endGame() {
		String gameEnding = (board.isStalemate() ? ui.informOfStalemate()
				: board.isCheckmate() ? ui.informOfCheckmate(board.isWinner())
						: board.isInvalidCheckMove() ? ui.informOfInvalid() : "The game has been chosen to end.");
		writer.writeToFile(gameEnding);
		System.out.println(gameEnding);
		if (!board.isCheckmate())
			board.writeBoard();
		try {
			writer.closeLogFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
