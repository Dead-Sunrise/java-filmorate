package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friendId);
        userStorage.update(user);
        friend.getFriends().add(userId);
        userStorage.update(friend);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        //if (!user.getFriends().contains(friendId)) {
          //  throw new NotFoundException("Пользователя нет в списке друзей");
        //}
        user.getFriends().remove(friendId);
        userStorage.update(user);
        friend.getFriends().remove(userId);
        userStorage.update(friend);
    }

    public Collection<User> getAllFriends(Long userId) {
        return userStorage.getUserById(userId).getFriends()
                .stream()
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId){
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        return user.getFriends()
                .stream()
                .filter(friend.getFriends()::contains)
                .map(userStorage::getUserById)
                .collect(Collectors.toList());
    }

    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User newUser) {
        return userStorage.update(newUser);
    }
}
