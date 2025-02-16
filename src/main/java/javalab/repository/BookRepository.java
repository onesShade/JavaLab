package javalab.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javalab.model.Book;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
    private final List<Book> books;

    public BookRepository() {
        books = new ArrayList<>();

        books.add(new Book(1, "Black", "Dmitro", 172));
        books.add(new Book(2, "Power and will", "Moss", 216));
        books.add(new Book(3, "Silence", "Great William", 71));
    }

    public List<Book> getBooks() {
        return books;
    }

    public Optional<Book> getBook(int id) {
        return books.stream().filter(b -> b.getId() == id).findFirst();
    }
}
