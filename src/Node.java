import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class Node {
	InteractiveHandler iHandle;
	Board board;
	private int depth;
	private boolean isLeaf;
	private int value;
	private HashSet<Node> children;
	private boolean isMin;
	private Move nodeMove;
	private LinkedList<Node> pathToHead;
	private Piece[][] currentBoard;
	private Piece[][] previousBoard;
	private Move choice;

	public Node(Board board, InteractiveHandler iHandle) {
		depth = 3;
		this.currentBoard = board.copyArray(board.getBoard());
		this.iHandle = iHandle;
		this.isLeaf = false;
		this.board = board;
		this.isMin = false;
		this.children = populateChildren();
		this.pathToHead.add(this);
		this.choice = setChoice();
	}

	public Node(Move nodeMove, int depth, boolean parentIsMin, LinkedList<Node> pathToHead, Board board,
			Piece[][] currentBoard, InteractiveHandler iHandle) {
		this.iHandle = iHandle;
		this.depth = depth;
		this.nodeMove = nodeMove;
		this.isLeaf = (depth == 0);
		this.isMin = !parentIsMin;
		this.pathToHead = pathToHead;
		this.board = board;
		this.value = getValue();
		this.previousBoard = board.copyArray(currentBoard);
		this.currentBoard = board.moveSinglePiece(nodeMove.getCurrentPosition(), nodeMove.getTravelPosition(),
				board.copyArray(currentBoard), nodeMove.getPiece());
		if (!isLeaf)
			this.children = populateChildren();
		this.pathToHead.add(this);
	}

	public boolean isMin() {
		return isMin;
	}

	public Move getMove() {
		return nodeMove;
	}

	private HashSet<Node> populateChildren() {
		ArrayList<Move> allPossibleMoves = new ArrayList<Move>();
		ArrayList<Piece> allPossiblePieces = board.getAllPossiblePieces(isMin, currentBoard);
		HashSet<Node> childrenNodes = new HashSet<>();
		for (Piece p : allPossiblePieces) {
			allPossibleMoves.addAll(iHandle.generateMovement(iHandle.getAllMovesForPiece(p), p));
		}
		for (Move m : allPossibleMoves) {
			childrenNodes.add(new Node(m, depth - 1, isMin, pathToHead, board, currentBoard, iHandle));
		}
		return childrenNodes;
	}

	private int getValue(){
		int value = 0;
		if(isLeaf)
			value = getHeuristicValue();
		else if(isMin){
			value = getMinValue();
		}else{
			value = getMaxValue();
		}
		return value;
	}

	private int getHeuristicValue() {
		int value = 0;
		for (Node n : pathToHead) {
			MoveType type = n.getMove().getType();
			if (type == MoveType.CAPTURE) {
				int valueOfPiece = previousBoard[n.getMove().getTravelPosition().getRank()][n.getMove()
						.getTravelPosition().getFile()].getType().getValue();
				value = (n.isMin() ? value - valueOfPiece : value + valueOfPiece);
			} else if (type == MoveType.CHECKMATE) {
				value = (n.isMin() ? value - 1000 : value + 1000);
			}
		}
		return value;
	}
	private int getMinValue(){
		value = 2000;
		for(Node n: children){
			if(n.getValue() < value)
				value = n.getValue();
		}
		return value;
	}
	
	private int getMaxValue(){
		value = -2000;
		for(Node n: children){
			if(n.getValue() > value)
				value = n.getValue();
		}
		return value;
	}
	private Move setChoice(){
		Move m = null;
		int value = -2000;
		for(Node n: children){
			if(n.getValue() > value){
				value = n.getValue();
				m = n.getMove();
			}
		}
		return m;
	}
}