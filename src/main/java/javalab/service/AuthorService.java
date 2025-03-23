package javalab.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javalab.exception.ConflictException;
import javalab.exception.NotFoundException;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
import javalab.utility.Cache;
import javalab.utility.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthorService {
    public static final String AUTHOR_ID_NOT_FOUND = "Author id not found: ";

    Logger logger = Logger.getLogger(getClass().getName());

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final Cache<Long, Book> bookCache;
    private final Cache<Long, Author> authorCache;

    @Autowired
    public AuthorService(AuthorRepository authorRepository,
                         BookService bookService,
                         BookRepository bookRepository,
                         Cache<Long, Author> authorCache,
                         Cache<Long, Book> bookCache) {
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
                    -> new NotFoundException(AUTHOR_ID_NOT_FOUND + id));
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
            throw new ConflictException("Book already exists");
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
            throw new NotFoundException("Author doesn't have book id: " + bookId);
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