package javalab.service;

import java.util.List;
import java.util.Optional;
import javalab.model.User;
import javalab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.getUsers();
    }

    public Optional<User> getUser(String id) {
        int verifiedId = Tools.tryParseInt(id);

        if (verifiedId == -1) {
            return Optional.empty();
        }
        return userRepository.getUser(verifiedId);
    }
}