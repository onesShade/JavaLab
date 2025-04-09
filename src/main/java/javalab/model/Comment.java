package javalab.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Model of comment")
@Table(name = "comments")
public class Comment {

    @Schema(description = "Identifier of the comment", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Book id the comment belongs to", example = "1")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"comments"})
    @JoinColumn(name = "book_id")
    private Book book;

    @Schema(description = "User id the comment belongs to", example = "1")
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"comments"})
    @JoinColumn(name = "user_id")
    private User user;

    @Schema(description = "Comment test", example = "Great book")
    @NotBlank(message = "Text cannot be null")
    @Size(min = 1, max = 255, message = "Text must be between 1 and 255 characters")
    String text;
}