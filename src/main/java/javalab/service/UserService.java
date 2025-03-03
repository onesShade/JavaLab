package javalab.service;

import java.util.List;
import javalab.model.User;
import javalab.repository.UserRepository;
import javalab.utility.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

    public User getUser(String id) {
        int verifiedId = Tools.tryParseInt(id);

        if (verifiedId == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong id notation");
        }
        return userRepository.getUser(verifiedId).orElseThrow(()
                -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}