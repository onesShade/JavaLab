package com.java.lab;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    public int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @GetMapping(path = "/")
    public String getGreeting() {
        return "Hello World By Ilya";
    }

    @GetMapping(value = "/books", produces = "text/plain")
    public String getBooks(
            @RequestParam(value = "id", required = false) String id) {
        if(id == null){
            return "All books are available";
        }
        if(tryParseInt(id) != -1)
            return "Book with id " + id + " is available";
        return "Incorrect book id";
    }
    @GetMapping(value = "/author/{id}", produces = "text/plain")
    public String getAuthor(@PathVariable String id) {
        if(tryParseInt(id) != -1)
            return "Author with id " + id + " is available";
        return "Incorrect author id";
    }
}