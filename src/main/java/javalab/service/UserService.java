package javalab.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;
import javalab.exception.NotFoundException;
import javalab.model.User;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    public static final String USER_ID_NOT_FOUND = "User id not found: ";
    private final CommentRepository commentRepository;

    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public List<User> getUsers(Optional<Long> commentCountMin) {
        if (commentCountMin.isPresent()) {
            Query query = entityManager.createNativeQuery("SELECT u.* FROM users u "
                    + "WHERE (SELECT COUNT(c.id) FROM comments c WHERE c.user_id = u.id) "
                    + ">= :commentCountMin", User.class);
            query.setParameter("commentCountMin", commentCountMin.get().intValue());
            return query.getResultList();
        }
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(USER_ID_NOT_FOUND));
    }

    @Transactional
    public User create(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_ID_NOT_FOUND);
        }
        User user = getUser(id);
        commentRepository.deleteAll(user.getComments());
        userRepository.deleteById(id);
    }

    @Transactional
    public User update(Long id, User user) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_ID_NOT_FOUND);
        }
        user.setId(id);
        return userRepository.save(user);
    }
}