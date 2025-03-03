package javalab.service;

import java.util.List;
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
        return bookRepository.getBooks();
    }

    public List<Book> getBookByTitle(String title) {
        List<Book> books = bookRepository.getBooks();
        if (title != null) {
            books = books.stream()
                    .filter(book -> book.getTitle().equalsIgnoreCase(title))
                    .toList();
        }
        if (books.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        return books;
    }
}