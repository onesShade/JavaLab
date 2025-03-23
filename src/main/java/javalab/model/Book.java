package javalab.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "Model of book")
@Table(name = "books")
public class Book {

    @Schema(description = "Identifier of the book", example = "1")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(description = "Title of a book", example = "Blood, sweat and pixels")
    @NotBlank(message = "Title shouldn't be empty")
    @Size(max = 128, message = "Max 128 characters for title")
    @Pattern(regexp = "^[a-zA-Z'\\d\\s]+$",
            message = "Title shall not contain special symbols")
    private String title;

    @Schema(description = "Number of pages of a book", example = "15")
    private int pages;

    @Schema(description = "List of authors of the book")
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"books"})
    @JoinTable(
            name = "book_author",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors = new ArrayList<>();

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"book", "user"})
    private List<Comment> comments = new ArrayList<>();

    public Book(String title, final List<Author> authors, int pages) {
        this.title = title;
        this.authors = authors;
        this.pages = pages;
    }


    public void addAuthor(Author author) {
        authors.add(author);
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Book that = (Book) o;
        return  this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}