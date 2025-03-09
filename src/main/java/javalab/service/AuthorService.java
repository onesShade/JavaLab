package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.AuthorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookService bookService;

    @Autowired
    public AuthorService(AuthorRepository authorRepository, BookService bookService) {
        this.authorRepository = authorRepository;
        this.bookService = bookService;
    }

    public Optional<Long> findAuthorByName(String name) {
        return authorRepository.findByName(name)
                .map(Author::getId);
    }

    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    @Transactional
    public Author create(Author author, List<Long> bookIds) {

        for (Long bookId : bookIds) {
            Optional<Book> book = bookService.getBookById(bookId);
            if (book.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong id");
            }
            author.addBook(book.get());
            book.get().addAuthor(author);
        }
        return authorRepository.save(author);
    }
}