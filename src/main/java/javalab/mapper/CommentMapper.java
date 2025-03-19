package javalab.mapper;

import javalab.dto.CommentDto;
import javalab.model.Comment;
import javalab.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    private final UserService userService;

    public CommentMapper(UserService userService) {
        this.userService = userService;
    }

    public CommentDto toDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setUserId(comment.getUser().getId());
        return commentDto;
    }

    public Comment toEntity(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setUser(userService.getUser(commentDto.getUserId()));
        return comment;
    }
}
