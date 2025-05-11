package javalab.mapper;

import javalab.dto.BookDto;
import javalab.model.Author;
import javalab.model.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    public BookDto toDto(Book book) {
        BookDto bookDto = new BookDto();
        bookDto.setId(book.getId());
        bookDto.setTitle(book.getTitle());
        bookDto.setAuthors(book.getAuthors().stream().map(Author::getName).toList());
        return bookDto;
    }
}