package javalab.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
import javalab.utility.InMemoryCache;
import javalab.utility.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthorService {
    Logger logger = Logger.getLogger(getClass().getName());

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final InMemoryCache<Long, Book> bookCache;
    private final InMemoryCache<Long, Author> authorCache;

    @Autowired
    public AuthorService(AuthorRepository authorRepository,
                         BookService bookService,
                         BookRepository bookRepository,
                         InMemoryCache<Long, Author> authorCache,
                         InMemoryCache<Long, Book> bookCache) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.authorCache = authorCache;
        this.bookCache = bookCache;
    }

    public Optional<Long> findAuthorByName(String name) {
        return authorRepository.findByName(name)
                .map(Author::getId);
    }


    public Author getById(Long id, Resource.LoadMode mode) {
        Author author = mode == Resource.LoadMode.DEFAULT ? authorCache.get(id) : null;
        if (author == null) {
            author = authorRepository.findById(id).orElseThrow(()
                    -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong author id"));
            if (mode == Resource.LoadMode.DIRECT) {
                authorCache.remove(id);
            } else {
                authorCache.put(id, author);
            }
        } else {
            logger.log(Level.INFO, "Author {0} was loaded from cache", id.intValue());
        }
        return author;
    }

    public List<Author> getAuthors() {
        return authorRepository.findAll();
    }

    @Transactional
    public Author create(Author author) {
        return authorRepository.save(author);
    }

    @Transactional
    public Author addBookToAuthor(Long authorId, Long bookId) {
        Author author = getById(authorId, Resource.LoadMode.DIRECT);
        Book book = bookService.getById(bookId, Resource.LoadMode.DIRECT);

        if (author.getBooks().contains(book)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book already exists");
        }

        author.addBook(book);
        book.addAuthor(author);

        bookRepository.save(book);
        return authorRepository.save(author);
    }

    @Transactional
    public void deleteBookFromAuthor(Long authorId, Long bookId) {
        Author author = getById(authorId, Resource.LoadMode.DIRECT);
        Book book = bookService.getById(bookId, Resource.LoadMode.DIRECT);

        if (!author.getBooks().contains(book)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book does not exist");
        }

        book.removeAuthor(author);
        author.removeBook(book);

        bookRepository.save(book);
        authorRepository.save(author);
    }

    @Transactional
    public Author update(Long id, Author author) {
        getById(id, Resource.LoadMode.DEFAULT);
        author.setId(id);
        authorCache.remove(id);
        return authorRepository.save(author);
    }

    @Transactional
    public void delete(Long id) {
        Author author = getById(id, Resource.LoadMode.DIRECT);
        for (Book book : author.getBooks()) {
            book.removeAuthor(author);
            bookRepository.save(book);
            bookCache.remove(id);
        }
        authorRepository.delete(author);
    }
}