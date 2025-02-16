package com.java.lab;

import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    public int TryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @GetMapping(path = "/")
    public String GetGreeting() {
        return "Hello World By Ilya";
    }

    @GetMapping("/books")
    public String GetBooks(
            @RequestParam(value = "id", required = false) String id) {
        if(id == null){
            return "All books are available";
        }
        if(TryParseInt(id) != -1)
            return "Book with id " + id + " is available";
        return "Incorrect book id";
    }
    @GetMapping("/author/{id}")
    private String GetAuthor(@PathVariable String id) {
        if(TryParseInt(id) != -1)
            return "Author with id " + id + " is available";
        return "Incorrect author id";
    }
}