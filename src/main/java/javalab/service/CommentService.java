package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.dto.CommentDto;
import javalab.exception.BadRequestException;
import javalab.exception.NotFoundException;
import javalab.mapper.CommentMapper;
import javalab.model.Book;
import javalab.model.Comment;
import javalab.model.User;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import javalab.utility.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    public static final String COMMENT_ID_NOT_FOUND = "Comment id not found: ";

    private final CommentRepository commentRepository;
    private final BookService bookService;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Autowired
    public CommentService(CommentRepository commentRepository, BookService bookService,
                          UserRepository userRepository, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
    }

    public Comment getById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(COMMENT_ID_NOT_FOUND + id));
    }

    public List<CommentDto> getAllComments(Long bookId) {
        List<Comment> comments =
                bookService.getById(bookId, Resource.LoadMode.DEFAULT).getComments();
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    @Transactional
    public Comment create(Long id, CommentDto commentDto) {
        Book book = bookService.getById(id, Resource.LoadMode.DIRECT);
        Optional<User> user = userRepository.findById(commentDto.getUserId());
        if (user.isEmpty()) {
            throw new BadRequestException(UserService.USER_ID_NOT_FOUND + commentDto.getUserId());
        }
        Comment comment = commentMapper.toEntity(commentDto);
        comment.setUser(user.get());
        comment.setBook(book);
        book.addComment(comment);
        return commentRepository.save(comment);
    }

    @Transactional
    public void delete(Long bookId, Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(COMMENT_ID_NOT_FOUND + commentId);
        }
        bookService.getById(bookId, Resource.LoadMode.DIRECT)
                .getComments().remove(getById(commentId));
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public Comment update(Long bookId, Long commentId, Comment comment) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(COMMENT_ID_NOT_FOUND + commentId);
        }
        comment.setId(commentId);
        comment.setBook(bookService.getById(bookId, Resource.LoadMode.DIRECT));
        return commentRepository.save(comment);
    }
}