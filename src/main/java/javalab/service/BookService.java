package javalab.service;

import java.util.List;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id"));
    }

    public List<Book> getBookByTitle(String title) {
        List<Book> books = bookRepository.findAll();
        if (title != null) {
            books = books.stream()
                    .filter(book -> book.getTitle().equalsIgnoreCase(title))
                    .toList();
        }
        return books;
    }

    public Book create(Book book) {
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        Book book = getById(id);
        for (Author author : book.getAuthors()) {
            author.getBooks().remove(book);
        }
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

