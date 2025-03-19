package javalab.service;

import java.util.List;
import java.util.Optional;

import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.BookRepository;
import javalab.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public BookService(BookRepository bookRepository, CommentRepository commentRepository) {
        this.bookRepository = bookRepository;
        this.commentRepository = commentRepository;
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id"));
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
        }
        commentRepository.deleteAll(book.getComments());
        bookRepository.delete(book);
    }

    public Book update(Long id, Book book) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id");
        }
        book.setId(id);
        return bookRepository.save(book);
    }
}

