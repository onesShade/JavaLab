package javalab.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javalab.config.CacheHolder;
import javalab.exception.BadRequestException;
import javalab.exception.NotFoundException;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.BookRepository;
import javalab.repository.CommentRepository;
import javalab.utility.Resource;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {
    public static final String BOOK_ID_NOT_FOUND = "Book id not found: ";
    Logger logger;

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final CacheHolder cacheHolder;

    @Autowired
    public BookService(BookRepository bookRepository,
                       CommentRepository commentRepository,
                       CacheHolder cacheHolder, AuthorService authorService) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
        this.cacheHolder = cacheHolder;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public Book getById(Long id, Resource.LoadMode mode) {
        Book book = mode == Resource.LoadMode.DEFAULT ? cacheHolder.getBookCache().get(id) : null;
        if (book == null) {
            book = bookRepository.findById(id).orElseThrow(()
                    -> new NotFoundException(BOOK_ID_NOT_FOUND + id));
            if (mode == Resource.LoadMode.DIRECT) {
                cacheHolder.getBookCache().remove(id);
            } else {
                cacheHolder.getBookCache().put(id, book);
            }
        } else {
            logger.log(Level.INFO, "Book {0} was loaded from cache", id.intValue());
        }
        Hibernate.initialize(book.getAuthors());
        return book;
    }

    public List<Book> getBookByFilter(Optional<String> author, Optional<Long> commentCountMin) {
        if (author.isPresent() && commentCountMin.isPresent()) {
            return bookRepository.findByAuthorNameAndCommentCount(author.get(),
                    commentCountMin.get());
        }
        if (author.isPresent()) {
            return bookRepository.findByAuthor(author.get());
        }
        if (commentCountMin.isPresent()) {
            return bookRepository.findByCommentCount(commentCountMin.get());
        }
        return bookRepository.findAll();
    }

    @Transactional
    public Book create(Book book) {
        if (book == null) {
            throw new BadRequestException("Book cannot be null");
        }
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getById(id, Resource.LoadMode.DIRECT);

        for (Author author : book.getAuthors()) {
            cacheHolder.getAuthorCache().remove(author.getId());
            author.getBooks().remove(book);
        }
        if (!book.getComments().isEmpty()) {
            commentRepository.deleteAll(book.getComments());
        }

        bookRepository.delete(book);
        cacheHolder.getBookCache().remove(id);
    }

    @Transactional
    public Book update(Long id, Book book) {
        getById(id, Resource.LoadMode.DIRECT);
        book.setId(id);
        return bookRepository.save(book);
    }
}