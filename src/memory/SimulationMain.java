/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.util.Set;
import java.util.Random;

/**
 * Example code.
 * 
 * <p>PS4 instructions: you may use, modify, or remove this class.
 */
public class SimulationMain {
    
    /**
     * Simulate a game.
     * 
     * @param args unused
     */
    public static void main(String[] args) {
        final int size = 3;
        final int players = 2;
        final int tries = 10;
        
        final Board board = Board.generateRandom(size, size, Set.of("A", "B"));
        
        for (int ii = 0; ii < players; ii++) {
            new Thread(() -> {
                final Random random = new Random();
                String playerID = String.valueOf(random.nextInt(100));
                board.addPlayer(playerID);
                System.out.println(board);
                
                for (int jj = 0; jj < tries; jj++) {
                    try {
                        Square card = new Square(random.nextInt(size)+1, random.nextInt(size)+1);
                        System.out.println(playerID + card.toString());
                        board.flipCard(card, playerID);
                        System.out.println(board.getSquareStates());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Square card = new Square(random.nextInt(size)+1, random.nextInt(size)+1);
                        System.out.println(playerID + card.toString());
                        board.flipCard(card, playerID);
                        System.out.println(board.getSquareStates());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}
