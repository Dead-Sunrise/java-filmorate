package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        try {
            for (User user1 : users.values()) {
                if (user.getEmail() != null && user.getEmail().equals(user1.getEmail())) {
                    log.error("Пользователь с таким email уже существует : {}", user.getEmail());
                    throw new ValidationException("Этот email уже используется.");
                }
            }
            if (user.getName() == null || user.getName().isBlank()) {
                log.debug("Замена пустого имени пользователя на логин: {}", user.getLogin());
                user.setName(user.getLogin());
            }
            user.setId(getNextId());
            users.put(user.getId(), user);
            log.info("Пользователь успешно добавлен: {}", user.getEmail());
            return user;
        } catch (RuntimeException e) {
            log.error("Ошибка при создании пользователя");
            throw e;
        }
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            log.error("Id не указан.");
            throw new ValidationException("Id должен быть указан.");
        }
        if (users.containsKey(newUser.getId())) {
            for (User user1 : users.values()) {
                if (newUser.getEmail() != null && newUser.getEmail().equals(user1.getEmail()) && !newUser.getId().equals(user1.getId())) {
                    log.error("Пользователь с таким email уже существует {}", newUser.getEmail());
                    throw new ValidationException("Этот email уже используется.");
                }
            }
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null) {
                log.debug("Указано новое значение email, изменение значения: {}", newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getLogin() != null) {
                log.debug("Указано новое значение login, изменение значения: {}", newUser.getLogin());
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null) {
                log.debug("Указано новое значение name, изменение значения: {}", newUser.getName());
                oldUser.setName(newUser.getName());
            }
            if (newUser.getBirthday() != null) {
                log.debug("Указано новое значение birthday, изменение значения: {}", newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.info("Данные пользователя успешно обновлены.");
            return oldUser;
        }
        log.error("Указан несуществующий id пользователя {}:", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public User getUserById(Long id) {
        if (id == null || !users.containsKey(id)) {
            log.error("Указан несуществующий id пользователя :{}", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public void deleteUserById(Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь с таким id не найден.");
        }
        users.remove(id);
    }
}