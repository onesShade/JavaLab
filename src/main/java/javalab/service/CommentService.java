package javalab.service;

import java.util.List;
import javalab.model.Book;
import javalab.model.Comment;
import javalab.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final BookService bookService;

    @Autowired
    public CommentService(CommentRepository commentRepository, BookService bookService) {
        this.commentRepository = commentRepository;
        this.bookService = bookService;
    }

    public Comment getById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong book id"));
    }

    public List<Comment> getAllComments(Long bookId) {
        return bookService.getById(bookId).getComments();
    }

    public Comment create(Long id, Comment comment) {
        Book book = bookService.getById(id);
        book.addComment(comment);
        comment.setBook(book);
        return commentRepository.save(comment);
    }

    public void delete(Long bookId, Long commentId) {
        bookService.getById(bookId).getComments().remove(getById(commentId));
        commentRepository.deleteById(commentId);
    }

    public Comment update(Long id, Comment comment) {
        if (!commentRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong comment id");
        }
        comment.setId(id);
        return commentRepository.save(comment);
    }
}