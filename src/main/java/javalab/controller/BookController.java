/*
 * TestController By Ilya Minov
 * No copyright 16.02.2025
 * Simple REST service that handles query parameter endpoint and path parameter
 * endpoint.
 * Version for the first Lab Work, name of the controller is to be changed
 * later.
*/

package javalab.controller;

import java.util.List;
import javalab.model.Book;
import javalab.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getBooks(
            @RequestParam(value = "id", required = false) String id) {
        if (id == null) {
            return bookService.getBooks();
        }
        return bookService.getBookById(id);
    }
}