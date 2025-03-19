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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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

    public Book getById(Long id) {
        Book book = (Book) bookCache.get(id);
        if (book == null) {
            book = bookRepository.findById(id).orElseThrow(()
                    -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id"));
            bookCache.put(id, book);
        } else {
            logger.log(Level.INFO, "Book {0} was loaded from cache", id.intValue());
        }
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

    public Book create(Book book) {
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        Book book = getById(id);
        for (Author author : book.getAuthors()) {
            author.getBooks().remove(book);
            authorCache.remove(author.getId());
        }
        bookCache.remove(id);
        commentRepository.deleteAll(book.getComments());
        bookRepository.delete(book);
    }

    public Book update(Long id, Book book) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id");
        }
        bookCache.remove(id);
        book.setId(id);
        return bookRepository.save(book);
    }
}

