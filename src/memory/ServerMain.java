/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Game server runner.
 * 
 * <p>PS4 instructions: you are encouraged *not* to modify this class.
 */
public class ServerMain {
    
    /**
     * Start a game server using the given arguments.
     * 
     * <p> Command-line usage:
     * <pre> java memory.ServerMain PORT FILENAME </pre>
     * or:
     * <pre> java memory.ServerMain PORT ROWS COLUMNS CARD... </pre>
     * where:
     * 
     * <p> PORT is an integer that specifies the server's listening port number,
     *     according to the spec of {@code java.net.ServerSocket(int)}.
     *     0 specifies that a random unused port will be automatically chosen.
     * <p> FILENAME is the path to a valid board file, which will be loaded as
     *     the starting game board.
     * <p> ROWS and COLUMNS are positive integer sizes for a randomly-generated
     *     board, and
     * <p> CARDs are given to specify the cards for the randomly-generated board.
     *     Each card is a Unicode code point in hexadecimal.
     * 
     * <p> For example, to start a web server on a randomly-chosen port using the
     *     board in {@code boards/hearts.txt}:
     * <pre> 0 boards/hearts.txt </pre>
     * 
     * <p> To start a web server on port 8080 with a 3-by-3 board of rainbows
     *     and unicorns:
     * <pre> 8080 3 3 1F308 1F984 </pre>
     * 
     * @param args arguments as described above
     * @throws IOException if an error occurs parsing a file or starting a server
     */
    public static void main(String[] args) throws IOException {
        final Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        
        final int port;
        final Board board;
        
        try {
            port = Integer.parseInt(arguments.remove());
        } catch (NoSuchElementException | NumberFormatException e) {
            throw new IllegalArgumentException("missing or invalid PORT", e);
        }
        
        if (arguments.size() == 1) {
            board = Board.parseFromFile(arguments.remove());
        } else if (arguments.size() > 2) {
            final int rows, columns;
            try {
                rows = Integer.parseInt(arguments.remove());
                columns = Integer.parseInt(arguments.remove());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("invalid size", nfe);
            }
            final Set<String> cards = new HashSet<>(parseCharacterCodes(arguments));
            board = Board.generateRandom(rows, columns, cards);
        } else {
            throw new IllegalArgumentException("expected FILENAME or ROWS COLUMNS CARD...");
        }
        
        new WebServer(board, port).start();
    }
    
    /**
     * @param characterCodes a collection of character codes as hexadecimal strings
     * @return a list of strings containing those characters
     */
    private static List<String> parseCharacterCodes(Collection<String> characterCodes) {
        try {
            return characterCodes.stream()
                    .map(code -> Integer.parseInt(code, Character.SIZE))
                    .peek(code -> { if (code <= ' ') throw new IllegalCharacterException(code); })
                    .map(code -> new String(new int[] { code }, 0, 1))
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("invalid character code", iae);
        }
    }
    
    static class IllegalCharacterException extends IllegalArgumentException {
        private IllegalCharacterException(Integer codePoint) {
            super("illegal character 0x" + Integer.toHexString(codePoint).toUpperCase() + " ("
                    + Optional.ofNullable(Character.getName(codePoint)).orElse("?") + ")");
        }
    }
}
