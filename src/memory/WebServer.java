/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import memory.web.ExceptionsFilter;
import memory.web.HeadersFilter;
import memory.web.LogFilter;

/**
 * HTTP web game server.
 * 
 * <p>PS4 instructions: the specifications of {@link #WebServer(Board, int)},
 * {@link #port()}, {@link #start()}, and {@link #stop()} are required.
 */
public class WebServer {
    
    private final HttpServer server;
    private final Board board;
    
    /* Abstraction function:
     *    AF(server, board): a game of Memory Scramble serviced by server and with a current
     *        game state represented by board
     *        
     * Representation invariant:
     *    true
     *    
     * Safety from rep exposure:
     *    all fields are private and final
     *    defensive copy is made in constructor
     *    all methods return immutable values or void
     * 
     * Thread safety argument:
     *    TODO here
     */
    
    /**
     * Make a new web game server using board that listens for connections on port.
     * 
     * @param board shared game board
     * @param port server port number
     * @throws IOException if an error occurs starting the server
     */
    public WebServer(Board board, int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.board = new Board(board);
        
        // handle concurrent requests with multiple threads
        server.setExecutor(Executors.newCachedThreadPool());
        
        HeadersFilter headers = new HeadersFilter(Map.of(
                // allow requests from web pages hosted anywhere
                "Access-Control-Allow-Origin", "*",
                // all responses will be plain-text UTF-8
                "Content-Type", "text/plain; charset=utf-8"
                ));
        List<Filter> filters = List.of(new ExceptionsFilter(), new LogFilter(), headers);
        
        // handle requests for /look/player
        HttpContext look = server.createContext("/look/", this::handleLook);
        look.getFilters().addAll(filters);
        
        // handle requests for /flip/player/row,column
        HttpContext flip = server.createContext("/flip/", this::handleFlip);
        flip.getFilters().addAll(filters);
        
        // handle requests for /scores
        HttpContext scores = server.createContext("/scores", this::handleScores);
        scores.getFilters().addAll(filters);

//        // handle requests for /watch/player
//        HttpContext watch = server.createContext("/watch/", this::handleWatch);
//        watch.getFilters().addAll(filters);
        
        checkRep();
    }
    
    private void checkRep() {
        assert server != null;
        assert board != null;
    }
    
    /**
     * @return the port on which this server is listening for connections
     */
    public int port() {
        return server.getAddress().getPort();
    }
    
    /**
     * Start this server in a new background thread.
     */
    public void start() {
        System.err.println("Server will listen on " + server.getAddress());
        server.start();
    }
    
    /**
     * Stop this server. Once stopped, this server cannot be restarted.
     */
    public void stop() {
        System.err.println("Server will stop");
        server.stop(0);
    }
    
    /**
     * Converts a board into the proper String representation to send as an HTTP response, 
     * as specified in the pset 4 handout..
     * @param board the Board to convert 
     * @param playerID the unique ID of the player requesting the response
     * @return the String representation
     */
    private String boardResponse(String playerID) {
//        Board boardCopy = new Board(board); 
        Board boardCopy = board; // TODO make copy for consistency issues
        List<Square> squaresHeld = boardCopy.getSquaresHeld(playerID);
        String response = boardCopy.getNumRows()+"x"+boardCopy.getNumCols()+"\n";
        for (int row=1; row<=boardCopy.getNumRows(); row++) {
            for (int col=1; col<=boardCopy.getNumCols(); col++) {
                Square sq = new Square(row, col);
                switch (boardCopy.getSquareState(sq)) {
                case GONE:
                    response += "none\n";
                    break;
                case UP:
                    if (squaresHeld.contains(sq)) {
                        response += "my " + boardCopy.getSquare(sq) + "\n";
                    } else {
                        response += "up " + boardCopy.getSquare(sq) + "\n";
                    }
                    break;
                case DOWN:
                    response += "down\n";
                    break;
                default:
                    throw new AssertionError("should never get here");
                }
            }
        }
        return response;
    }
    
    /**
     * Handles the /look/player route. Sends a response showing the board, formatted as described 
     * in the grammar in the pset4 instructions, or reports "Your player name ID contains non-alphanumeric characters, 
     * or you have not participated yet in this game." if the playerID is not alphanumeric, or the player has not flipped
     * a card in this game yet.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handleLook(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String player = path.substring(base.length());
        
        final String response;
        if (player.matches("\\w+") && board.isPlayer(player)) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(200, 0);
            response = boardResponse(player);
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(404, 0);
            response = "Your player name ID contains non-alphanumeric characters, or you have not participated yet in this game.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.println(response);
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /flip/player/row,column route. Attempts to flip the card at (row, column) for the given player.
     * Sends a response showing the board, formatted as described in the grammar in the pset4 instructions, or reports 
     * "Your requested flip was not valid." if the request was not formatted correctly or specified an out-of-bounds
     * square, or reports "Your requested flip was interrupted." in case of an InterruptedException.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handleFlip(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String parameters = path.substring(base.length());
        
        String response;
        if (parameters.matches("\\w+/[0-9]+,[0-9]+")) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            
            final String[] splitParameters = parameters.split("/|,");
            final String playerID = splitParameters[0];
            final Square card = new Square(Integer.valueOf(splitParameters[1]),
                    Integer.valueOf(splitParameters[2]));
            
            if (!board.isPlayer(playerID)) {
                board.addPlayer(playerID);
            }
            
            try {
                board.flipCard(card, playerID);
                exchange.sendResponseHeaders(200, 0);
                response = boardResponse(playerID);
            } catch (InterruptedException e) {
                exchange.sendResponseHeaders(404, 0);
                response = "Your requested flip was interrupted.";
            } catch (NullPointerException npe) {
                exchange.sendResponseHeaders(404, 0);
                response = "Your requested flip was not valid.";
            }
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(404, 0);
            response = "Your requested flip was not valid.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.println(response);
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /scores route. Returns a list of the current scores of each player in the game,
     * formatted as specified in the pset4 instructions, or reports that "There should be no additional 
     * characters following /scores in the request." if the GET request contains any extraneous information
     * beyond the /scores path.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handleScores(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String params = path.substring(base.length());
        
        final String response;
        if (params.length() == 0) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(200, 0);
            Map<String, Integer> scores = board.getScores();
            String scoreString = "";
            for (String player: scores.keySet()) {
                scoreString += player + " " + scores.get(player) + "\n";
            }
            response = scoreString;
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(404, 0);
            response = "There should be no additional characters following /scores in the request.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.println(response);
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
//    private void handleWatch(HttpExchange exchange) throws IOException {
//        // if you want to know the requested path:
//        final String path = exchange.getRequestURI().getPath();
//        
//        // it will always start with the base path from server.createContext():
//        final String base = exchange.getHttpContext().getPath();
//        assert path.startsWith(base);
//        
//        final String player = path.substring(base.length());
//        
//        final String response;
//        if (player.matches("\\w+")) {
//            // if the request is valid, respond with HTTP code 200 to indicate success
//            // - response length 0 means a response will be written
//            // - you must call this method before calling getResponseBody()
//            exchange.sendResponseHeaders(200, 0);
//            response = "this need to be implemented"; 
//        } else {
//            // otherwise, respond with HTTP code 404 to indicate an error
//            exchange.sendResponseHeaders(404, 0);
//            response = "Your player name ID may only consist of alphanumeric characters.";
//        }
//        // write the response to the output stream using UTF-8 character encoding
//        OutputStream body = exchange.getResponseBody();
//        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
//        // println(..) will append a newline and auto-flush
//        // - to write without a newline, use e.g. print(..) and flush()
//        out.println(response);
//        
//        // if you do not close the exchange, the response will not be sent!
//        exchange.close();
//    }
}
