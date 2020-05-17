/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

//import static java.nio.charset.StandardCharsets.UTF_8;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.jupiter.api.Test;

/**
 * Tests for the WebServer datatype.
 */
public class WebServerTest {
    
    /*
     * Testing strategy
     *    handleLook:
     *      partition on playerID given in request: valid, invalid
     *    handleFlip: 
     *      partition on the request: valid, invalid
     *    handleScores: 
     *      partition on the request: valid, invalid
     *    handleWatch:
     *      partition on playerID given in request: valid, invalid
     */
    
//    @Test
//    public void testAssertionsEnabled() {
//        assertThrows(AssertionError.class, () -> { assert false; },
//                "make sure assertions are enabled with VM argument '-ea'");
//    }
//    
//    
//    // tests handleLook, covers valid playerID
//    @Test
//    public void testHandleLookValid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,1").openStream(); // will be displayed as none
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,2").openStream(); // will be displayed as none 
//        new URL("http://localhost:" + server.port() + "/flip/tom/2,1").openStream(); // will be displayed as "my b"
//        new URL("http://localhost:" + server.port() + "/flip/jerry/2,2").openStream(); // will be displayed as "up a"
//        
//        final URL valid = new URL("http://localhost:" + server.port() + "/look/tom");
//        
//        final HttpURLConnection connection = (HttpURLConnection) valid.openConnection();
//        assertEquals(200, connection.getResponseCode(), "response code");
//        
//        final InputStream input = valid.openStream();
//        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
//        assertEquals("3x2", reader.readLine(), "expected dimensions output");
//        assertEquals("none", reader.readLine(), "expected output");
//        assertEquals("none", reader.readLine(), "expected output");
//        assertEquals("my b", reader.readLine(), "expected output");
//        assertEquals("up a", reader.readLine(), "expected output");
//        assertEquals("down", reader.readLine(), "expected output");
//        assertEquals("down", reader.readLine(), "expected output");
//        assertEquals(null, reader.readLine(), "end of stream");
//        server.stop();
//    }
//    
//    // tests handleLook, covers invalid playerID
//    @Test
//    public void testHandleLookInvalid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        final URL invalid = new URL("http://localhost:" + server.port() + "/look/++");
//        final HttpURLConnection connection = (HttpURLConnection) invalid.openConnection();
//        assertEquals(404, connection.getResponseCode(), "response code");
//        server.stop();
//    }
//    
//    // tests handleFlip, covers valid playerID
//    @Test
//    public void testHandleFlipValid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,1").openStream(); // will be displayed as none
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,2").openStream(); // will be displayed as none
//        
//        final URL valid = new URL("http://localhost:" + server.port() + "/flip/tom/2,1"); // will be displayed as "my b"
//        
//        final InputStream input = valid.openStream();
//        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
//        assertEquals("3x2", reader.readLine(), "expected dimensions output");
//        assertEquals("none", reader.readLine(), "expected output");
//        assertEquals("none", reader.readLine(), "expected output");
//        assertEquals("my b", reader.readLine(), "expected output");
//        assertEquals("down", reader.readLine(), "expected output");
//        assertEquals("down", reader.readLine(), "expected output");
//        assertEquals("down", reader.readLine(), "expected output");
//        assertEquals(null, reader.readLine(), "end of stream");
//        server.stop();
//    }
//    
//    // tests handleFlip, covers invalid playerID
//    @Test
//    public void testHandleFlipInValid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        final URL invalid = new URL("http://localhost:" + server.port() + "/flip/jerry/17");
//        final HttpURLConnection connection = (HttpURLConnection) invalid.openConnection();
//        assertEquals(404, connection.getResponseCode(), "response code");
//        server.stop();
//    }
//    
//    // tests handleScores, covers valid request
//    @Test
//    public void testHandleScoresValid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,1").openStream(); 
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,2").openStream(); 
//        new URL("http://localhost:" + server.port() + "/flip/jerry/2,2").openStream(); 
//        
//        final URL valid = new URL("http://localhost:" + server.port() + "/scores"); 
//        
//        final HttpURLConnection connection = (HttpURLConnection) valid.openConnection();
//        assertEquals(200, connection.getResponseCode(), "response code");
//        
//        final InputStream input = valid.openStream();
//        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
//        List<String> responses = new ArrayList<>();
//        responses.add(reader.readLine());
//        responses.add(reader.readLine());
//        assertTrue(responses.contains("tom 1"), "expected tom to have scored 1");
//        assertTrue(responses.contains("jerry 0"), "expected jerry to have scored 0");
//        
//        server.stop();
//    }
//    
//    // tests handleScores, covers invalid request
//    @Test
//    public void testHandleScoresInValid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        final URL invalid = new URL("http://localhost:" + server.port() + "/scores/extraneous");
//        final HttpURLConnection connection = (HttpURLConnection) invalid.openConnection();
//        assertEquals(404, connection.getResponseCode(), "response code");
//        server.stop();
//    }
//    
//    // tests handleWatch, covers valid request
//    private URL validTest = null;
//    private BufferedReader readerTest = null;
//    private InputStream inputTest = null;
//    private String lineTest = null;
//    @Test
//    public void testHandleWatchValid() throws IOException, URISyntaxException, InterruptedException {
//
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        new URL("http://localhost:" + server.port() + "/flip/tom/1,1").openStream(); 
//        
//        new Thread(new Runnable() {
//            public void run() {
//                try {
//                    validTest = new URL("http://localhost:" + server.port() + "/watch/jerry");
//                    inputTest = validTest.openStream();
//                    readerTest = new BufferedReader(new InputStreamReader(inputTest, UTF_8));
//                    lineTest = readerTest.readLine();
//                } catch (MalformedURLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } 
//            }
//        }).start();
//        
//        Thread.sleep(1001);
//        assertEquals(null, lineTest, "watch should be blocking");
//        
//        new URL("http://localhost:" + server.port() + "/flip/jerry/1,2").openStream();
//        
//        while (readerTest == null) { // wait for /watch to return
//            continue;
//        }
////        assertEquals("3x2", reader.readLine(), "expected dimensions output");
//        assertEquals("up a", readerTest.readLine(), "expected output");
//        assertEquals("my a", readerTest.readLine(), "expected output");
//        assertEquals("down", readerTest.readLine(), "expected output");
//        assertEquals("down", readerTest.readLine(), "expected output");
//        assertEquals("down", readerTest.readLine(), "expected output");
//        assertEquals("down", readerTest.readLine(), "expected output");
//        assertEquals(null, readerTest.readLine(), "end of stream");
//        server.stop();
//    }
//    
//    // tests handleWatch, covers invalid request
//    @Test
//    public void testHandleWatchInValid() throws IOException, URISyntaxException {
//        List<List<String>> boardList = List.of(List.of("a", "a"), List.of("b", "a"), List.of("b", "b"));
//        Board board = new Board(boardList);
//        final WebServer server = new WebServer(board, 0);
//        server.start();
//        
//        final URL invalid = new URL("http://localhost:" + server.port() + "/watch/++");
//        final HttpURLConnection connection = (HttpURLConnection) invalid.openConnection();
//        assertEquals(404, connection.getResponseCode(), "response code");
//        server.stop();
//    }
    
}
