package javalab.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javalab.config.CacheHolder;
import javalab.exception.BadRequestException;
import javalab.exception.ConflictException;
import javalab.exception.NotFoundException;
import javalab.mapper.CommentMapper;
import javalab.model.Author;
import javalab.model.Book;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import javalab.utility.Cache;
import javalab.utility.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookService bookService;
    @Mock
    private UserService userService;
    @Mock
    private CommentService commentService;
    @InjectMocks
    private AuthorService authorService;
    @Mock
    private Cache<Long, Author> authorCache;
    @Mock
    private Cache<Long, Book> bookCache;
    @Mock
    private CacheHolder cacheHolder;

    @Test
    void getById_ShouldReturnCachedAuthor_WhenDefaultModeAndCached() {
        Long id = 1L;
        Author cachedAuthor = new Author("Cached Author", id, Collections.emptyList());
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getAuthorCache().get(id)).thenReturn(cachedAuthor);

        // Act
        Author result = authorService.getById(id, Resource.LoadMode.DEFAULT);

        // Assert
        assertSame(cachedAuthor, result);
        verifyNoInteractions(authorRepository);
    }

    @Test
    void getById_ShouldFetchFromRepository_WhenDefaultModeAndNotCached() {
        // Arrange
        Long id = 2L;
        Author repoAuthor = new Author( "Repo Author", id, Collections.emptyList());
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getAuthorCache().get(id)).thenReturn(null);
        when(authorRepository.findById(id)).thenReturn(Optional.of(repoAuthor));

        // Act
        Author result = authorService.getById(id, Resource.LoadMode.DEFAULT);

        // Assert
        assertEquals(repoAuthor, result);
        verify(authorRepository).findById(id);
        verify(cacheHolder.getAuthorCache(), timeout(100)).put(id, repoAuthor);
    }

    @Test
    void getById_ShouldFetchFromRepositoryAndClearCache_WhenDirectMode() {
        // Arrange
        Long id = 3L;
        Author repoAuthor = new Author("Direct Author", id, Collections.emptyList());
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.of(repoAuthor));

        // Act
        Author result = authorService.getById(id, Resource.LoadMode.DIRECT);

        // Assert
        assertEquals(repoAuthor, result);
        verify(cacheHolder.getAuthorCache(), never()).get(id);
        verify(authorRepository).findById(id);
        verify(cacheHolder.getAuthorCache(), never()).put(any(), any());
    }

    @Test
    void getById_ShouldThrowNotFoundException_WhenAuthorNotFound() {
        // Arrange
        Long id = 999L;
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authorService.getById(id, Resource.LoadMode.DEFAULT));

        assertEquals(AuthorService.AUTHOR_ID_NOT_FOUND + id, exception.getMessage());

        verify(authorRepository).findById(id);
        verify(cacheHolder.getAuthorCache(), never()).put(any(), any());
    }

    @Test
    void getById_ShouldNotCheckCache_WhenDirectMode() {
        // Arrange
        Long id = 4L;
        Author repoAuthor = new Author("Direct Mode Author", id, Collections.emptyList());
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.of(repoAuthor));

        // Act
        Author result = authorService.getById(id, Resource.LoadMode.DIRECT);

        // Assert
        assertEquals(repoAuthor, result);
        verify(cacheHolder.getAuthorCache(), never()).get(id);
        verify(authorRepository).findById(id);
    }

    @Test
    void getById_ShouldCacheAuthor_WhenDefaultModeAndFetchedFromRepository() {
        // Arrange
        Long id = 5L;
        Author repoAuthor = new Author("Newly Cached Author", id, Collections.emptyList());
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.of(repoAuthor));

        // Act
        Author result = authorService.getById(id, Resource.LoadMode.DEFAULT);

        // Assert
        assertEquals(repoAuthor, result);

        verify(cacheHolder.getAuthorCache()).put(id, repoAuthor);
    }

    @Test
    void findAuthorByName_ShouldReturnId_WhenAuthorExists() {
        // Arrange
        String name = "J.R.R. Tolkien";
        Author author = new Author(name, 1L, Collections.emptyList());

        when(authorRepository.findByName(name)).thenReturn(Optional.of(author));

        // Act
        Optional<Long> result = authorService.findAuthorByName(name);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get());
        verify(authorRepository).findByName(name);
    }

    @Test
    void findAuthorByName_ShouldReturnEmpty_WhenAuthorNotFound() {
        // Arrange
        String name = "Unknown Author";
        when(authorRepository.findByName(name)).thenReturn(Optional.empty());

        // Act
        Optional<Long> result = authorService.findAuthorByName(name);

        // Assert
        assertFalse(result.isPresent());
        verify(authorRepository).findByName(name);
    }

    @Test
    void findAuthorByName_ShouldHandleNullInput() {
        // Act & Assert
        assertThrows(BadRequestException.class,
                () -> authorService.findAuthorByName(null));

        verifyNoInteractions(authorRepository);
    }

    @Test
    void addBookToAuthor_ShouldAddBook_WhenNotAlreadyPresent() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Author author = new Author("Existing Author", authorId, new ArrayList<>());
        Book book = new Book(bookId, "New Book", 2023, new ArrayList<>(), new ArrayList<>());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        // Act
        Author result = authorService.addBookToAuthor(authorId, bookId);

        // Assert
        assertSame(author, result);
        assertTrue(author.getBooks().contains(book));
        assertTrue(book.getAuthors().contains(author));

        verify(authorRepository).findById(authorId);
        verify(authorRepository).save(author);
        verify(bookRepository).save(book);
    }

    @Test
    void addBookToAuthor_ShouldThrowConflict_WhenBookAlreadyExists() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Book existingBook = new Book(bookId, "Existing Book", 2023, new ArrayList<>(), new ArrayList<>());
        Author author = new Author("Author", authorId, new ArrayList<>(List.of(existingBook)));

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class,
                () -> authorService.addBookToAuthor(authorId, bookId));

        assertEquals("Book already exists", exception.getMessage());
        verify(authorRepository, never()).save(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void addBookToAuthor_ShouldThrowNotFound_WhenAuthorDoesNotExist() {
        // Arrange
        Long authorId = 99L;
        Long bookId = 2L;

        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authorService.addBookToAuthor(authorId, bookId));

        verify(bookService, never()).getById(any(), any());
    }

    @Test
    void addBookToAuthor_ShouldThrowNotFound_WhenBookDoesNotExist() {
        // Arrange
        Long authorId = 99L;
        Long bookId = 2L;
        Author author = new Author();
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authorService.addBookToAuthor(authorId, bookId));

        verify(bookService, never()).getById(any(), any());
    }

    @Test
    void addBookToAuthor_ShouldMaintainBidirectionalRelationship() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Author author = new Author("Author", authorId, new ArrayList<>());
        Book book = new Book(bookId, "Book", 2023, new ArrayList<>(), new ArrayList<>());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        authorService.addBookToAuthor(authorId, bookId);

        // Assert
        assertTrue(author.getBooks().contains(book));
        assertTrue(book.getAuthors().contains(author));
    }

    @Test
    void addBookToAuthor_ShouldSaveBothEntities() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Author author = new Author("Author", authorId, new ArrayList<>());
        Book book = new Book(bookId, "Book", 2023, new ArrayList<>(), new ArrayList<>());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        // Act
        authorService.addBookToAuthor(authorId, bookId);

        // Assert
        verify(authorRepository).save(author);
        verify(bookRepository).save(book);
        // Verify save order if important
        InOrder inOrder = inOrder(bookRepository, authorRepository);
        inOrder.verify(bookRepository).save(book);
        inOrder.verify(authorRepository).save(author);
    }

    @Test
    void deleteBookFromAuthor_ShouldRemoveRelationship_WhenBookExists() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Author author = new Author("J.R.R. Tolkien", authorId, new ArrayList<>());
        Book book = new Book(bookId, "The Hobbit", 241, new ArrayList<>(), new ArrayList<>());
        author.getBooks().add(book);
        book.getAuthors().add(author);

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(authorRepository.save(author)).thenReturn(author);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        // Act
        authorService.deleteBookFromAuthor(authorId, bookId);

        // Assert
        assertFalse(author.getBooks().contains(book));
        assertFalse(book.getAuthors().contains(author));

        verify(bookRepository).save(book);
        verify(authorRepository).save(author);
    }

    @Test
    void deleteBookFromAuthor_ShouldThrowNotFoundException_WhenBookNotLinked() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 99L; // Not linked

        Author author = new Author("George Orwell", authorId, new ArrayList<>());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class,
                () -> authorService.deleteBookFromAuthor(authorId, bookId));

        verify(authorRepository, never()).save(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void deleteBookFromAuthor_ShouldMaintainBidirectionalConsistency() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Author author = new Author("Author", authorId, new ArrayList<>());
        Book book = new Book(bookId, "Book", 315, new ArrayList<>(), new ArrayList<>());
        author.getBooks().add(book);
        book.getAuthors().add(author);

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        authorService.deleteBookFromAuthor(authorId, bookId);

        // Assert
        assertFalse(author.getBooks().contains(book));
        assertFalse(book.getAuthors().contains(author));
    }

    @Test
    void deleteBookFromAuthor_ShouldSaveBothEntities() {
        // Arrange
        Long authorId = 1L;
        Long bookId = 2L;

        Author author = new Author("Author", authorId, new ArrayList<>());
        Book book = new Book(bookId, "Book", 14, new ArrayList<>(), new ArrayList<>());
        author.getBooks().add(book);
        book.getAuthors().add(author);

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        authorService.deleteBookFromAuthor(authorId, bookId);

        // Assert
        verify(authorRepository).save(author);
        verify(bookRepository).save(book);
    }

    @Test
    void deleteBookFromAuthor_ShouldNotAffectOtherRelationships() {
        // Arrange
        Long authorId = 1L;
        Long book1Id = 2L;
        Long book2Id = 3L;

        Author author = new Author("Author", authorId, new ArrayList<>());
        Book book1 = new Book(book1Id, "Book 1", 512, new ArrayList<>(), new ArrayList<>());
        Book book2 = new Book(book2Id, "Book 2", 231, new ArrayList<>(), new ArrayList<>());

        author.getBooks().addAll(List.of(book1, book2));
        book1.getAuthors().add(author);
        book2.getAuthors().add(author);
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
        when(bookRepository.findById(book1Id)).thenReturn(Optional.of(book1));
        // Act - Remove only book1
        authorService.deleteBookFromAuthor(authorId, book1Id);

        // Assert
        assertFalse(author.getBooks().contains(book1));
        assertTrue(author.getBooks().contains(book2));
        assertFalse(book1.getAuthors().contains(author));
        assertTrue(book2.getAuthors().contains(author));
    }

    @Test
    void update_ShouldUpdateAuthor_WhenAuthorExists() {
        // Arrange
        Long id = 1L;
        Author existingAuthor = new Author("Old Name", id, Collections.emptyList());
        Author updatedAuthor = new Author("New Name", id, Collections.emptyList());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(updatedAuthor)).thenReturn(updatedAuthor);

        // Act
        Author result = authorService.update(id, updatedAuthor);

        // Assert
        assertSame(updatedAuthor, result);
        assertEquals(id, updatedAuthor.getId());
        verify(cacheHolder.getAuthorCache()).remove(id);
        verify(authorRepository).save(updatedAuthor);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenAuthorDoesNotExist() {
        // Arrange
        Long id = 99L;
        Author author = new Author("New Author", id, Collections.emptyList());

        when(authorRepository.findById(id)).thenReturn(Optional.empty());
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        // Act & Assert
        assertThrows(NotFoundException.class, () -> authorService.update(id, author));
        verify(authorRepository, never()).save(any());
        verify(cacheHolder.getAuthorCache(), never()).remove(any());
    }

    @Test
    void update_ShouldClearCache_OnSuccessfulUpdate() {
        // Arrange
        Long id = 1L;
        Author existingAuthor = new Author("Existing", id, Collections.emptyList());
        Author updatedAuthor = new Author("Updated", id, Collections.emptyList());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(updatedAuthor)).thenReturn(updatedAuthor);

        // Act
        authorService.update(id, updatedAuthor);

        // Assert
        verify(cacheHolder.getAuthorCache()).remove(id);
    }

    @Test
    void update_ShouldMaintainBookRelationships() {
        // Arrange
        Long id = 1L;
        Book book = new Book(1L, "Sample Book", 300, null, Collections.emptyList());
        Author existingAuthor = new Author("Existing", id, new ArrayList<>(List.of(book)));
        Author updatedAuthor = new Author("Updated", id, new ArrayList<>(List.of(book)));

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(id)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(updatedAuthor)).thenReturn(updatedAuthor);

        // Act
        Author result = authorService.update(id, updatedAuthor);

        // Assert
        assertSame(updatedAuthor, result);
        assertEquals(1, updatedAuthor.getBooks().size());
        assertEquals("Sample Book", updatedAuthor.getBooks().getFirst().getTitle());
    }

    @Test
    void delete_ShouldHandleAuthorWithNoBooks() {
        // Arrange
        Long authorId = 1L;

        List<Book> books = List.of(new Book(), new Book());
        Author author = new Author("Author", authorId, books);

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

        // Act
        authorService.delete(authorId);

        // Assert
        verify(authorRepository).delete(author);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenAuthorDoesNotExist() {
        // Arrange
        Long authorId = 99L;
        when(authorRepository.findById(authorId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> authorService.delete(authorId));
        verify(authorRepository, never()).delete(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void delete_ShouldNotCallBookService_WhenNoBooksExist() {
        // Arrange
        Long authorId = 1L;
        Author author = new Author("Author", authorId, Collections.emptyList());

        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));

        // Act
        authorService.delete(authorId);

        // Assert
        verify(bookService, never()).getById(any(), any());
        verify(authorRepository).delete(author);
    }


    @Test
    void createAuthor_whenNullAuthor_shouldThrowException() {
        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            authorService.create(null);
        });

        verifyNoInteractions(authorRepository);
    }


    @Test
    void createAuthor_shouldHandleDatabaseErrors() {
        // Arrange
        Author validAuthor = new Author("Joe", 9L, Collections.emptyList());

        when(authorRepository.save(validAuthor))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authorService.create(validAuthor);
        });
    }

    @Test
    void getAuthor_shouldReturnAuthors() {
        Author authorA = new Author("Joe", 9L, Collections.emptyList());
        Author authorB = new Author("Rick", 10L, Collections.emptyList());

        when(authorRepository.findAll()).thenReturn(List.of(authorA, authorB));

        List<Author> res = authorService.getAuthors();

        assertEquals(List.of(authorA, authorB), res);

    }

}