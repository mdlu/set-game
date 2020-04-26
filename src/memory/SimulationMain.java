/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        final int size = 1;
        final int players = 3;
        final int tries = 3000;
        final int idGenerator = 1000;
        
        final Board board = Board.generateRandom(size, size, Set.of("A", "B"));
        final Map<String, Integer> tryCounts = new ConcurrentHashMap<>();
        
        for (int ii = 0; ii < players; ii++) {
            new Thread(() -> {
                final Random random = new Random();
                String playerID = String.valueOf(random.nextInt(idGenerator));
                board.addPlayer(playerID);
//                System.out.println(board);
                
                tryCounts.put(playerID, 0);
                for (int jj = 0; jj < tries; jj++) {
                    try {
                        Square card = new Square(random.nextInt(size)+1, random.nextInt(size)+1);
//                        System.out.println(playerID + " 1 " + card.toString());
                        board.flipCard(card, playerID);
//                        System.out.println(playerID + " 1 " + card.toString() + " " + board.getSquareStates());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        Square card = new Square(random.nextInt(size)+1, random.nextInt(size)+1);
//                        System.out.println(playerID + " 2 " + card.toString());
                        board.flipCard(card, playerID);
//                        System.out.println(playerID + " 2 " + card.toString() + " " + board.getSquareStates());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    tryCounts.put(playerID, tryCounts.get(playerID)+1);
                }
                
            }).start();
        }
        
        System.out.println("the end");
    }
}
