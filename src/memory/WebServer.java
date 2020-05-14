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
import java.util.Set;
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
    
    private static final int ERROR_CODE = 404;
    private static final int SUCCESS_CODE = 200;
    
    private final HttpServer server;
    private final Board board;
    
    // TODO update this if we want to be thorough
    // TODO DRY out this code
    // TODO might not be threadsafe, interleaving between actions and calls to boardResponse()
    /* Abstraction function:
     *    AF(server, board): a game of Memory Scramble serviced by server and with a current
     *        game state represented by board
     *        
     * Representation invariant:
     *    true
     *    
     * Safety from rep exposure:
     *    all fields are private and final
     *    all methods return immutable values or void
     * 
     * Thread safety argument:
     *    all fields are private and threadsafe
     *    boardResponse uses a method guaranteed to return a consistent report of the 
     *      current states of all squares on the Board, and which are held by the player
     *    all handling methods can be called concurrently, since the HttpServer uses
     *      multiple threads to handle this, and the board is safe for concurrency 
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
        this.board = board;
        board.addBoardListener(() -> {
           synchronized (board) {
               board.notifyAll();
           }
        });
        
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
        
        // handle requests for /declare/player
        HttpContext declare = server.createContext("/declare/", this::handleDeclare);
        declare.getFilters().addAll(filters);
        
        // handle requests for /pick/player/row,column
        HttpContext pick = server.createContext("/pick/", this::handlePick);
        pick.getFilters().addAll(filters);
        
        // handle requests for /add/player
        HttpContext add = server.createContext("/add/", this::handleAdd);
        add.getFilters().addAll(filters);
        
        // handle requests for /scores
        HttpContext scores = server.createContext("/scores", this::handleScores);
        scores.getFilters().addAll(filters);

        // handle requests for /watch/player
        HttpContext watch = server.createContext("/watch/", this::handleWatch);
        watch.getFilters().addAll(filters);
        
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
     * Converts a board into the proper String representation to send as an HTTP response.
     * @param playerID the unique ID of the player
     * @return the String representation
     */
    private String boardResponse(String playerID) {
        List<Square> emptySquares = board.getEmptySquares();
        List<Square> squaresHeld = board.getSquaresHeld();
        String declarer = board.getDeclarer();
        
        final String modifier;
        if (declarer.equals(playerID)) {
            modifier = "my ";
        } else {
            modifier = "up ";
        }
        
        String response = board.getNumRows()+"x"+board.getNumCols()+"\n";
        if (declarer.equals("")) {
            response += "none\n";
        } else {
            response += modifier + "\n"; // TODO update with MILLIS
        }
        for (int row=0; row<board.getNumRows(); row++) {
            for (int col=0; col<board.getNumCols(); col++) {
                Square sq = new Square(row, col);
                if (emptySquares.contains(sq)) {
                    response += "none\n";
                } else if (squaresHeld.contains(sq)) {
                    response += modifier + board.getCard(sq).toString() + "\n";
                } else {
                    response += board.getCard(sq).toString() + "\n";
                }
            }
        }
        return response;
    }
    
    /**
     * Handles the /look/player route. Sends a response showing the board, formatted as described 
     * in the grammar in the API, or reports "Your player name ID contains non-alphanumeric characters." 
     * if the playerID is not alphanumeric.
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
        if (player.matches("\\w+")) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(SUCCESS_CODE, 0);
            if (!board.isPlayer(player)) {
                board.addPlayer(player);
            }
            response = boardResponse(player);
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            response = "Your player name ID contains non-alphanumeric characters.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /declare/player route.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handleDeclare(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String playerID = path.substring(base.length());
        
        final String response;
        if (playerID.matches("\\w+")) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(SUCCESS_CODE, 0);
            if (!board.isPlayer(playerID)) {
                board.addPlayer(playerID);
            }
            board.declareSet(playerID);
            response = boardResponse(playerID);
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            response = "Your player name ID contains non-alphanumeric characters.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /pick/player/row,column route. Attempts to flip the card at (row, column) for the given player.
     * Sends a response showing the board, formatted as described in the API, or reports 
     * "Your requested pick was not valid." if the request was not formatted correctly or specified an out-of-bounds
     * square, or reports "Your requested flip was interrupted." in case of an InterruptedException.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handlePick(HttpExchange exchange) throws IOException {
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
            final Square square = new Square(Integer.valueOf(splitParameters[1]),
                    Integer.valueOf(splitParameters[2]));
            
            if (!board.isPlayer(playerID)) {
                board.addPlayer(playerID);
            }
            
            try {
                board.pickCard(square, playerID);
                exchange.sendResponseHeaders(SUCCESS_CODE, 0);
                response = boardResponse(playerID);
            } catch (InterruptedException e) {
                exchange.sendResponseHeaders(ERROR_CODE, 0);
                response = "Your requested pick was interrupted.";
            } catch (NullPointerException npe) {
                exchange.sendResponseHeaders(ERROR_CODE, 0);
                response = "Your requested pick was not valid.";
            }
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            response = "Your requested pick was not valid.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /add/player route.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handleAdd(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String playerID = path.substring(base.length());
        
        final String response;
        if (playerID.matches("\\w+")) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(SUCCESS_CODE, 0);
            if (!board.isPlayer(playerID)) {
                board.addPlayer(playerID);
            }
            board.vote(playerID);
            
            Set<String> votes = board.getVotes();
            Map<String, Integer> scores = board.getScores();
            String scoreString = "";
            for (String player: scores.keySet()) {
                String vote;
                if (votes.contains(player)) {
                    vote = "add";
                } else {
                    vote = "none";
                }
                scoreString += player + " " + scores.get(player) + " " + vote + "\n";
            }
            response = scoreString;
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            response = "Your player name ID contains non-alphanumeric characters.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /scores route. Returns a list of the current scores of each player in the game,
     * formatted as specified in the API, or reports that "There should be no additional 
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
            exchange.sendResponseHeaders(SUCCESS_CODE, 0);
            Map<String, Integer> scores = board.getScores();
            String scoreString = "";
            for (String player: scores.keySet()) {
                scoreString += player + " " + scores.get(player) + "\n";
            }
            response = scoreString;
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            response = "There should be no additional characters following /scores in the request.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
    
    /**
     * Handles the /watch/player route.
     * @param exchange the HttpExchange used
     * @throws IOException
     */
    private void handleWatch(HttpExchange exchange) throws IOException {
        // if you want to know the requested path:
        final String path = exchange.getRequestURI().getPath();
        
        // it will always start with the base path from server.createContext():
        final String base = exchange.getHttpContext().getPath();
        assert path.startsWith(base);
        
        final String player = path.substring(base.length());
        
        final String response;
        if (player.matches("\\w+")) {
            // if the request is valid, respond with HTTP code 200 to indicate success
            // - response length 0 means a response will be written
            // - you must call this method before calling getResponseBody()
            exchange.sendResponseHeaders(SUCCESS_CODE, 0);
            synchronized (board) {
                try {
                    board.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            response = boardResponse(player);
        } else {
            // otherwise, respond with HTTP code 404 to indicate an error
            exchange.sendResponseHeaders(ERROR_CODE, 0);
            response = "Your player name ID may only consist of alphanumeric characters.";
        }
        // write the response to the output stream using UTF-8 character encoding
        OutputStream body = exchange.getResponseBody();
        PrintWriter out = new PrintWriter(new OutputStreamWriter(body, UTF_8), true);
        // println(..) will append a newline and auto-flush
        // - to write without a newline, use e.g. print(..) and flush()
        out.print(response);
        out.flush();
        
        // if you do not close the exchange, the response will not be sent!
        exchange.close();
    }
}
