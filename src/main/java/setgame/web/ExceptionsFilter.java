/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package setgame.web;

import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Logging filter that reports exceptions.
 * <p>PS4 instructions: you may use, modify, or remove this class.
 */
public class ExceptionsFilter extends Filter {
    
    @Override public String description() { return "Log exceptions"; }
    
    @Override public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        try {
            chain.doFilter(exchange);
        } catch (IOException | RuntimeException e) {
            System.err.print(" !! ");
            e.printStackTrace();
            throw e; // after logging, let the exception continue
        }
    }
}
