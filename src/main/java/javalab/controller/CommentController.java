package javalab.controller;

import java.util.List;
import javalab.dto.CommentDto;
import javalab.model.Comment;
import javalab.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books/{id}/comments")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDto> getByBook(@PathVariable Long id) {
        return commentService.getAllComments(id);
    }

    @PostMapping
    public Comment create(@PathVariable Long id,
                          @RequestBody CommentDto comment) {
        return commentService.create(id, comment);
    }

    @DeleteMapping("/{commentId}")
    public void delete(@PathVariable Long id, @PathVariable Long commentId) {
        commentService.delete(id, commentId);
    }

    @PutMapping("/{commentId}")
    public Comment update(@PathVariable Long id,
                          @PathVariable Long commentId,
                          @RequestBody Comment comment) {
        return commentService.update(id, commentId, comment);
    }
}