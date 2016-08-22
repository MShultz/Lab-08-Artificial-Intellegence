public class Main {

	public static void main(String[] args) {
		Game game = (args.length > 0? new Game(args[0], true): new Game(null, false));
		game.play();
	}

}
