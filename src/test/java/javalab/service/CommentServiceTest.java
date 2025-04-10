package javalab.service;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javalab.dto.CommentDto;
import javalab.exception.NotFoundException;
import javalab.mapper.CommentMapper;
import javalab.model.Author;
import javalab.model.Book;
import javalab.model.Comment;
import javalab.model.User;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import javalab.utility.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentMapper commentMapper;
    @Mock
    private BookService bookService;
    @InjectMocks
    private CommentService commentService;

    @Test
    void getById_ShouldReturnComment_WhenExists() {
        Long commentId = 1L;
        User user = mock(User.class);
        Book book = mock(Book.class);
        Comment expectedComment = new Comment(1L, book, user, "Text");
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.of(expectedComment));
        Comment actualComment = commentService.getById(commentId);
        assertNotNull(actualComment);
        assertEquals(expectedComment, actualComment);
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getById_ShouldThhrowNotFoundException_WhenNotExists() {
        Long commentId = 1L;
        when(commentRepository.findById(commentId))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> commentService.getById(commentId));
    }

    @Test
    void getByIdShouldThrowNotFoundExceptionWhenNotExists() {
        Long nonExistentId = 999L;
        when(commentRepository.findById(nonExistentId))
                .thenReturn(empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> commentService.getById(nonExistentId)
        );

        assertTrue(exception.getMessage().contains(String.valueOf(nonExistentId)));
        verify(commentRepository).findById(nonExistentId);
    }

    @Test
    void getAllComments_ShouldReturnEmptyList_WhenNoCommentsExist() {
        List<Author> authors = Collections.emptyList();
        List<Comment> comments = Collections.emptyList();
        Long bookId = 1L;
        Book book = new Book(bookId, "Book A", 16, authors, comments);

        when(bookService.getById(bookId, Resource.LoadMode.DEFAULT)).thenReturn(book);
        List<CommentDto> result = commentService.getAllComments(bookId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllComments_ShouldReturnMappedComments_WhenCommentsExist() {
        Long bookId = 1L;
        List<Author> authors = Collections.emptyList();
        User user = mock(User.class);

        Book book = new Book(bookId, "Book A", 16 , authors, Collections.emptyList());

        CommentDto dtoA = new CommentDto(1L, 1L, "text1");
        CommentDto dtoB = new CommentDto(2L, 1L, "text2");
        Comment comA = new Comment(1L, book, user, "text1");
        Comment comB = new Comment(2L, book, user, "text2");
        book.setComments(Arrays.asList(comA, comB));

        List<CommentDto> dto = Arrays.asList(dtoA, dtoB);

        when(commentMapper.toDto(comA)).thenReturn(dtoA);
        when(commentMapper.toDto(comB)).thenReturn(dtoB);
        when(bookService.getById(bookId, Resource.LoadMode.DEFAULT)).thenReturn(book);

        List<CommentDto> result = commentService.getAllComments(bookId);

        assertEquals(dto, result);
    }

    @Test
    void create_ShouldCreateComment_WhenExists() {
        Long id = 1L;
        CommentDto dtoA = new CommentDto(1L, 1L, "text1");
        Book book = new Book(id, "Book A", 16, new ArrayList<>(), new ArrayList<>());
        User user = new User(id, "Jake", new ArrayList<>());

        Comment expectedComment = new Comment(id, book, user, "text1");

        when(bookService.getById(id, Resource.LoadMode.DIRECT)).thenReturn(book);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(expectedComment);
        when(commentMapper.toEntity(any(CommentDto.class))).thenReturn(expectedComment);

        Comment result = commentService.create(id, dtoA);

        assertNotNull(result);
        assertEquals(1, book.getComments().size());

        Comment savedComment = book.getComments().getFirst();
        assertEquals(expectedComment.getText(), savedComment.getText());
        assertEquals(expectedComment.getUser(), savedComment.getUser());
        assertEquals(expectedComment.getBook(), savedComment.getBook());
    }

    @Test
    void create_shouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long id = 1L;
        CommentDto dtoA = new CommentDto(1L, 1L, "text1");
        Book book = new Book(id, "Book A", 16, new ArrayList<>(), new ArrayList<>());

        when(bookService.getById(id, Resource.LoadMode.DIRECT)).thenReturn(book);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.create(id, dtoA));
    }

    @Test
    void delete_ShouldRemoveCommentFromBookAndDeleteIt() {

        Long commentId = 1L;
        Book book = new Book();
        book.setComments(new ArrayList<>());
        Comment comment = new Comment();
        comment.setId(commentId);
        book.getComments().add(comment);
        Long bookId = 1L;

        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(bookService.getById(bookId, Resource.LoadMode.DIRECT)).thenReturn(book);

        commentService.delete(bookId, commentId);

        assertFalse(book.getComments().contains(comment));
        verify(commentRepository).deleteById(commentId);
        verify(commentRepository).existsById(commentId);
        verify(bookService).getById(bookId, Resource.LoadMode.DIRECT);
    }

    @Test
    void delete_ShouldThrowNotFoundException_WhenCommentDoesNotExist() {
        Long bookId = 1L;
        Long commentId = 999L;

        when(commentRepository.existsById(commentId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.delete(bookId, commentId));

        assertEquals(CommentService.COMMENT_ID_NOT_FOUND + commentId, exception.getMessage());
        verify(commentRepository).existsById(commentId);
        verifyNoInteractions(bookService);
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void update_ShouldUpdateCommentSuccessfully() {
        Long bookId = 1L;
        Long commentId = 1L;
        Book book = new Book(bookId, "Sample Book", 2023, new ArrayList<>(), new ArrayList<>());
        Comment updatedComment = new Comment(commentId, null, null, "New text");

        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(bookService.getById(bookId, Resource.LoadMode.DIRECT)).thenReturn(book);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            c.setId(commentId);
            return c;
        });

        Comment result = commentService.update(bookId, commentId, updatedComment);

        assertNotNull(result);
        assertEquals(result, updatedComment);
        assertEquals(book, result.getBook());

        verify(commentRepository).existsById(commentId);
        verify(bookService).getById(bookId, Resource.LoadMode.DIRECT);
        verify(commentRepository).save(updatedComment);
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenCommentDoesNotExist() {
        // Arrange
        Long bookId = 1L;
        Long commentId = 999L;
        Comment comment = new Comment();

        when(commentRepository.existsById(commentId)).thenReturn(false);

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> commentService.update(bookId, commentId, comment));

        assertEquals(CommentService.COMMENT_ID_NOT_FOUND + commentId, exception.getMessage());
        verify(commentRepository).existsById(commentId);
        verifyNoInteractions(bookService);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void update_ShouldSetBookAndIdBeforeSaving() {
        // Arrange
        Long bookId = 1L;
        Long commentId = 1L;
        Book book = new Book();
        Comment comment = new Comment();
        comment.setText("Updated text");

        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(bookService.getById(bookId, Resource.LoadMode.DIRECT)).thenReturn(book);
        when(commentRepository.save(comment)).thenReturn(comment);

        Comment result = commentService.update(bookId, commentId, comment);

        assertNotNull(result);
        assertEquals(commentId, comment.getId());
        assertEquals(book, comment.getBook());
        assertEquals("Updated text", comment.getText());
        verify(commentRepository).save(comment);
    }

    @Test
    void update_ShouldReturnSavedComment() {
        Long bookId = 1L;
        Long commentId = 1L;
        Book book = new Book();
        Comment inputComment = new Comment();
        Comment savedComment = new Comment(commentId, book, new User(), "Saved text");

        when(commentRepository.existsById(commentId)).thenReturn(true);
        when(commentRepository.save(inputComment)).thenReturn(savedComment);

        Comment result = commentService.update(bookId, commentId, inputComment);

        assertSame(savedComment, result);
        assertEquals(commentId, result.getId());
        assertEquals(book, result.getBook());
    }

}