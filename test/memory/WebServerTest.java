/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package memory;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.Test;

/**
 * TODO
 */
public class WebServerTest {
    
    // Testing strategy
    //   TODO
    
    // Manual tests
    //   TODO (if any)
    
    @Test
    public void testAssertionsEnabled() {
        assertThrows(AssertionError.class, () -> { assert false; },
                "make sure assertions are enabled with VM argument '-ea'");
    }
    
    @Test
    public void testHelloValid() throws IOException, URISyntaxException {
        // Warning! This test is not legal because it provides a null board!
        // TODO You should revise or remove this test
        // TODO You should also avoid duplicating similar code in many tests
        final WebServer server = new WebServer(null, 0);
        server.start();
        
        final URL valid = new URL("http://localhost:" + server.port() + "/hello/w0rld");
        
        // in this test, we will just assert correctness of the server's output
        final InputStream input = valid.openStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8));
        assertEquals("Hello, w0rld!", reader.readLine(), "greeting");
        assertEquals(null, reader.readLine(), "end of stream");
        server.stop();
    }
    
    @Test
    public void testHelloInvalid() throws IOException, URISyntaxException {
        // Warning! This test is not legal because it provides a null board!
        // TODO You should revise or remove this test
        // TODO You should also avoid duplicating similar code in many tests
        final WebServer server = new WebServer(null, 0);
        server.start();
        
        final URL invalid = new URL("http://localhost:" + server.port() + "/hello/world!");
        
        // in this test, we will just assert correctness of the response code
        // unfortunately, an unsafe cast is required here to go from general
        //   URLConnection to the HTTP-specific HttpURLConnection that will
        //   always be returned when we connect to a "http://" URL
        final HttpURLConnection connection = (HttpURLConnection) invalid.openConnection();
        assertEquals(404, connection.getResponseCode(), "response code");
        server.stop();
    }
    
    // TODO tests
    
}
