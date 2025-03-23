package javalab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import javalab.model.Author;
import javalab.service.AuthorService;
import javalab.utility.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/author")
@Tag(name = "Author controller", description = "Allows to add/get/delete/update authors. "
        + "Also allows to add/delete books to an author.")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/all")
    @Operation(summary = "Get all the authors")
    public List<Author> getAll() {
        return authorService.getAuthors();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by id")
    public Author getById(@PathVariable Long id) {
        return authorService.getById(id, Resource.LoadMode.DEFAULT);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new author",
            description = "Creates a new author with the provided details."
                        + "Returns the created author."
    )
    public Author createAuthor(@RequestBody Author author) {
        return authorService.create(author);
    }

    @PostMapping("/{authorId}/books/{bookId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Links book to author.",
            description = "Creates a connection between an existing book and"
                        + "an existing author."
    )
    public Author addBook(@PathVariable Long authorId,
                          @PathVariable Long bookId) {
        return authorService.addBookToAuthor(authorId, bookId);
    }

    @DeleteMapping("/{authorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletes author by id.")
    public void deleteAuthor(@PathVariable Long authorId) {
        authorService.delete(authorId);
    }

    @DeleteMapping("/{authorId}/books/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Deletes book from author.",
            description = "Unlinks a existing connection between book and author."
    )
    public void deleteBook(@PathVariable Long authorId,
                             @PathVariable Long bookId) {
        authorService.deleteBookFromAuthor(authorId, bookId);
    }

    @PutMapping("/{authorId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Updates author by id.",
            description = "Updates an existing author by its unique ID. Returns the updated author."
    )
    public Author updateAuthor(@PathVariable Long authorId, @RequestBody Author author) {
        return authorService.update(authorId, author);
    }
}