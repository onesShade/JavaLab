package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.exception.NotFoundException;
import javalab.model.Comment;
import javalab.model.User;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public static final String USER_ID_NOT_FOUND = "User id not found: ";
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, CommentRepository
            commentRepository) {
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public List<User> getUsers(Optional<Long> commentCountMin) {
        if (commentCountMin.isPresent()) {
            return userRepository.findByCommentCount(commentCountMin.get());
        }
        return userRepository.findAll();
    }

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(USER_ID_NOT_FOUND + id));
    }

    public User create(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_ID_NOT_FOUND + id);
        }
        User user = getUser(id);
        commentRepository.deleteAll(user.getComments());
        userRepository.deleteById(id);
    }

    public User update(Long id, User user) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_ID_NOT_FOUND + id);
        }
        user.setId(id);
        return userRepository.save(user);
    }

    public List<Comment> getUserComments(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(USER_ID_NOT_FOUND + id);
        }
        User user = getUser(id);
        return user.getComments();
    }
}