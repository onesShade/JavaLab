/*
 * TestController By Ilya Minov
 * No copyright 16.02.2025
 * Simple REST service that handles query parameter endpoint and path parameter
 * endpoint.
 * Version for the first Lab Work, name of the controller is to be changed
 * later.
*/

package com.java.lab;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {
    private static final String MESSAGE = "message";

    public int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @GetMapping(value = "/", produces = "application/json")
    public Map<String, String> getGreeting() {
        Map<String, String> response = new HashMap<>();
        response.put(MESSAGE, "Hello World By Ilya");
        return response;
    }

    @GetMapping(value = "/books", produces = "application/json")
    public Map<String, String> getBooks(
            @RequestParam(value = "id", required = false) String id) {
        Map<String, String> response = new HashMap<>();

        if (id == null) {
            response.put(MESSAGE, "All books are available");
        } else if (tryParseInt(id) == -1) {
            response.put(MESSAGE, "Invalid book id");
            response.put("type", "exception");
        } else {
            response.put("author", "Ilya");
            response.put("id", id);
            response.put("type", "book");
        }
        return response;
    }

    @GetMapping(value = "/author/{id}", produces = "application/json")
    public Map<String, String> getAuthor(@PathVariable String id) {
        Map<String, String> response = new HashMap<>();
        if (tryParseInt(id) == -1) {
            response.put(MESSAGE, "Invalid author id");
            response.put("type", "exception");
        } else {
            response.put("name", "Ilya");
            response.put("id", id);
            response.put("type", "author");
        }
        return response;
    }
}