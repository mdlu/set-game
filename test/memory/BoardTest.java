/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Tests for the Board datatype.
 */
public class BoardTest {
    
    /* Testing strategy
     *    parseFromFile:
     *      partition on filename: perfect.txt, zoom.txt, invalid file
     *    generateRandom:
     *      partition on board size: square, non-square
     *    addPlayer:
     *      partition on playerID: new player ID, existing player ID
     *    checkMatch:
     *      partition on the player's cards: has <2 cards, has 2 non-matching cards, has 2 matching cards
     *    flipCard:
     *      partition on the rules given in the specification for gameplay:
     *          1A, 1B, 1C, 1D, 2A, 2B, 2C, 2D, 2E, 3A, 3B
     */
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    // test parseFromFile, covers perfect.txt
    @Test
    public void testPerfectBoard() throws IOException {
        Board board = Board.parseFromFile("boards/perfect.txt");
        List<List<String>> expected = List.of(List.of("🦄","🦄","🌈"),List.of("🌈","🌈","🦄"),List.of("🌈","🦄","🌈"));
        assertEquals(new Board(expected), board, "expected properly parsed board from perfect.txt");
    }
    
    // test parseFromFile, covers zoom.txt
    @Test
    public void testZoomBoard() throws IOException {
        Board board = Board.parseFromFile("boards/zoom.txt");
        List<List<String>> expected = List.of(List.of("🚚","🏎","🚜","🚂","🚲"), List.of("🚜","🚂","🚲","🚚","🏎"), List.of("🚲","🚚","🏎","🚜","🚂"),
                List.of("🏎","🚜","🚂","🚲","🚚"), List.of("🚂","🚲","🚚","🏎","🚜"));
        assertEquals(new Board(expected), board, "expected properly parsed board from zoom.txt");
    }
    
    // test parseFromFile, covers invalid file
    @Test
    public void testInvalid() throws IOException {
        assertThrows(IOException.class, () -> Board.parseFromFile("abc.txt"), "should have been invalid filepath");
    }
    
    // test generateRandom, covers square board
    @Test
    public void testRandomSquareBoard() {
        Board board = Board.generateRandom(3, 3, Set.of("a", "b"));
        assertEquals(3, board.getNumRows(), "expected 3 rows");
        assertEquals(3, board.getNumCols(), "expected 3 columns");
        int aCount = 0;
        int bCount = 0;
        for (int i=1; i<=3; i++) {
            for (int j=1; j<=3; j++) {
                if (board.getSquare(new Square(i,j)).equals("a")) {
                    aCount += 1;
                } else { 
                    bCount += 1;
                }
            }
        }
        assertEquals(9, aCount+bCount, "expected 9 values");
        assertEquals(1, Math.abs(aCount-bCount), "number of As and Bs should only differ by 1");
    }
    
    // test generateRandom, covers non-square board
    @Test
    public void testRandomNonSquareBoard() {
        Board board = Board.generateRandom(2, 4, Set.of("a", "b", "c"));
        assertEquals(2, board.getNumRows(), "expected 2 rows");
        assertEquals(4, board.getNumCols(), "expected 4 columns");
        int aCount = 0;
        int bCount = 0;
        int cCount = 0;
        for (int i=1; i<=2; i++) {
            for (int j=1; j<=4; j++) {
                if (board.getSquare(new Square(i,j)).equals("a")) {
                    aCount += 1;
                } else if (board.getSquare(new Square(i,j)).equals("b")) { 
                    bCount += 1;
                } else {
                    cCount += 1;
                }
            }
        }
        assertEquals(8, aCount+bCount+cCount, "expected 8 values");
        assertTrue(Math.abs(aCount-bCount) <= 1, "number of As and Bs should only differ by at most 1");
        assertTrue(Math.abs(bCount-cCount) <= 1, "number of Bs and Cs should only differ by at most 1");
        assertTrue(Math.abs(aCount-cCount) <= 1, "number of As and Cs should only differ by at most 1");
    }
    
    // test addPlayer, both new and old player
    @Test
    public void testAddPlayer() {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        assertTrue(board.addPlayer("tom"), "new player should have been added");
        assertFalse(board.addPlayer("tom"), "player already exists");
    }
    
    
    // below are all test cases for checkMatch and flipCard
    
    // covers rule 1A, checkMatch with <2 cards held
    @Test
    public void test1A() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 3), "tom");
        board.flipCard(new Square(1, 2), "tom");
        
        board.flipCard(new Square(1, 1), "jerry");
        assertEquals(0, board.getSquaresHeld("jerry").size(), "retrieving an empty square should fail");
        assertTrue(board.isPlayerFirst("jerry"), "since this failed, next flip should still be a first card");
        assertEquals(0, board.getScores().get("jerry"), "score should be zero");
        assertEquals(Board.State.GONE, board.getSquareState(new Square(1, 1)), "square should have been removed");
    }
    
    // covers rule 1B
    @Test
    public void test1B() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(2, 2), "tom");
        assertEquals(List.of(new Square(2, 2)), board.getSquaresHeld("tom"), "should be controlling the flipped card");
        assertFalse(board.isPlayerFirst("tom"), "next flip is now a second card");
        assertEquals(0, board.getScores().get("tom"), "score should be zero");
        assertEquals(Board.State.UP, board.getSquareState(new Square(2, 2)), "square should be up");
    }
    
    // covers rule 1C
    @Test
    public void test1C() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(2, 2), "jerry");
        board.flipCard(new Square(2, 3), "jerry");
        board.flipCard(new Square(2, 2), "tom");
        
        assertEquals(List.of(new Square(2, 2)), board.getSquaresHeld("tom"), "should be controlling the flipped card");
        assertFalse(board.isPlayerFirst("tom"), "next flip is now a second card");
        assertEquals(0, board.getScores().get("tom"), "score should be zero");
        assertEquals(Board.State.UP, board.getSquareState(new Square(2, 2)), "square should be up");
    }
    
    // covers rule 1D
    @Test
    public void test1D() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(2, 2), "jerry");
        
        final List<Integer> test = Collections.synchronizedList(new ArrayList<>());
        new Thread(() -> {
            try {
                board.flipCard(new Square(2, 2), "tom");
                test.add(5); // if tom is blocked, then this will always execute after jerry adds 0 to the list
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        
        test.add(0);
        board.flipCard(new Square(2, 3), "jerry");
        
        while (test.size() != 2) {
            continue; // wait for second thread to complete
        }
        assertEquals(5, test.get(1), "list was added to out of order, so blocking did not occur");
        assertEquals(List.of(new Square(2, 2)), board.getSquaresHeld("tom"), "should be controlling the flipped card");
        assertFalse(board.isPlayerFirst("tom"), "next flip is now a second card");
        assertEquals(0, board.getScores().get("tom"), "score should be zero");
        assertEquals(Board.State.UP, board.getSquareState(new Square(2, 2)), "square should be up");
    }
    
    // covers rule 2A
    @Test
    public void test2A() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 3), "tom");
        board.flipCard(new Square(1, 2), "tom");
        
        board.flipCard(new Square(2, 1), "jerry");
        board.flipCard(new Square(1, 1), "jerry");
        assertEquals(null, board.getController(new Square(2, 1)), "retrieving an empty square means all controlled cards are relinquished");
        assertTrue(board.isPlayerFirst("jerry"), "since this failed, next is now a first card");
        assertEquals(0, board.getScores().get("jerry"), "score should be zero");
        assertEquals(Board.State.GONE, board.getSquareState(new Square(1, 1)), "square should have been removed");
        assertEquals(Board.State.UP, board.getSquareState(new Square(2, 1)), "first flipped card should still remain face up");
    }
    
    // covers rule 2B
    @Test
    public void test2B() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");

        board.flipCard(new Square(2, 1), "jerry");
        board.flipCard(new Square(1, 1), "jerry");
        assertEquals(null, board.getController(new Square(2, 1)), "retrieving an empty square means all controlled cards are relinquished");
        assertTrue(board.isPlayerFirst("jerry"), "since this failed, next is now a first card");
        assertEquals(0, board.getScores().get("jerry"), "score should be zero");
        assertEquals(Board.State.UP, board.getSquareState(new Square(2, 1)), "first flipped card should still remain face up");
    }
    
    // covers rule 2C
    @Test
    public void test2C() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 3), "tom");
        assertEquals(Board.State.UP, board.getSquareState(new Square(1, 3)), "second card should now face up");
    }
    
    // covers rule 2D, checkMatch with 2 matching cards
    @Test
    public void test2D() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 3), "tom");
        assertEquals(1, board.getScores().get("tom"), "score should be 1 now, from successful match");
        assertEquals("tom", board.getController(new Square(1, 1)), "first held square should still be controlled");
        assertEquals("tom", board.getController(new Square(1, 3)), "second held square should still be controlled");
        assertEquals(Board.State.UP, board.getSquareState(new Square(1, 1)), "the first card stays up");
        assertEquals(Board.State.UP, board.getSquareState(new Square(1, 3)), "the second card stays up");
    }
    
    // covers rule 2E, checkMatch with 2 non-matching cards
    @Test
    public void test2E() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 2), "tom");
        assertEquals(0, board.getScores().get("tom"), "match unsuccessful, score is still 0");
        assertEquals(null, board.getController(new Square(1, 1)), "first held square should still be controlled");
        assertEquals(null, board.getController(new Square(1, 3)), "second held square should still be controlled");
        assertEquals(Board.State.UP, board.getSquareState(new Square(1, 1)), "did not match, but the first card stays up");
        assertEquals(Board.State.UP, board.getSquareState(new Square(1, 2)), "did not match, but the second card stays up");
    }
    
    // covers rule 3A
    @Test
    public void test3A() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 3), "tom");
        
        board.flipCard(new Square(1, 2), "tom");
        
        assertEquals(null, board.getController(new Square(1, 1)), "first held square is no longer controlled");
        assertEquals(null, board.getController(new Square(1, 3)), "second held square is no longercontrolled");
        assertEquals(Board.State.GONE, board.getSquareState(new Square(1, 1)), "the first card is removed");
        assertEquals(Board.State.GONE, board.getSquareState(new Square(1, 3)), "the second card is removed");
    }
    
    // covers rule 3B
    @Test
    public void test3B() throws InterruptedException {
        List<List<String>> boardList = List.of(List.of("a", "b", "a"), List.of("b", "a", "b"), List.of("a", "b", "a"));
        Board board = new Board(boardList);
        board.addPlayer("tom");
        board.addPlayer("jerry");
        
        board.flipCard(new Square(1, 1), "tom");
        board.flipCard(new Square(1, 1), "tom");
        
        board.flipCard(new Square(1, 2), "tom");
        assertEquals(Board.State.DOWN, board.getSquareState(new Square(1, 1)), "one non-matching card is now flipped down");
        
        board.flipCard(new Square(1, 3), "tom");
        board.flipCard(new Square(1, 2), "jerry");
        
        board.flipCard(new Square(2, 2), "tom");
        
        assertEquals("jerry", board.getController(new Square(1, 2)), "card should now be controlled by second player");
        assertEquals(Board.State.UP, board.getSquareState(new Square(1, 2)), "card is now controlled by another player, so it remains up");
        assertEquals(Board.State.DOWN, board.getSquareState(new Square(1, 3)), "uncontrolled card should now be flipped down");
    }
    
    
}
