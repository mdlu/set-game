/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADT representing a Set game board.
 * Mutable and threadsafe.
 */
public class Board {
    
    /**
     * Make a new board by parsing a file.
     * 
     * @param filename path to a game board file
     * @return a new board with the size and cards from the given file
     * @throws IOException if an error occurs reading or parsing the file
     */
//    public static Board parseFromFile(String filename) throws IOException {
//        List<List<String>> squares = new ArrayList<>();
//        BufferedReader in = new BufferedReader(new FileReader(filename));
//        String dimensions = in.readLine();
//        String[] dims = dimensions.split("x");
//        int rows = Integer.parseInt(dims[0]);
//        int cols = Integer.parseInt(dims[1]);
//        for (int r=0; r<rows; r++) {
//            List<String> newRow = new ArrayList<>();
//            for (int c=0; c<cols; c++) {
//                newRow.add(in.readLine());
//            }
//            squares.add(newRow);
//        }
//        in.close();
//        return new Board(squares);
//    }
    
    /**
     * Returns a shuffled list of all cards in the deck.
     * 
     * @param attributes the number of attributes a card should have (default is always 4 for now)
     * @return a shuffled list of all possible combinations of attributes, each combination representing a card
     */
    public static Board generateRandom(int attributes) {
        
        List<Card> cards = new ArrayList<>();
        
        for (Card.Color color: Card.Color.values()) {
            for (Card.Number number: Card.Number.values()) {
                for (Card.Shading shading: Card.Shading.values()) {
                    for (Card.Shape shape: Card.Shape.values()) {
                        cards.add(new Card(color, number, shading, shape));
                    }
                }
            }
        }
        
        Collections.shuffle(cards);
        return new Board(cards);
    }
    
    
    /** A listener for the Board. */
    public interface BoardListener {
        /** Called when the Board changes, as defined in the pset4 specification.
          */
        public void boardChanged(); 
    }
    
    private List<List<Card>> gameBoard;
    private Map<String, Integer> scores;
    private LinkedList<Card> cardsRemaining;
    
    private String activePlayer;
    private List<Square> squaresHeld;
    private List<Square> emptySquares;
    
    private Set<BoardListener> listeners = new HashSet<>();
    
    /* Abstraction function:
     *    AF(gameBoard, scores, heldSquares, isFirst, squareStates, squareQueues, listeners) = 
     *      a game of Memory Scramble with dimensions gameBoard.size() by gameBoard.get(0).size(),
     *      with the text of a card at position (row, col) on the board 
     *      found in gameBoard.get(row-1).get(col-1), the state of that card being held in squareStates.get(new Square(row, col)),
     *      a blocking queue of size 1 holding the current player holding that card being found in squareQueues.get(new Square(row, col)),
     *      and for every player with playerID playing in the game, their score is found in scores.get(playerID),
     *      the squares they currently control is in heldSauares.get(playerID), and whether their next flip will be a "first card"
     *      (as opposed to a "second card") is held in isFirst.get(playerID);
     *      all BoardListeners on the Board are held in listeners
     *    
     * Representation invariant:
     *    all rows in gameBoard are of the same length, and there is at least 1 row and 1 column
     *    every List stored as a value in heldSquares has at most 2 elements
     *    every Square with row number between 1 and the number of rows on the Board, and with column number
     *      between 1 and the number of columns on the Board, must be present as a key in squareStates and squareQeueus
     * 
     * Safety from rep exposure:
     *    all fields are private
     *    all getter methods return immutable values (String, State, int) or explicitly make a defensive copy of 
     *      a mutable value before returning it
     *    constructors make defensive copies of the inputs given; parseFromFile and generateRandom both use the first constructor
     *      taking in a list of rows of card texts, which explicitly makes a copy of each row before inserting it into the Board
     * 
     * Thread safety argument:
     *      all fields are threadsafe datatypes and contain threadsafe datatypes
     *      almost all methods besides constructors are getter methods, which are safe due to the above
     *      for those methods which mutate the rep:
     *          addPlayer: lock allows only one new player to be added at a time, and it only adds new keys to the 
     *              Maps in the rep, which does not interfere with other players
     *          addBoardListener, removeBoardListener: require lock on the listeners Set to add/remove listeners
     *          checkMatch: called only within flipCard when held by a lock for a square controlled 
     *              by a player; the only possible concurrency problems arise when taking from the BlockingQueues 
     *              for the squares they control, but they are guaranteed to be the current and only entry in the BlockingQueue
     *          flipCard: every access to a particular square's current state and BlockingQueue is protected 
     *              requiring the lock for that square's BlockingQueue; the only exception is the line implementing 
     *              rule 1D, which waits when flipping a "first card" until a square's BlockingQueue is empty before 
     *              adding to it, but this line uses a blocking and threadsafe BlockingQueue and cannot interfere with 
     *              the performance of other threads
     */

    /**
     * Constructs an instance of Board, a game of Set with 3 rows and 4 columns.
     * @param cards a list of cards for the Board, 
     */
    public Board(List<Card> cards) {
        List<Card> cardsCopy = new ArrayList<>(cards);
        int counter = 0;
        gameBoard = new ArrayList<>();
        
        final int rows = 3;
        final int cols = 4;
        
        for (int i=0; i<rows; i++) {
            List<Card> newRow = new ArrayList<>();
            for (int j=0; j<cols; j++) {
                newRow.add(cardsCopy.get(counter));
                counter += 1;
            }
            gameBoard.add(Collections.synchronizedList(new ArrayList<>(newRow)));
        }
        
        gameBoard = Collections.synchronizedList(gameBoard);
        scores = new ConcurrentHashMap<>();
        
        // linked list is more efficient for removing the first card
        cardsRemaining = new LinkedList<>(cardsCopy.subList(rows*cols, cardsCopy.size()));
        activePlayer = "";
        squaresHeld = Collections.synchronizedList(new ArrayList<>());
        emptySquares = Collections.synchronizedList(new ArrayList<>());

        checkRep();
    }
    
    /**
     * Constructor to make a new Board that has not been played on, by copying the card layout of another Board.
     * @param that the Board to copy from 
     */
//    public Board(Board that) {
//        gameBoard = Collections.synchronizedList(new ArrayList<>(that.gameBoard));
//        scores = new ConcurrentHashMap<>();
//        squareStates = new ConcurrentHashMap<>();
//        heldSquares = new ConcurrentHashMap<>();
//        squareQueues = new ConcurrentHashMap<>();
//        isFirst = new ConcurrentHashMap<>();
//        
//        int rows = gameBoard.size();
//        int cols = gameBoard.get(0).size();
//        for (int r=1; r<=rows; r++) {
//            for (int c=1; c<=cols; c++) {
//                squareStates.put(new Square(r,c), State.DOWN);
//                squareQueues.put(new Square(r,c), new ArrayBlockingQueue<>(1));
//            }
//        }
//        checkRep();
//    }
    
    /**
     * Assert the representation invariant is true.
     */
    private void checkRep() {
        assert gameBoard.size() > 0;
        assert gameBoard.get(0).size() > 0;
        int rowLength = gameBoard.get(0).size();
        for (int i=0; i<gameBoard.size(); i++) {
            assert gameBoard.get(i).size() == rowLength;
        }
    }

    @Override
    public String toString() {
        return gameBoard.toString();
    }
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Board && this.sameValue((Board) that);
    }
    
    /**
     * Returns true if the two Boards have the same values in all fields except queues and listeners.
     * @param that another Board
     * @return whether the two have the same value
     */
    private boolean sameValue(Board that) {
        // TODO update once all fields finalized
        return this.gameBoard.equals(that.gameBoard) 
                && this.scores.equals(that.scores)
                && this.cardsRemaining.equals(that.cardsRemaining);
    }
    
    @Override
    public int hashCode() { 
        int sum = 0;
        for (int i=0; i<gameBoard.size(); i++) {
            List<Card> row = new ArrayList<>(gameBoard.get(i));
            for (int j=0; j<row.size(); j++) {
                sum += row.get(j).hashCode();
            }
        }
        checkRep();
        return sum;
    }
    
    /**
     * Adds a new player to the current game board.
     * @param playerID a unique ID for a particular player
     * @return true if the player was added successfully, false if the playerID has been already taken
     */
    public boolean addPlayer(String playerID) {
        synchronized (scores) {
            if (scores.containsKey(playerID)) {
                return false;
            }
            scores.put(playerID, 0);
            checkRep();
            return true;
        }
    }
    
    /**
     * Finds if a player is playing the game.
     * @param playerID a unique ID for a player
     * @return whether the player is playing
     */
    public boolean isPlayer(String playerID) {
        return scores.containsKey(playerID);
    }
    
    /**
     * Gets the number of rows in the Board.
     * @return the number of rows
     */
    public synchronized int getNumRows() {
        return gameBoard.size();
    }
    
    /**
     * Gets the number of columns in the Board.
     * @return the number of columns
     */
    public synchronized int getNumCols() {
        return gameBoard.get(0).size();
    }
    
    /**
     * Retrieves a game board row.
     * @param row the row, must lie between 0 (inclusive) and the number of rows in the game board (exclusive)
     * @return the specified row
     */
    public synchronized List<Card> getRow(int row) {
        return new ArrayList<>(gameBoard.get(row));
    }
    
    /**
     * Retrieves a game board column.
     * @param col the column, must lie between 0 (inclusive) and the number of columns in the game board (exclusive)
     * @return the specified column
     */
    public synchronized List<Card> getColumn(int col) {
        List<Card> column = new ArrayList<>();
        for (int i=0; i<gameBoard.size(); i++) {
            column.add(gameBoard.get(i).get(col));
        }
        checkRep();
        return column;
    }
    
    /**
     * Retrieves a given card.
     * @param square the coordinates of the card requested
     * @return the text of the requested card
     */
    public synchronized Card getCard(Square square) {
        return gameBoard.get(square.getRow()).get(square.getCol());
    }
    
    /**
     * Sets a given square to the given Set card.
     * @param square
     * @param card
     */
    public synchronized void setCard(Square square, Card card) {
        gameBoard.get(square.getRow()).set(square.getCol(), card);
    }
    
    /**
     * Gets a copy of the current board.
     * @return returns the current board, with only the text of the cards; does not return any player data or card states
     */
//    public Board getBoard() {
//        return new Board(gameBoard);
//    }
    
//    /**
//     * Returns the state of a given square.
//     * @param square the square's state to be queried
//     * @return the square's current state
//     */
//    public State getSquareState(Square square) {
//        return squareStates.get(square);
//    }

//    /**
//     * Returns the current square states.
//     * @return a copy of the current states of the squares in the board
//     */
//    public Map<Square, State> getSquareStates() {
//        return new HashMap<Square, State>(squareStates);
//    }

    /**
     * Returns the squares currently being held by a player.
     * @return a copy of the list of squares held
     */
    public synchronized List<Square> getSquaresHeld() {
        return new ArrayList<>(squaresHeld);
    }
    
    /**
     * Returns the empty squares on the board.
     * @return a copy of the list of empty squares
     */
    public synchronized List<Square> getEmptySquares() {
        return new ArrayList<>(emptySquares);
    }
    
    /**
     * Adds a listener to the Board.
     * @param listener called when the Board changes
     */
    public void addBoardListener(BoardListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a listener from the Board.
     * @param listener which will no longer be called when the Board changes
     */
    public void removeBoardListener(BoardListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    private void callListeners() {
        for (BoardListener listener: Set.copyOf(listeners)) {
            listener.boardChanged();
        }
    }
    
    /**
     * Returns the scores of the Board at present.
     * @return a Map mapping player IDs to their scores
     */
    public synchronized Map<String, Integer> getScores() {
        return new HashMap<>(scores);
    }
    
    /**
     * Determines whether the three cards held make a Set, as defined by the game rules.
     * @return whether the three given cards is a set
     */
    public synchronized boolean checkSet() {
        Card card1 = getCard(squaresHeld.get(0));
        Card card2 = getCard(squaresHeld.get(1));
        Card card3 = getCard(squaresHeld.get(2));
        int colors = new HashSet<Card.Color>(Arrays.asList(card1.color(), card2.color(), card3.color())).size();
        int numbers = new HashSet<Card.Number>(Arrays.asList(card1.number(), card2.number(), card3.number())).size();
        int shadings = new HashSet<Card.Shading>(Arrays.asList(card1.shading(), card2.shading(), card3.shading())).size();
        int shapes = new HashSet<Card.Shape>(Arrays.asList(card1.shape(), card2.shape(), card3.shape())).size();
        return (colors*numbers*shadings*shapes)%2 != 0; // the sets should all be either of size 1 or size 3
    }
    
    /** 
     * Allows a player to declare they've foud a set, giving them rights to start picking 3 cards.
     * @param playerID the unique ID of the player
     */
    public synchronized void declareSet(String playerID) {
        if (!activePlayer.equals("")) { // another player is currently selecting cards
            return;
        } else {
            activePlayer = playerID;
        }
        // TODO update for controlling time limits?
    }
    
    /**
     * Executes the replacement of three cards once they're found.
     */
    public synchronized void replaceCards() {
        final int numCards = 3;
        if (cardsRemaining.size() == 0) {
            emptySquares.addAll(squaresHeld);
        } else {
            for (int i=0; i<numCards; i++) {
                Square sq = squaresHeld.get(i);
                Card newCard = cardsRemaining.removeFirst();
                setCard(sq, newCard);
            }
        }
    }
    
    /**
     * Adds three cards to the board; called if no one can find a Set on the given board.
     */
    public synchronized void addCards() {
        final int rows = 3;
        if (cardsRemaining.size() == 0) { // can't add more cards if none left
            return; 
        }
        for (int row=0; row<rows; row++) {
            Card newCard = cardsRemaining.removeFirst();
            gameBoard.get(row).add(newCard);
        }
    }
    
    /**
     * Performs the selection of a card by a particular player, according to the rules of Set.
     * @param square the coordinates of the card the player wants to flip
     * @param playerID the unique of the player
     * @throws InterruptedException
     */
    public synchronized void pickCard(Square square, String playerID) throws InterruptedException {
        
        if (!playerID.equals(activePlayer)) { // cannot pick card if not currently the player picking cards
            return; 
        }
        
        if (squaresHeld.contains(square)) { // nothing happens if card already selected is picked again
            return;
        }
        
        squaresHeld.add(square);
        callListeners();
        
        // TODO can probably move these constants elsewhere later
        final int setSize = 3;
        final int pointsWon = 10; // gain 10 points for a correct set
        final int pointsLost = 5; // lose 5 points for an incorrect set
        
        if (squaresHeld.size() == setSize) {
            int score = scores.get(playerID);
            if (checkSet()) {
                scores.put(playerID, score + pointsWon);
                replaceCards();
                
                callListeners();
            } else {
                scores.put(playerID, score - pointsLost);
            }
            // reset the board so the next player can find a Set
            squaresHeld.clear();
            activePlayer = "";
        }
        
        checkRep();
    }
}
