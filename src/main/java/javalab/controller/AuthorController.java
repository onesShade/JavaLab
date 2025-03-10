package javalab.controller;

import java.util.List;
import javalab.model.Author;
import javalab.service.AuthorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/author")
public class AuthorController {

    private final AuthorService authorService;

    @Autowired
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public List<Author> getAuthors() {
        return authorService.getAuthors();
    }

    @PostMapping
    public Author createAuthor(@RequestBody Author author,
                               @RequestParam(name = "bookId", required = true) List<Long> bookIds) {
        return authorService.create(author, bookIds);
    }

    @DeleteMapping("/{authorId}")
    public void deleteAuthor(@PathVariable Long authorId) {
        authorService.delete(authorId);
    }

    @DeleteMapping("/{authorId}/{bookId}")
    public void deleteAuthor(@PathVariable Long authorId, @PathVariable Long bookId) {
        authorService.deleteBookFromAuthor(authorId, bookId);
    }

    @PutMapping("/{authorId}")
    public Author updateAuthor(@PathVariable Long authorId, @RequestBody Author author) {
        return authorService.update(authorId, author);
    }

}