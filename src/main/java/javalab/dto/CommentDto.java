package javalab.dto;

public class CommentDto {
    private Long id;
    private Long userId;
    private String text;

    public void setId(Long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUserId(Long authorId) {
        this.userId = authorId;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Long getUserId() {
        return userId;
    }
}
