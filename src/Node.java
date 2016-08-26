import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class Node {
	InteractiveHandler iHandle;
	Board board;
	private int depth;
	private boolean isLeaf;
	private boolean isRoot;
	private int value;
	private HashSet<Node> children;
	private boolean isMin;
	private Move nodeMove;
	private LinkedList<Node> pathToHead = new LinkedList<Node>();
	public Piece[][] currentBoard;
	private int heuristicValue;
	public Piece[][] previousBoard;

	public Node(Board board, InteractiveHandler iHandle) {
		depth = 3;
		this.currentBoard = board.copyArray(board.getBoard());
		this.iHandle = iHandle;
		this.isLeaf = false;
		this.isRoot = true;
		this.board = board;
		this.isMin = false;
		this.pathToHead.add(this);
		this.children = populateChildren(this.isMin);

	}

	public Node(Move nodeMove, int depth, boolean parentIsMin, LinkedList<Node> pathToHead, Board board,
			Piece[][] currentBoard, InteractiveHandler iHandle) {
		this.iHandle = iHandle;
		this.depth = depth;
		this.nodeMove = nodeMove;
		this.isMin = (depth == 2 ? parentIsMin : !parentIsMin);
		this.isLeaf = (depth == 0 || nodeMove.getType() == MoveType.CHECKMATE);
		this.isRoot = false;
		this.pathToHead = pathToHead;
		this.board = board;
		if (isLeaf)
			this.heuristicValue = getHeuristicValue();
		this.pathToHead.add(this);
		this.previousBoard = board.copyArray(currentBoard);
		this.currentBoard = board.moveSinglePiece(nodeMove.getCurrentPosition(), nodeMove.getTravelPosition(),
				board.copyArray(currentBoard), nodeMove.getPiece());

		if (!isLeaf)
			this.children = populateChildren();
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
			allPossibleMoves.addAll(iHandle.generateMovement(iHandle.getAllMovesForPiece(p, true), p));
		}
		for (Move m : allPossibleMoves) {
			childrenNodes.add(new Node(m, depth - 1, isMin, pathToHead, board, currentBoard, iHandle));
		}
		return childrenNodes;
	}

	private HashSet<Node> populateChildren(boolean isMinimum) {
		ArrayList<Move> allPossibleMoves = new ArrayList<Move>();
		ArrayList<Piece> allPossiblePieces = board.getAllPossiblePieces(isMinimum, currentBoard);
		HashSet<Node> childrenNodes = new HashSet<>();
		for (Piece p : allPossiblePieces) {
			allPossibleMoves.addAll(iHandle.generateMovement(iHandle.getAllMovesForPiece(p, true), p));
		}
		if (board.isCheck((King) board.getTeamKing(false, currentBoard))) {
			for (Move m : allPossibleMoves) {
				System.out.println(m.getPiece() + " " + m.getTravelPosition());
			}
		}
		for (Move m : allPossibleMoves) {
			childrenNodes.add(new Node(m, depth - 1, isMin, pathToHead, board, currentBoard, iHandle));
		}
		return childrenNodes;
	}

	private int getValue() {
		int value = 0;
		if (isLeaf)
			value = heuristicValue;
		else if (isMin) {
			value = getMinValue();
		} else {
			value = getMaxValue();
		}
		return value;
	}

	private int getHeuristicValue() {
		int value = 0;
		for (Node n : pathToHead) {
			if (!n.isRoot) {
				MoveType type = n.getMove().getType();
				if (type == MoveType.CAPTURE) {
					int valueOfPiece = n.currentBoard[n.getMove().getTravelPosition().getRank()][n.getMove()
							.getTravelPosition().getFile()].getType().getValue();
					value = (n.isMin() ? value + valueOfPiece : value - valueOfPiece);
				} else if (type == MoveType.CHECKMATE) {
					value = (n.isMin() ? value + 1000 : value - 1000);
				}
			}
		}
		return value;
	}

	private int getMinValue() {
		value = 2000;
		for (Node n : children) {
			if (n.getValue() < value)
				value = n.getValue();
		}
		return value;
	}

	private int getMaxValue() {
		value = -2000;
		for (Node n : children) {
			if (n.getValue() > value)
				value = n.getValue();
		}
		return value;
	}

	public Move getChoice() {
		Move m = null;
		if (immenentCapture()) {
			m = getBestCaptureMove();
		}
		if (m == null) {
			m = getBestCalculateWithoutCapture();
		}
		if (m == null) {
			m = getRandomMove();
			System.out.println("Random");
		}
		return m;
	}

	private Move getRandomMove() {
		System.out.println("Random");
		Move m = null;
		int choice = new Random().nextInt(children.size() - 1);
		int count = 0;
		for (Node n : children) {
			if (count == choice) {
				m = n.getMove();
			}
			++count;
		}
		System.out.println("Rank: " + m.getTravelPosition().getRank() + " File: " + m.getTravelPosition().getFile());
		return m;
	}

	private Move getBestCalculateWithoutCapture() {
		Move m = null;
		this.value = getValue();
		int value = -2000;
		for (Node n : children) {
			if (!n.isLeaf) {
				int nodeVal = n.getValue();
				if ((nodeVal > value && !couldBeCapture(n))
						|| (nodeVal > value && couldBeCapture(n) && betterThanLoss(n))) {
					if (n.getMove() != null) {
						value = nodeVal;
						m = n.getMove();
					}
				}
			}
		}
		if (m != null)
			System.out.println("Best");
		return m;
	}

	private boolean couldBeCapture(Node node) {
		boolean canCap = false;
		for (Node n : node.children) {
			if (n.getMove().getType() == MoveType.CAPTURE) {
				canCap = true;
			}
		}
		return canCap;
	}

	private boolean immenentCapture() {
		boolean capture = false;
		for (Node n : children) {
			if (n.getMove().getType() == MoveType.CAPTURE)
				capture = true;
		}
		return capture;
	}

	private Move getBestCaptureMove() {
		Move m = null;
		int value = -2000;
		for (Node n : children) {
			if (n.getMove().getType() == MoveType.CAPTURE && n.getValue() > value) {
				if (betterThanLoss(n)) {
					value = n.getValue();
					m = n.getMove();
				}
			}
		}
		return m;
	}

	private boolean betterThanLoss(Node n) {
		boolean better = true;
		Position travel = n.getMove().getTravelPosition();
		Piece pieceCaptured = n.previousBoard[travel.getRank()][travel.getFile()];
		int pieceValue = (pieceCaptured == null ? 0 : pieceCaptured.getType().getValue());
		int parentPieceValue = n.getMove().getPiece().getType().getValue();
		for (Node childrenN : n.children) {
			if (childrenN.getMove().getTravelPosition().equals(travel) && parentPieceValue > pieceValue) {
				better = false;
			}
		}
		return better;
	}
}
