package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;

    @Autowired
    public AuthorService(AuthorRepository authorRepository,
                         BookService bookService,
                         BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
    }

    public Optional<Long> findAuthorByName(String name) {
        return authorRepository.findByName(name)
                .map(Author::getId);
    }

    public Author getById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong author id"));
    }

    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    @Transactional
    public Author create(Author author, List<Long> bookIds) {
        for (Long bookId : bookIds) {
            Book book = bookService.getById(bookId);
            author.addBook(book);
            book.addAuthor(author);
        }
        return authorRepository.save(author);
    }

    public void deleteBookFromAuthor(Long authorId, Long bookId) {
        Author author = getById(authorId);
        Book book = bookService.getById(bookId);

        book.removeAuthor(author);
        author.removeBook(book);
        bookRepository.save(book);
        if (!author.getBooks().isEmpty()) {
            authorRepository.save(author);
        } else {
            authorRepository.delete(author);
        }
    }

    public Author update(Long id, Author author) {
        getById(id);
        author.setId(id);
        return authorRepository.save(author);
    }

    public void delete(Long id) {
        Author author = getById(id);

        for (Book book : author.getBooks()) {
            book.removeAuthor(author);
        }
        authorRepository.delete(author);
    }
}