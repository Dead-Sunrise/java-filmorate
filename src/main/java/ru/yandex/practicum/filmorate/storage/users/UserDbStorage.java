package ru.yandex.practicum.filmorate.storage.users;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;

@Repository
@AllArgsConstructor
@Qualifier("db")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private static final String FIND_ALL_USERS = "SELECT * FROM users";
    private static final String FIND_USER_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String FIND_USER_FRIENDS = """
            SELECT u.*
            FROM users u
            JOIN friends f ON u.id = f.friend_id
            WHERE f.user_id = ?""";
    private static final String FIND_COMMON_FRIENDS = """
            SELECT u.*
            FROM users u
            JOIN friends f1 ON u.id = f1.friend_id
            JOIN friends f2 ON u.id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ?""";
    private static final String ADD_USER = """
            INSERT INTO users(email, login, name, birthday)
            VALUES(?, ?, ?, ?)
            """;
    private static final String ADD_FRIENDSHIP = "INSERT INTO friends(user_id, friend_id) VALUES(?, ?)";
    private static final String UPDATE_USER = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String DELETE_FRIENDSHIP = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_USER_BY_ID = "DELETE FROM users WHERE id = ?";
    private static final String DELETE_ALL_USERS = "DELETE FROM users";
    private static final String FIND_FRIEND_IDS_BY_USER_ID = """
            SELECT friend_id FROM friends
            WHERE user_id = ?""";
    private static final String CHECK_FRIENDSHIP_EXISTS = """
            SELECT COUNT(*) FROM friends
            WHERE user_id = ? AND friend_id = ?""";
    private static final String ALL_USER_LIKES = "SELECT user_id, film_id FROM film_likes";

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbcTemplate.query(FIND_ALL_USERS, userRowMapper);
        users.forEach(this::loadUserFriends);
        return users;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(FIND_USER_BY_ID, userRowMapper, id);
            if (user != null) {
                loadUserFriends(user);
            }
            return Optional.ofNullable(user);
        } catch (
                EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Collection<User> getFriends(Long id) {
        if (!userExists(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }
        return jdbcTemplate.query(FIND_USER_FRIENDS, userRowMapper, id);
    }

    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        return jdbcTemplate.query(FIND_COMMON_FRIENDS, userRowMapper, userId, friendId);
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(ADD_USER, new String[]{"id"});
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getLogin());
            preparedStatement.setString(3, user.getName());
            if (user.getBirthday() != null) {
                preparedStatement.setDate(4, Date.valueOf(user.getBirthday()));
            } else {
                preparedStatement.setNull(4, Types.DATE);
            }
            return preparedStatement;
        }, keyHolder);
        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return user;
    }

    public void addFriendship(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }
        if (isFriendshipExists(userId, friendId)) {
            throw new ValidationException("Дружба уже существует");
        }
        jdbcTemplate.update(ADD_FRIENDSHIP, userId, friendId);
    }

    @Override
    public User update(User user) {
        if (!userExists(user.getId())) {
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        jdbcTemplate.update(UPDATE_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        return user;
    }

    @Override
    public Map<Long, List<Long>> getAllUserLikes() {
        Map<Long, List<Long>> userLikesMap = new HashMap<>();
        jdbcTemplate.query(ALL_USER_LIKES, rs -> {
            Long userId = rs.getLong("user_id");
            Long filmId = rs.getLong("film_id");
            userLikesMap.computeIfAbsent(userId, key -> new ArrayList<>()).add(filmId);
        });
        return userLikesMap;
    }

    public void deleteFriendship(Long userId, Long friendId) {
        if (!userExists(userId) || !userExists(friendId)) {
            throw new NotFoundException("Один из пользователей не найден");
        }
        jdbcTemplate.update(DELETE_FRIENDSHIP, userId, friendId);
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userExists(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? OR friend_id = ?", id, id);
        jdbcTemplate.update("DELETE FROM film_likes WHERE user_id = ?", id);
        jdbcTemplate.update(DELETE_USER_BY_ID, id);
    }

    public void deleteAllUsers() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update(DELETE_ALL_USERS);
    }

    private void loadUserFriends(User user) {
        List<Long> friendsId = jdbcTemplate.query(
                FIND_FRIEND_IDS_BY_USER_ID,
                (rs, rowNum) -> rs.getLong("friend_id"), user.getId());
        user.setFriends(new HashSet<>(friendsId));
    }

    private boolean userExists(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        return count > 0;
    }

    private boolean isFriendshipExists(Long userId, Long friendId) {
        Integer count = jdbcTemplate.queryForObject(CHECK_FRIENDSHIP_EXISTS, Integer.class, userId, friendId);
        return count > 0;
    }
}