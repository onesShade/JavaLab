package javalab.service;

import java.util.ArrayList;
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

    public List<Book> getBookById(String id) {
        List<Book> books = new ArrayList<>(1);
        int verifiedId = Tools.tryParseInt(id);

        if (verifiedId == -1) {
            return books;
        }
        books.add(bookRepository.getBook(verifiedId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found")));
        return books;
    }
}