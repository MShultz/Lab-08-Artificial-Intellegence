
public enum PieceType {
	KING('K', 'k', 0), QUEEN('Q', 'q', 9), ROOK('R', 'r', 5), KNIGHT('N', 'n', 3), BISHOP('B', 'b', 3), PAWN('P', 'p', 1);
	private char whiteType;
	private char blackType;
	private int value;

	private PieceType(char whiteType, char blackType, int value) {
		this.whiteType = whiteType;
		this.blackType = blackType;
		this.value = value;
	}

	public char getWhiteType() {
		return whiteType;
	}

	public char getBlackType() {
		return blackType;
	}

	public int getValue() {
		return value;
	}

}
