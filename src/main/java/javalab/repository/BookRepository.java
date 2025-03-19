package javalab.repository;

import java.util.List;
import javalab.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE SIZE(b.comments) >= :commentCount")
    List<Book> findByCommentCount(@Param("commentCount") Long commentCount);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.name = :authorName")
    List<Book> findByAuthor(@Param("authorName") String authorName);

    @Query("SELECT b FROM Book b JOIN b.authors a WHERE a.name = :authorName "
            + "AND SIZE(b.comments) >= :commentCount")
    List<Book> findByAuthorNameAndCommentCount(
            @Param("authorName") String authorName,
            @Param("commentCount") Long commentCount
    );
}