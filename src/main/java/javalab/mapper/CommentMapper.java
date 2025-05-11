package javalab.mapper;

import javalab.dto.CommentDto;
import javalab.model.Comment;
import javalab.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {
    private final UserService userService;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    @Autowired
    public CommentMapper(UserService userService, UserMapper userMapper, BookMapper bookMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
    }

    public CommentDto toDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setUserId(comment.getUser().getId());
        commentDto.setUser(userMapper.toDto(comment.getUser()));
        commentDto.setBook(bookMapper.toDto(comment.getBook()));
        return commentDto;
    }

    public Comment toEntity(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setUser(userService.getUser(commentDto.getUserId()));
        return comment;
    }
}
