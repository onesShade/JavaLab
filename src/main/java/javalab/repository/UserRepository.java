package javalab.repository;

import java.util.List;
import javalab.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT u.* FROM users u "
            + "WHERE (SELECT COUNT(c.id) FROM comments c WHERE c.user_id = u.id) "
            + ">= :commentCountMin", nativeQuery = true)
    List<User> findByCommentCount(@Param("commentCount") Long commentCount);
}