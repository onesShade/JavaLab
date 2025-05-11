package javalab.service;

import java.util.List;
import java.util.Optional;

import javalab.config.CacheHolder;
import javalab.dto.CommentDto;
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
    private final CacheHolder cacheHolder;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          BookService bookService,
                          UserRepository userRepository,
                          CommentMapper commentMapper,
                          CacheHolder cacheHolder) {
        this.commentRepository = commentRepository;
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.cacheHolder = cacheHolder;
    }

    public Comment getById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(COMMENT_ID_NOT_FOUND + id));
    }

    @Transactional
    public List<CommentDto> getAllComments(Long bookId) {
        List<Comment> comments =
                bookService.getById(bookId, Resource.LoadMode.DEFAULT).getComments();
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    public Comment create(Long id, CommentDto commentDto) {
        Book book = bookService.getById(id, Resource.LoadMode.DIRECT);
        Optional<User> user = userRepository.findById(commentDto.getUserId());
        if (user.isEmpty()) {
            throw new NotFoundException(UserService.USER_ID_NOT_FOUND + commentDto.getUserId());
        }
        Comment comment = commentMapper.toEntity(commentDto);
        comment.setUser(user.get());
        comment.setBook(book);
        book.addComment(comment);
        return commentRepository.save(comment);
    }

    public void delete(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(COMMENT_ID_NOT_FOUND + commentId);
        }
        cacheHolder.getBookCache().clear();
        commentRepository.deleteById(commentId);
    }

    public Comment update(Long bookId, Long commentId, Comment comment) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException(COMMENT_ID_NOT_FOUND + commentId);
        }
        comment.setId(commentId);
        comment.setBook(bookService.getById(bookId, Resource.LoadMode.DIRECT));
        return commentRepository.save(comment);
    }
}