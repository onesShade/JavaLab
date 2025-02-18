package javalab.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javalab.model.User;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final List<User> users;

    public UserRepository() {
        users = new ArrayList<>();
        users.addAll(List.of(
                new User(1, "Joseph"),
                new User(2, "Robert"),
                new User(3, "Mary")));
    }

    public List<User> getUsers() {
        return users;
    }

    public Optional<User> getUser(int id) {
        return users.stream().filter(u -> u.getId() == id).findFirst();
    }
}