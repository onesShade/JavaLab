package javalab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import javalab.model.Book;
import javalab.service.BookService;
import javalab.utility.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@Tag(name = "Book controller", description = "Allows to add/get/delete/update books")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/all")
    @Operation(
            summary = "Get all books",
            description = "Retrieves a list of all books. "
                    + "You can filter books by author or minimum comment count."
    )
    public List<Book> getAll(
            @RequestParam(value = "author", required = false) Optional<String> author,
            @RequestParam(value = "commentCountMin", required = false)
            Optional<Long> commentCountMin) {
        return bookService.getBookByFilter(author, commentCountMin);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a book by ID",
            description = "Retrieves a book by it's ID."
    )
    public Book getById(@PathVariable Long id) {
        return bookService.getById(id, Resource.LoadMode.DEFAULT);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new book",
            description = "Creates a new book with the provided details. Returns the created book."
    )
    public Book createBook(@Valid @RequestBody Book book, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid book data");
        }
        return bookService.create(book);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a book by ID")
    public void deleteBook(@PathVariable Long id) {
        bookService.delete(id);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Update a book by ID",
            description = "Updates an existing book by its unique ID. Returns the updated book."
    )
    public Book updateBook(@Valid @RequestBody Book book,
                           @PathVariable Long id, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ValidationException("Invalid book data");
        }
        return bookService.update(id, book);
    }
}