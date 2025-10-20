package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.users.UserStorage;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FeedService feedService;

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriendship(userId, friendId);
        feedService.addEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.deleteFriendship(userId, friendId);
        feedService.addEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    public List<User> getAllFriends(Long userId) {
        return userStorage.getFriends(userId).stream().toList();
    }

    public List<User> getCommonFriends(Long userId, Long friendId) {
        return userStorage.getCommonFriends(userId, friendId).stream().toList();
    }

    public Map<Long, List<Long>> getAllUserLikes() {
        return userStorage.getAllUserLikes();
    }

    public void removeUserById(Long id) {
        userStorage.deleteUserById(id);
    }

    public void removeAllUsers() {
        userStorage.deleteAllUsers();
    }

    public List<User> getAllUsers() {
        return userStorage.findAll().stream().toList();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User newUser) {
        return userStorage.update(newUser);
    }
}
