package javalab.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javalab.config.CacheHolder;
import javalab.exception.BadRequestException;
import javalab.exception.ConflictException;
import javalab.exception.NotFoundException;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
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
    private final CacheHolder cacheHolder;

    @Autowired
    public AuthorService(AuthorRepository authorRepository,
                         BookRepository bookRepository,
                         CacheHolder cacheHolder) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.cacheHolder = cacheHolder;
    }

    public Optional<Long> findAuthorByName(String name) {
        if (name == null) {
            throw new BadRequestException("Author name cannot be null");
        }
        return authorRepository.findByName(name)
                .map(Author::getId);
    }

    public Author getById(Long id, Resource.LoadMode mode) {
        Author author = mode == Resource.LoadMode.DEFAULT
                ? cacheHolder.getAuthorCache().get(id) : null;
        if (author == null) {
            Optional<Author> authorOptional = authorRepository.findById(id);
            if (authorOptional.isEmpty()) {
                throw new NotFoundException(AUTHOR_ID_NOT_FOUND + id);
            }
            author = authorOptional.get();
            if (mode == Resource.LoadMode.DIRECT) {
                cacheHolder.getAuthorCache().remove(id);
            } else {
                cacheHolder.getAuthorCache().put(id, author);
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
        if (author == null) {
            throw new BadRequestException("Author cannot be null");
        }
        return authorRepository.save(author);
    }

    @Transactional
    public Author addBookToAuthor(Long authorId, Long bookId) {
        Author author = getById(authorId, Resource.LoadMode.DIRECT);
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isEmpty()) {
            throw new NotFoundException("Book not found");
        }

        if (author.getBooks().contains(book.get())) {
            throw new ConflictException("Book already exists");
        }

        cacheHolder.getBookCache().remove(bookId);

        author.addBook(book.get());
        book.get().addAuthor(author);

        bookRepository.save(book.get());
        return authorRepository.save(author);
    }

    @Transactional
    public void deleteBookFromAuthor(Long authorId, Long bookId) {
        Author author = getById(authorId, Resource.LoadMode.DIRECT);
        Optional<Book> book = bookRepository.findById(bookId);
        if (book.isEmpty()) {
            throw new NotFoundException("Book not found");
        }

        cacheHolder.getBookCache().remove(bookId);

        if (!author.getBooks().contains(book.get())) {
            throw new NotFoundException("Author doesn't have book id: " + bookId);
        }

        book.get().removeAuthor(author);
        author.removeBook(book.get());

        bookRepository.save(book.get());
        authorRepository.save(author);
    }

    @Transactional
    public Author update(Long id, Author author) {
        getById(id, Resource.LoadMode.DEFAULT);
        author.setId(id);
        cacheHolder.getAuthorCache().remove(id);
        return authorRepository.save(author);
    }

    @Transactional
    public void delete(Long id) {
        Author author = getById(id, Resource.LoadMode.DIRECT);
        for (Book book : author.getBooks()) {
            book.removeAuthor(author);
            bookRepository.save(book);
            cacheHolder.getBookCache().remove(id);
        }
        authorRepository.delete(author);
    }
}