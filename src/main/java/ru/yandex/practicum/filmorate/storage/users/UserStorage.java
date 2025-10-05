package ru.yandex.practicum.filmorate.storage.users;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    Optional<User> getUserById(Long id);

    Collection<User> getFriends(Long id);

    Collection<User> getCommonFriends(Long userId, Long friendId);

    User create(User user);

    void addFriendship(Long userId, Long friendId);

    User update(User newUser);

    void deleteAllUsers();

    void deleteFriendship(Long userId, Long friendId);

    void deleteUserById(Long id);
}
