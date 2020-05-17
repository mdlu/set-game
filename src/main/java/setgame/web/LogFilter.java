/* Copyright (c) 2017-2020 MIT 6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package setgame.web;

import java.io.IOException;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

/**
 * Logging filter that reports request URLs and response codes to the console.
 * <p>PS4 instructions: you may use, modify, or remove this class.
 */
public class LogFilter extends Filter {
    
    @Override public String description() { return "Log requests"; }
    
    @Override public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        System.err.println(" -> " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
        chain.doFilter(exchange);
        System.err.println(" <- " + exchange.getResponseCode());
    }
}
