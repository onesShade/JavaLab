package javalab.service;

import static javalab.service.BookService.BOOK_ID_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javalab.config.CacheHolder;
import javalab.exception.BadRequestException;
import javalab.exception.NotFoundException;
import javalab.mapper.CommentMapper;
import javalab.model.Author;
import javalab.model.Book;
import javalab.model.Comment;
import javalab.repository.AuthorRepository;
import javalab.repository.BookRepository;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import javalab.utility.Cache;
import javalab.utility.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {
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
    @InjectMocks
    private BookService bookService;
    @Mock
    private UserService userService;
    @Mock
    private CommentService commentService;
    @Mock
    private AuthorService authorService;
    @Mock
    private Cache<Long, Author> authorCache;
    @Mock
    private Cache<Long, Book> bookCache;
    @Mock
    private CacheHolder cacheHolder;

    @Test
    void getBooks_shouldReturnAllBooks() {
        // Arrange
        Author author = new Author("J.K. Rowling");
        author.setId(1L);

        Book book1 = new Book("Harry Potter and the Philosopher's Stone",
                List.of(author), 223);
        book1.setId(1L);

        Book book2 = new Book("Clean Code", List.of(new Author("Robert C. Martin")), 464);
        book2.setId(2L);

        List<Book> expectedBooks = List.of(book1, book2);

        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // Act
        List<Book> actualBooks = bookService.getBooks();

        // Assert
        assertNotNull(actualBooks);
        assertEquals(2, actualBooks.size());
        assertEquals(expectedBooks, actualBooks);
        verify(bookRepository, times(1)).findAll();
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getBooks_whenNoBooksExist_shouldReturnEmptyList() {
        // Arrange
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Book> actualBooks = bookService.getBooks();

        // Assert
        assertNotNull(actualBooks);
        assertTrue(actualBooks.isEmpty());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getById_DefaultMode_ReturnsCachedBook() {
        // Arrange
        Long bookId = 1L;
        Book cachedBook = new Book(bookId, "Cached Book", 200, new ArrayList<>(), new ArrayList<>());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookCache.get(bookId)).thenReturn(cachedBook);

        // Act
        Book result = bookService.getById(bookId, Resource.LoadMode.DEFAULT);

        // Assert
        assertSame(cachedBook, result);
        verify(bookCache).get(bookId);
        verifyNoInteractions(bookRepository);
        verify(bookCache, never()).put(any(), any());
        verify(bookCache, never()).remove(any());
    }

    @Test
    void getById_DefaultMode_FetchesFromRepoWhenNotCached() {
        // Arrange
        Long bookId = 2L;
        Book repoBook = new Book(bookId, "Repo Book", 300, new ArrayList<>(), new ArrayList<>());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookCache.get(bookId)).thenReturn(null);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(repoBook));

        // Act
        Book result = bookService.getById(bookId, Resource.LoadMode.DEFAULT);

        // Assert
        assertSame(repoBook, result);
        verify(bookRepository).findById(bookId);
        verify(bookCache).put(bookId, repoBook);
    }

    @Test
    void getById_DirectMode_AlwaysFetchesFromRepo() {
        // Arrange
        Long bookId = 3L;
        Book repoBook = new Book(bookId, "Direct Book", 400, new ArrayList<>(), new ArrayList<>());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(repoBook));

        // Act
        Book result = bookService.getById(bookId, Resource.LoadMode.DIRECT);

        // Assert
        assertSame(repoBook, result);
        verify(bookCache, never()).get(bookId);
        verify(bookRepository).findById(bookId);
        verify(bookCache).remove(bookId);
        verify(bookCache, never()).put(any(), any());
    }

    @Test
    void getById_DefaultMode_ThrowsWhenBookNotFound() {
        // Arrange
        Long bookId = 99L;

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookCache.get(bookId)).thenReturn(null);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookService.getById(bookId, Resource.LoadMode.DEFAULT);
        });

        assertEquals(BOOK_ID_NOT_FOUND + bookId, exception.getMessage());
        verify(bookRepository).findById(bookId);
        verify(bookCache, never()).put(any(), any());
    }

    @Test
    void getById_DirectMode_ThrowsWhenBookNotFound() {
        // Arrange
        Long bookId = 99L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookService.getById(bookId, Resource.LoadMode.DIRECT);
        });

        assertEquals(BOOK_ID_NOT_FOUND + bookId, exception.getMessage());
        verify(bookCache, never()).get(any());
    }

    @Test
    void getById_InitializesLazyCollections() {
        // Arrange
        Long bookId = 5L;
        List<Author> authors = new ArrayList<>();
        authors.add(new Author("Test Author", 1L, new ArrayList<>()));
        Book repoBook = new Book(bookId, "Lazy Book", 500, authors, new ArrayList<>());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookCache.get(bookId)).thenReturn(null);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(repoBook));

        // Act
        Book result = bookService.getById(bookId, Resource.LoadMode.DEFAULT);

        // Assert
        assertNotNull(result.getAuthors());
        assertEquals(1, result.getAuthors().size());
        verify(bookCache).put(bookId, repoBook);
    }

    @Test
    void getById_WithInvalidId() {
        // Arrange
        Long bookId = 999L;

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookCache.get(bookId)).thenReturn(null);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookService.getById(bookId, Resource.LoadMode.DEFAULT));
        // Assert
        assertEquals(BOOK_ID_NOT_FOUND + bookId, exception.getMessage());

        // Verify cache interactions
        verify(bookCache).get(bookId);
        verify(bookRepository).findById(bookId);
        verify(bookCache, never()).put(any(), any());
    }

    @Test
    void getBookByFilter_WithAuthorOnly_ShouldCallFindByAuthor() {
        // Arrange
        String authorName = "George Orwell";
        List<Book> expectedBooks = List.of(
                new Book(1L, "1984", 300, List.of(new Author(authorName)), List.of()),
                new Book(2L, "Animal Farm", 200, List.of(new Author(authorName)), List.of())
        );

        when(bookRepository.findByAuthor(authorName)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.getBookByFilter(
                Optional.of(authorName),
                Optional.empty()
        );

        // Assert
        assertEquals(expectedBooks, result);
        verify(bookRepository).findByAuthor(authorName);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getBookByFilter_WithCommentCountOnly_ShouldCallFindByCommentCount() {
        // Arrange
        long commentCount = 10L;
        List<Book> expectedBooks = List.of(
                new Book(1L, "Popular Book 1", 300, List.of(), List.of()),
                new Book(2L, "Popular Book 2", 400, List.of(), List.of())
        );

        when(bookRepository.findByCommentCount(commentCount)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.getBookByFilter(
                Optional.empty(),
                Optional.of(commentCount)
        );

        // Assert
        assertEquals(expectedBooks, result);
        verify(bookRepository).findByCommentCount(commentCount);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getBookByFilter_WithNoFilters_ShouldCallFindAll() {
        // Arrange
        List<Book> expectedBooks = List.of(
                new Book(1L, "Book 1", 300, List.of(), List.of()),
                new Book(2L, "Book 2", 400, List.of(), List.of()),
                new Book(3L, "Book 3", 500, List.of(), List.of())
        );

        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.getBookByFilter(
                Optional.empty(),
                Optional.empty()
        );

        // Assert
        assertEquals(expectedBooks, result);
        verify(bookRepository).findAll();
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getBookByFilter_WithZeroCommentCount_ShouldCallCorrectRepositoryMethod() {
        // Arrange
        long commentCount = 0L;
        List<Book> expectedBooks = List.of(
                new Book(1L, "Uncommented Book", 300, List.of(), List.of())
        );

        when(bookRepository.findByCommentCount(commentCount)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.getBookByFilter(
                Optional.empty(),
                Optional.of(commentCount)
        );

        // Assert
        assertEquals(expectedBooks, result);
        verify(bookRepository).findByCommentCount(commentCount);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @Transactional
    void create_ShouldSaveBookAndReturnIt() {
        // Arrange
        Book newBook = new Book("New Book", new ArrayList<>(), 200);
        Book savedBook = new Book(1L, "New Book", 200, new ArrayList<>(), new ArrayList<>());

        when(bookRepository.save(newBook)).thenReturn(savedBook);

        // Act
        Book result = bookService.create(newBook);

        // Assert
        assertEquals(savedBook, result);
        verify(bookRepository).save(newBook);
    }

    @Test
    void create_ShouldThrowExceptionWhenBookIsNull() {
        assertThrows(BadRequestException.class, () -> bookService.create(null));
        verifyNoInteractions(bookRepository);
        verifyNoInteractions(bookCache);
    }

    @Test
    @Transactional
    void create_ShouldHandleEmptyTitle() {
        // Arrange
        Book book = new Book("", new ArrayList<>(), 100);
        Book savedBook = new Book(1L, "", 100, new ArrayList<>(), new ArrayList<>());

        when(bookRepository.save(book)).thenReturn(savedBook);

        // Act
        Book result = bookService.create(book);

        // Assert
        assertEquals(savedBook, result);
        verify(bookRepository).save(book);
    }

    @Test
    @Transactional
    void create_ShouldHandleZeroPages() {
        // Arrange
        Book book = new Book("Zero Pages Book", new ArrayList<>(), 0);
        Book savedBook = new Book(1L, "Zero Pages Book", 0, new ArrayList<>(), new ArrayList<>());

        when(bookRepository.save(book)).thenReturn(savedBook);

        // Act
        Book result = bookService.create(book);

        // Assert
        assertEquals(savedBook, result);
        verify(bookRepository).save(book);
    }

    @Test
    @Transactional
    void create_ShouldHandleNegativePages() {
        // Arrange
        Book book = new Book("Negative Pages", new ArrayList<>(), -50);
        Book savedBook = new Book(1L, "Negative Pages", -50, new ArrayList<>(), new ArrayList<>());

        when(bookRepository.save(book)).thenReturn(savedBook);

        // Act
        Book result = bookService.create(book);

        // Assert
        assertEquals(savedBook, result);
        verify(bookRepository).save(book);
    }

    @Test
    @Transactional
    void create_ShouldPropagateDatabaseExceptions() {
        // Arrange
        Book validBook = new Book("Valid Book", new ArrayList<>(), 200);
        when(bookRepository.save(validBook)).thenThrow(new DataAccessException("DB error") {});

        // Act & Assert
        assertThrows(DataAccessException.class, () -> bookService.create(validBook));
        verify(bookCache, never()).put(any(), any());
    }

    @Test
    @Transactional
    void create_ShouldHandleExistingAuthors() {
        // Arrange
        Author author = new Author("Existing Author", 1L, Collections.emptyList());
        Book book = new Book("Book With Author", List.of(author), 300);
        Book savedBook = new Book(1L, "Book With Author", 300, List.of(author), new ArrayList<>());

        when(bookRepository.save(book)).thenReturn(savedBook);

        // Act
        Book result = bookService.create(book);

        // Assert
        assertEquals(1, result.getAuthors().size());
        assertEquals("Existing Author", result.getAuthors().get(0).getName());
    }

    @Test
    @Transactional
    void create_ShouldHandleEmptyAuthorList() {
        // Arrange
        Book book = new Book("No Authors Book", new ArrayList<>(), 400);
        Book savedBook = new Book(1L, "No Authors Book", 400, new ArrayList<>(), new ArrayList<>());

        when(bookRepository.save(book)).thenReturn(savedBook);

        // Act
        Book result = bookService.create(book);

        // Assert
        assertTrue(result.getAuthors().isEmpty());
    }


    @Test
    void delete_ShouldRemoveBookAndCleanupRelations() {
        // Arrange
        Long bookId = 1L;
        Author author1 = new Author("Author 1");
        Author author2 = new Author("Author 2");
        Book book = new Book(bookId, "Book to Delete",300, List.of(author1, author2), Collections.emptyList());
        book.setComments(List.of(new Comment(), new Comment()));

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        bookService.delete(bookId);

        // Assert
        // Verify author relationships were cleaned up
        assertFalse(author1.getBooks().contains(book));
        assertFalse(author2.getBooks().contains(book));

        // Verify repository calls
        verify(commentRepository).deleteAll(book.getComments());
        verify(bookRepository).delete(book);

        // Verify cache was cleared
        verify(bookCache, atLeast(1)).remove(bookId);
    }

    @Test
    void delete_ShouldHandleBookWithNoAuthors() {
        // Arrange
        Long bookId = 1L;
        Book book = new Book(bookId, "Book with no authors", 150, new ArrayList<>(), new ArrayList<>());
        book.setComments(List.of(new Comment()));

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        bookService.delete(bookId);

        // Assert
        verify(authorService, never()).getById(any(), any());
        verify(commentRepository).deleteAll(book.getComments());
        verify(bookRepository).delete(book);
        verify(bookCache, atLeast(1)).remove(bookId);
    }

    @Test
    void delete_ShouldHandleBookWithNoComments() {
        // Arrange
        Long bookId = 1L;
        Author author = new Author("Single Author");
        Book book = new Book(bookId, "Book with no comments", 180, List.of(author), new ArrayList<>());
        book.setComments(new ArrayList<>());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(cacheHolder.getAuthorCache()).thenReturn(authorCache);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act
        bookService.delete(bookId);

        // Assert
        verify(commentRepository, never()).deleteAll(any());
        verify(bookRepository).delete(book);
        verify(bookCache, atLeast(1)).remove(bookId);
    }

    @Test
    void delete_ShouldThrowNotFoundExceptionWhenBookDoesNotExist() {
        // Arrange
        Long nonExistentId = 999L;

        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> bookService.delete(nonExistentId));

        verifyNoInteractions(authorService);
        verifyNoInteractions(commentRepository);
        verify(bookRepository, never()).delete(any());
        verify(bookCache, never()).remove(any());
    }

    @Test
    @Transactional
    void update_ShouldUpdateExistingBook() {
        // Arrange
        Long bookId = 1L;
        Book existingBook = new Book(bookId, "Old Title", 200, Collections.emptyList(), Collections.emptyList());
        Book updatedBook = new Book(bookId, "New Title", 300, Collections.emptyList(), Collections.emptyList());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Book result = bookService.update(bookId, updatedBook);

        // Assert
        assertEquals(result, updatedBook);
        verify(bookRepository).save(updatedBook);
        verify(cacheHolder.getBookCache()).remove(bookId);
    }


    @Test
    void update_ShouldThrowWhenBookNotFound() {
        // Arrange
        Long nonExistentId = 99L;
        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        Book book = new Book();
        // Act & Assert
        assertThrows(NotFoundException.class, () ->
                bookService.update(nonExistentId, book)
        );
        verify(bookRepository, never()).save(any());
    }

    @Test
    void update_ShouldClearCache() {
        // Arrange
        Long bookId = 2L;
        Book existing = new Book(bookId, "Existing", 150, Collections.emptyList(), Collections.emptyList());
        Book updates = new Book(bookId, "Updated", 200, Collections.emptyList(), Collections.emptyList());

        when(cacheHolder.getBookCache()).thenReturn(bookCache);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existing));
        when(bookRepository.save(updates)).thenReturn(updates);

        // Act
        bookService.update(bookId, updates);

        // Assert
        verify(cacheHolder.getBookCache()).remove(bookId);
    }

    @Test
    void getBookByFilter_WithBothValidFilters_ShouldReturnFilteredResults() {
        // Arrange
        String author = "Author";
        long commentCount = 5L;
        List<Book> expected = List.of(new Book(1L, "Book", 100, Collections.emptyList(), Collections.emptyList()));
        when(bookRepository.findByAuthorNameAndCommentCount(author, commentCount))
                .thenReturn(expected);

        // Act & Assert
        assertEquals(expected,
                bookService.getBookByFilter(Optional.of(author), Optional.of(commentCount)));
    }

    @Test
    void getBookByFilter_WithAuthorOnly_ShouldUseAuthorFilter() {
        // Arrange
        String author = "Author";
        List<Book> expected = List.of(new Book(1L, "Book", 100, Collections.emptyList(), Collections.emptyList()));
        when(bookRepository.findByAuthor(author)).thenReturn(expected);

        // Act & Assert
        assertEquals(expected,
                bookService.getBookByFilter(Optional.of(author), Optional.empty()));
    }

    @Test
    void getBookByFilter_WithCommentCountOnly_ShouldUseCommentFilter() {
        // Arrange
        long commentCount = 1L;
        List<Book> expected = List.of(new Book(1L, "Book", 100, Collections.emptyList(), List.of(new Comment())));
        when(bookRepository.findByCommentCount(commentCount)).thenReturn(expected);

        // Act & Assert
        assertEquals(expected,
                bookService.getBookByFilter(Optional.empty(), Optional.of(commentCount)));
    }
    @Test
    void getBooks_ShouldReturnAllBooksFromRepository() {
        // Arrange
        List<Book> expectedBooks = List.of(
                new Book(1L, "Book 1", 100, Collections.emptyList(), Collections.emptyList()),
                new Book(2L, "Book 2", 200, Collections.emptyList(), Collections.emptyList())
        );
        when(bookRepository.findAll()).thenReturn(expectedBooks);
        // Act
        List<Book> result = bookService.getBooks();
        // Assert
        assertEquals(expectedBooks, result);
        verify(bookRepository).findAll();
    }

    @Test
    void getBookByFilter_OnlyCommentCount_ShouldReturnFilteredBooks() {
        // Arrange
        Long minComments = 5L;
        List<Book> expectedBooks = List.of(
                new Book(1L, "Popular Book", 200, Collections.emptyList(), Collections.emptyList())
        );

        when(bookRepository.findByCommentCount(minComments)).thenReturn(expectedBooks);

        // Act
        List<Book> result = bookService.getBookByFilter(
                Optional.empty(),
                Optional.of(minComments)
        );

        // Assert
        assertEquals(expectedBooks, result);
        verify(bookRepository).findByCommentCount(minComments);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getBookByFilter_NoFilters_ShouldReturnAllBooks() {
        // Arrange
        List<Book> allBooks = List.of(
                new Book(1L, "Book One", 150, Collections.emptyList(), Collections.emptyList()),
                new Book(2L, "Book Two", 250, Collections.emptyList(), Collections.emptyList())
        );

        when(bookRepository.findAll()).thenReturn(allBooks);

        // Act
        List<Book> result = bookService.getBookByFilter(
                Optional.empty(),
                Optional.empty()
        );

        // Assert
        assertEquals(allBooks, result);
        verify(bookRepository).findAll();
        verifyNoMoreInteractions(bookRepository);
    }
}