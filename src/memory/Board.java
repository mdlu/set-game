/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADT representing a Memory Scramble game board.
 * Mutable and threadsafe.
 * 
 * <p>PS4 instructions: the specifications of static methods
 * {@link #parseFromFile(String)} and {@link #generateRandom(int, int, Set)} are
 * required.
 */
public class Board {
    
    /**
     * Make a new board by parsing a file.
     * 
     * @param filename path to a game board file
     * @return a new board with the size and cards from the given file
     * @throws IOException if an error occurs reading or parsing the file
     */
    public static Board parseFromFile(String filename) throws IOException {
        List<List<String>> squares = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String dimensions = in.readLine();
        String[] dims = dimensions.split("x");
        int rows = Integer.parseInt(dims[0]);
        int cols = Integer.parseInt(dims[1]);
        for (int r=0; r<rows; r++) {
            List<String> newRow = new ArrayList<>();
            for (int c=0; c<cols; c++) {
                newRow.add(in.readLine());
            }
            squares.add(newRow);
        }
        return new Board(squares);
    }
    
    /**
     * Make a new random board.
     * 
     * @param rows board height
     * @param columns board width
     * @param cards cards that appear on the board
     * @return a new rows-by-columns-size board filled with a random permutation
     *         of the given cards repeated in as equal numbers as possible
     */
    public static Board generateRandom(int rows, int columns, Set<String> cards) {
        List<String> squaresList = new ArrayList<>();
        List<String> cardsList = new ArrayList<>(cards);
        int index = 0;
        int length = cards.size();
        
        for (int s=0; s<rows*columns; s++) { // add cards in as equal numbers as possible
            squaresList.add(cardsList.get(index));
            index = (index+1) % length;
        }
        Collections.shuffle(squaresList);
        
        // assemble the cards into an array of dimensions rows*columns
        List<List<String>> squares = new ArrayList<>(); 
        index = 0;
        for (int r=0; r<rows; r++) {
            List<String> newRow = new ArrayList<>();
            for (int c=0; c<columns; c++) {
                newRow.add(squaresList.get(index));
                index += 1;
            }
            squares.add(newRow);
        }
        return new Board(squares);
    }
    
    
    private List<List<String>> gameBoard;
    private Map<String, Integer> scores;
    private Map<String, List<Square>> heldSquares;
    private Map<String, Boolean> isFirst;
    private Map<Square, State> squareStates;
    private Map<Square, BlockingQueue<String>> squareQueues;
    
    public enum State {UP, DOWN, GONE};
    
    /* Abstraction function:
     *    AF(gameBoard, scores, heldSquares, isFirst, squareStates, squareQueues) = 
     *      a game of Memory Scramble with the text of a card at position (row, col) on the board 
     *      found in gameBoard.get(row-1).get(col-1), the state of that card being held in squareStates.get(new Square(row, col)),
     *      a blocking queue of size 1 holding the current player holding that card being found in squareQueues.get(new Square(row, col)),
     *      and for every player with playerID playing in the game, their score is found in scores.get(playerID),
     *      the squares they currently control is in heldSauares.get(playerID), and whether their next flip will be a "first card"
     *      (as opposed to a "second card") is held in isFirst.get(playerID)
     *    
     * Representation invariant:
     *    all rows in gameBoard are of the same length
     *    scores, heldSquares, and isFirst all share the same set of keys, i.e., the set of playerIDs
     *    every List stored as a value in heldSquares has at most 2 elements
     *    every Square with row number between 1 and the number of rows on the Board, and with column number
     *      between 1 and the number of columns on the Board, must be present as a key in squareStates and squareQeueus
     * 
     * Safety from rep exposure:
     *    all fields are private
     *    all getter methods return immutable values or defensive copies
     *    constructors make defensive copies of the inputs given
     * 
     * Thread safety argument:
     *      TODO write this!!
     */

    /**
     * Constructs an instance of Board.
     * @param squares a list of lists containing the text of the cards on the Board, 
     * where the nth element in squares contains the text of all squares in order for the nth row
     */
    public Board(List<List<String>> squares) {
        gameBoard = new ArrayList<>();
        for (List<String> row: squares) {
            List<String> newRow = Collections.synchronizedList(new ArrayList<>(row));
            gameBoard.add(newRow);
        }
        gameBoard = Collections.synchronizedList(gameBoard);
        scores = new ConcurrentHashMap<>();
        squareStates = new ConcurrentHashMap<>();
        heldSquares = new ConcurrentHashMap<>();
        squareQueues = new ConcurrentHashMap<>();
        isFirst = new ConcurrentHashMap<>();
        
        int rows = squares.size();
        int cols = squares.get(0).size();
        for (int r=1; r<=rows; r++) {
            for (int c=1; c<=cols; c++) {
                squareStates.put(new Square(r,c), State.DOWN);
                squareQueues.put(new Square(r,c), new ArrayBlockingQueue<>(1));
            }
        }
    }
    
    /**
     * Constructor to make a new Board that has not been played on, by copying the card layout of another Board.
     * @param that the Board to copy from 
     */
    public Board(Board that) {
        gameBoard = Collections.synchronizedList(new ArrayList<>(that.gameBoard));
        scores = new ConcurrentHashMap<>();
        squareStates = new ConcurrentHashMap<>();
        heldSquares = new ConcurrentHashMap<>();
        squareQueues = new ConcurrentHashMap<>();
        isFirst = new ConcurrentHashMap<>();
        
        int rows = gameBoard.size();
        int cols = gameBoard.get(0).size();
        for (int r=1; r<=rows; r++) {
            for (int c=1; c<=cols; c++) {
                squareStates.put(new Square(r,c), State.DOWN);
                squareQueues.put(new Square(r,c), new ArrayBlockingQueue<>(1));
            }
        }
    }
    
    private void checkRep() {
        
    }

    @Override
    public String toString() {
        return gameBoard.toString();
    }
    
    @Override
    public boolean equals(Object that) {
        return that instanceof Board && this.sameValue((Board) that);
    }
    
    private boolean sameValue(Board that) {
        return this.gameBoard.equals(that.gameBoard);
//                && this.scores.equals(that.scores)
//                && this.heldSquares.equals(that.heldSquares) && this.isFirst.equals(that.isFirst)
//                && this.squareStates.equals(that.squareStates) && this.squareQueues.equals(that.squareQueues);
    }
    
    @Override
    public int hashCode() { 
        int sum = 0;
        for (int i=0; i<gameBoard.size(); i++) {
            List<String> row = new ArrayList<>(gameBoard.get(i));
            for (int j=0; j<row.size(); j++) {
                sum += row.get(j).hashCode();
            }
        }
        return sum;
    }
    
    /**
     * Adds a new player to the current game board.
     * @param playerID a unique ID for a particular player
     * @return true if the player was added successfully, false if the playerID has been already taken
     */
    public boolean addPlayer(String playerID) {
        if (scores.containsKey(playerID)) {
            return false;
        }
        scores.put(playerID, 0);
        heldSquares.put(playerID, new ArrayList<>());
        isFirst.put(playerID, true);
        return true;
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
    public int getNumRows() {
        return gameBoard.size();
    }
    
    /**
     * Gets the number of columns in the Board.
     * @return the number of columns
     */
    public int getNumCols() {
        return gameBoard.get(0).size();
    }
    
    /**
     * Retrieves a game board row.
     * @param row the row, must lie between 1 and the number of rows in the game board, inclusive
     * @return the specified row
     */
    public List<String> getRow(int row) {
        return new ArrayList<>(gameBoard.get(row-1));
    }
    
    /**
     * Retrieves a game board column.
     * @param col the column, must lie between 1 and the number of columns in the game board, inclusive
     * @return the specified column
     */
    public List<String> getColumn(int col) {
        List<String> column = new ArrayList<>();
        for (int i=0; i<gameBoard.size(); i++) {
            column.add(gameBoard.get(i).get(col-1));
        }
        return column;
    }
    
    /**
     * Retrieves the text on a given card.
     * @param card the coordinates of the card requested
     * @return the text of the requested card
     */
    public String getSquare(Square card) {
        return gameBoard.get(card.getRow()-1).get(card.getCol()-1);
    }
    
    /**
     * Gets a copy of the current board.
     * @return returns the current board, with only the text of the cards; does not return any player data or card states
     */
    public Board getBoard() {
        return new Board(gameBoard);
    }
    
    /**
     * Returns the state of a given square.
     * @param square the square's state to be queried
     * @return the square's current state
     */
    public State getSquareState(Square square) {
        return squareStates.get(square);
    }
    
    /**
     * Returns the current square states.
     * @return a copy of a copy of the current states of the squares in the board
     */
    public Map<Square, State> getSquareStates(){
        return new HashMap<Square, State>(squareStates);
    }
    
    /**
     * Returns the squares held by a palyer.
     * @param playerID the unique ID of a player
     * @return a list of squares held by that player
     */
    public List<Square> getSquaresHeld(String playerID) {
        return new ArrayList<>(heldSquares.get(playerID));
    }
    
    /**
     * Returns the scores of the Board at present.
     * @return a Map mapping player IDs to their scores
     */
    public Map<String, Integer> getScores() {
        return new HashMap<>(scores);
    }
    
    /**
     * Returns whether the next flip by the player is a "first card".
     * @param playerID the unique ID of the player
     * @return whether the next flip will be a "first card"
     */
    public boolean isPlayerFirst(String playerID) {
        return isFirst.get(playerID);
    }
    
    /**
     * Returns the current controller of a given square.
     * @param square the requested square to query
     * @return the player currently controlling that square
     */
    public String getController(Square square) {
        return squareQueues.get(square).peek();
    }
    
    /**
     * Performs the flipping of a card by a particular player, according to the rules given in the pset 4 instructions.
     * @param card the coordinates of the card the player wants to flip
     * @param playerID the unique of the player
     * @throws InterruptedException
     */
    public void flipCard(Square card, String playerID) throws InterruptedException {
        
        if (!isFirst.get(playerID)) {
            Square heldSquare = heldSquares.get(playerID).get(0);
            
            if (squareStates.get(card).equals(State.GONE)) { // rule 2A
                squareQueues.get(heldSquare).take(); // relinquish control
            } else if (squareStates.get(card).equals(State.DOWN)) { // rule 2C
                squareQueues.get(card).put(playerID);
                
                squareStates.put(card, State.UP);
                heldSquares.get(playerID).add(card);
                checkMatch(playerID);
            } else {
                // potential concurrency issue!
                if (squareQueues.get(card).size() != 0) { // rule 2B
                    squareQueues.get(heldSquare).take();
                } else {
                    squareQueues.get(card).put(playerID);
                    heldSquares.get(playerID).add(card);
                    checkMatch(playerID);
                }
            }
            isFirst.put(playerID, true);
            
        } else { // flipping a first card, possibly after trying to turn a second card
            if (heldSquares.get(playerID).size() == 2) {
                Square square1 = heldSquares.get(playerID).get(0);
                Square square2 = heldSquares.get(playerID).get(1);
                String card1 = getSquare(square1);
                String card2 = getSquare(square2);
                if (card1.equals(card2)) { // rule 3A
                    squareStates.put(square1, State.GONE); // remove cards
                    squareStates.put(square2, State.GONE);
                    squareQueues.get(square1).take(); // relinquish control
                    squareQueues.get(square2).take();
                } 
            } 
            // rule 3B
            // potential concurrency issue
            for (Square square: heldSquares.get(playerID)) {
                if (squareStates.get(square).equals(State.UP) && squareQueues.get(square).size() == 0) {
                    squareStates.put(square, State.DOWN);
                }
            }
            heldSquares.get(playerID).clear();
            
            squareQueues.get(card).put(playerID); // rule 1D, blocks if held
            
            if (squareStates.get(card).equals(State.GONE)) { // rule 1A
                squareQueues.get(card).take();
                return;
            } else if (squareStates.get(card).equals(State.DOWN)) { // rule 1B
                squareStates.put(card, State.UP);
                heldSquares.get(playerID).add(card);
            } else { // rule 1C
                heldSquares.get(playerID).add(card);
            }
            
            isFirst.put(playerID, false);
        }
    }
    
    /**
     * Handles if the cards held by a player have matching text symbols on the board.
     * Adds 1 to the player's score if the player holds two cards that match, and otherwise relinquishes control of them.
     * @param playerID unique ID of the player
     * @throws InterruptedException
     */
    private void checkMatch(String playerID) throws InterruptedException {
        if (heldSquares.get(playerID).size() != 2) {
            return;
        }
        Square square1 = heldSquares.get(playerID).get(0);
        Square square2 = heldSquares.get(playerID).get(1);
        String card1 = getSquare(square1);
        String card2 = getSquare(square2);
        
        if (card1.equals(card2)) { // rule 2D
            int score = scores.get(playerID);
            scores.put(playerID, score + 1);
        } else { // rule 2E, relinquish control of squares
            squareQueues.get(square1).take();
            squareQueues.get(square2).take();
        }
        
    }
}
