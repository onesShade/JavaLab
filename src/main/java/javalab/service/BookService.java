package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.model.Book;
import javalab.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
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
}