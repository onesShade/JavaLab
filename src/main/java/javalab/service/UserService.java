package javalab.service;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import javalab.model.User;
import javalab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @PersistenceContext
    private EntityManager entityManager;

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong user id"));
    }

    public User create(User user) {
        return userRepository.save(user);
    }
}