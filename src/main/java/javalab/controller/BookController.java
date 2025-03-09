package javalab.controller;

import java.util.List;
import javalab.model.Book;
import javalab.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
            @RequestParam(value = "title", required = false) String title) {
        return bookService.getBookByTitle(title);
    }

    @PostMapping
    public Book createBook(@RequestBody Book book) {
        return bookService.create(book);
    }
}