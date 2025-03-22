package javalab.service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.BookRepository;
import javalab.repository.CommentRepository;
import javalab.utility.InMemoryCache;
import javalab.utility.Resource;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {
    Logger logger = Logger.getLogger(getClass().getName());

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final InMemoryCache<Long, Book> bookCache;
    private final InMemoryCache<Long, Author> authorCache;

    @Autowired
    public BookService(BookRepository bookRepository,
                       CommentRepository commentRepository,
                       InMemoryCache<Long, Book> bookCache,
                       InMemoryCache<Long, Author> authorCache) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
        this.bookCache = bookCache;
        this.authorCache = authorCache;
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }


    public Book getById(Long id, Resource.LoadMode mode) {
        Book book = mode == Resource.LoadMode.DEFAULT ? bookCache.get(id) : null;
        if (book == null) {
            book = bookRepository.findById(id).orElseThrow(()
                    -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id"));
            if (mode == Resource.LoadMode.DIRECT) {
                bookCache.remove(id);
            } else {
                bookCache.put(id, book);
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
        return bookRepository.save(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = getById(id, Resource.LoadMode.DIRECT);

        for (Author author : book.getAuthors()) {
            authorCache.remove(author.getId());
            author.getBooks().remove(book);
        }
        commentRepository.deleteAll(book.getComments());
        bookRepository.delete(book);

        bookCache.remove(id);
    }

    @Transactional
    public Book update(Long id, Book book) {
        getById(id, Resource.LoadMode.DIRECT);
        book.setId(id);
        return bookRepository.save(book);
    }
}