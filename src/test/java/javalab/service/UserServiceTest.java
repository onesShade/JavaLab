package javalab.service;

import javalab.exception.NotFoundException;
import javalab.model.Comment;
import javalab.model.User;
import javalab.repository.CommentRepository;
import javalab.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void getUsers_shouldReturnUsers() {
        List<User> users = List.of(new User("A"), new User("B"));
        when(userRepository.findAll()).thenReturn(users);
        List<User> res = userService.getUsers(Optional.empty());

        assertEquals(users, res);
    }

    @Test
    void getUsers_withCommentCountMin_shouldReturnUsers() {
        Long commentCount = 1L;
        List<User> users = List.of(new User("A"), new User("B"));
        when(userRepository.findByCommentCount(commentCount)).thenReturn(users);

        List<User> res = userService.getUsers(Optional.of(commentCount));

        assertEquals(users, res);
    }

    @Test
    void getUser_shouldReturnUser() {
        User user = new User("A");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        User res = userService.getUser(1L);

        assertEquals(user, res);
    }

    @Test
    void getUser_withNonExistentUser_shouldThrowNotFoundException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getUser(1L));
    }

    @Test
    void create_ShouldSaveAndReturnUser() {
        User inputUser = new User("john_doe");
        User savedUser = new User(1L, "john_doe", new ArrayList<>());
        when(userRepository.save(inputUser)).thenReturn(savedUser);

        User result = userService.create(inputUser);

        assertNotNull(result.getId());
        assertEquals(savedUser, result);
        assertEquals("john_doe", result.getName());
        verify(userRepository).save(inputUser);
    }

    @Test
    void create_ShouldHandleDatabaseErrors() {
        User validUser = new User("valid_user");
        when(userRepository.save(validUser))
                .thenThrow(new InternalError("Database error") {});

        assertThrows(InternalError.class,
                () -> userService.create(validUser));
    }

    @Test
    void create_ShouldInitializeComments() {
        User inputUser = new User("new_user");
        User savedUser = new User(1L, "new_user", new ArrayList<>());

        when(userRepository.save(inputUser)).thenReturn(savedUser);

        User result = userService.create(inputUser);

        assertNotNull(result.getComments());
        assertTrue(result.getComments().isEmpty());
    }

    @Test
    void delete_ShouldRemoveUserAndComments() {
        Long userId = 1L;
        User user = new User(userId, "testuser", List.of(new Comment(), new Comment()));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.delete(userId);

        verify(commentRepository).deleteAll(user.getComments());
        verify(userRepository).deleteById(userId);
    }

    @Test
    void delete_ShouldThrowWhenUserNotFound() {
        Long nonExistentId = 999L;
        when(userRepository.existsById(nonExistentId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.delete(nonExistentId));

        assertEquals("User id not found: 999", exception.getMessage());
        verifyNoInteractions(commentRepository);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void update_ShouldUpdateUser() {
        Long userId = 1L;
        User updatedUser = new User(userId, "newname", List.of(new Comment(), new Comment()));
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.update(userId, updatedUser);

        assertEquals(result.getId(), userId);
        assertEquals("newname", result.getName());
        verify(userRepository).save(argThat(user ->
                user.getId().equals(userId) &&
                        user.getName().equals("newname")
        ));
    }

    @Test
    void update_ShouldThrowWhenUserNotFound() {
        Long userId = 1L;
        User user = new User(userId, "test", Collections.emptyList());

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.update(userId, user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_ShouldRollbackOnException() {
        Long userId = 1L;
        User user = new User(userId, "test", Collections.emptyList());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> userService.update(userId, user));
    }
}