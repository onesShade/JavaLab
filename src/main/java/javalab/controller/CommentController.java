package javalab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import javalab.dto.CommentDto;
import javalab.model.Comment;
import javalab.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books/{id}/comments")
@Validated
@Tag(name = "Comment controller", description = "Allows to add, get, delete, or update comments. "
        + "Every comment is linked to a book")
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    @Operation(
            summary = "Get all comments for a book",
            description = "Retrieves a list of all comments of a given existing book."
    )
    public List<CommentDto> getByBook(@PathVariable Long id) {
        return commentService.getAllComments(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new comment",
            description = "Creates a new comment for an existing book. "
                        + "Shall contain text and user id"
    )
    public Comment create(@PathVariable Long id,
                          @Valid @RequestBody CommentDto comment) {
        return commentService.create(id, comment);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new comment",
            description = "Creates comments for an existing book. "
                    + "Shall contain texts and user ids"
    )
    public List<Comment> createInBulk(@PathVariable Long id,
                                @Valid @RequestBody List<CommentDto> comments) {
        return comments.stream().map(commentDto
                -> commentService.create(id, commentDto)).toList();
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a com for book ID by comment ID")
    public void delete(@PathVariable Long id, @PathVariable Long commentId) {
        commentService.delete(commentId);
    }

    @PutMapping("/{commentId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Update a comment of book ID by comment ID",
            description = "Updates an existing comment. Returns the updated comment."
    )
    public Comment update(@PathVariable Long id,
                          @PathVariable Long commentId,
                          @RequestBody Comment comment) {
        return commentService.update(id, commentId, comment);
    }
}