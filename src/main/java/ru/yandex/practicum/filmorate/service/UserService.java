package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.users.UserDbStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDbStorage userDbStorage;


    public void addFriend(Long userId, Long friendId) {
        userDbStorage.addFriendship(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userDbStorage.deleteFriendship(userId, friendId);
    }

    public Collection<User> getAllFriends(Long userId) {
        return userDbStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        return userDbStorage.getCommonFriends(userId, friendId);
    }

    public void removeAllUsers() {
        userDbStorage.deleteAllUsers();
    }

    public Collection<User> getAllUsers() {
        return userDbStorage.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userDbStorage.getUserById(id);
    }

    public User createUser(User user) {
        return userDbStorage.create(user);
    }

    public User updateUser(User newUser) {
        return userDbStorage.update(newUser);
    }
}
