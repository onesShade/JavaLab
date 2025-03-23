package javalab.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "DTO model of the comment")
public class CommentDto {
    private Long id;

    @NotNull(message = "User id should be specified")
    private Long userId;

    @NotBlank(message = "Text cannot be null")
    @Size(min = 1, max = 255, message = "Text must be between 1 and 255 characters")
    private String text;
}
