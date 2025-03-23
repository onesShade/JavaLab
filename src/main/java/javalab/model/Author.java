package javalab.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Schema(description = "Model of author")
@Table(name = "authors")
public class Author {

    @NotBlank(message = "Name shouldn't be empty")
    @Size(max = 100, message = "Max 100 characters for name")
    @Pattern(regexp = "^[a-zA-Z\\s]+$",
            message = "Name shall not contain numbers or special symbols")
    @Schema(description = "Name of the author", example = "John")
    private String name;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identifier of the author", example = "1")
    private Long id;

    @ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"authors", "comments"})
    @Schema(description = "List of the books of the author")
    private List<Book> books = new ArrayList<>();

    protected Author() {}

    public Author(String name) {
        this.name = name;
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void removeBook(Book book) {
        books.remove(book);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Author that = (Author) o;
        return  this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}