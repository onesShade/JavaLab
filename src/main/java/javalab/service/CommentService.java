package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.dto.CommentDto;
import javalab.mapper.CommentMapper;
import javalab.model.Book;
import javalab.model.Comment;
import javalab.model.User;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

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
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id"));
    }

    public List<CommentDto> getAllComments(Long bookId) {
        List<Comment> comments = bookService.getById(bookId).getComments();
        return comments.stream()
                .map(commentMapper::toDto)
                .toList();
    }

    public Comment create(Long id, CommentDto commentDto) {
        Book book = bookService.getById(id);
        Optional<User> user = userRepository.findById(commentDto.getUserId());
        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong user id");
        }
        Comment comment = commentMapper.toEntity(commentDto);
        comment.setUser(user.get());
        comment.setBook(book);
        book.addComment(comment);
        return commentRepository.save(comment);
    }

    public void delete(Long bookId, Long commentId) {
        bookService.getById(bookId).getComments().remove(getById(commentId));
        commentRepository.deleteById(commentId);
    }

    public Comment update(Long bookId, Long id, Comment comment) {
        if (!commentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong comment id");
        }
        comment.setId(id);
        comment.setBook(bookService.getById(bookId));
        return commentRepository.save(comment);
    }
}