package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @GetMapping
    public Collection<User> findAll() {
        log.info("/users GET Запрос на получение списка пользователей.");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("/users POST Запрос на создание нового пользователя");
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
            if (user.getBirthday().isAfter(LocalDate.now())) {
                log.error("Дата рождения в будущем: {}", user.getBirthday());
                throw new ValidationException("Дата рождения не может быть в будущем.");
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

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("/users PUT Запрос на обновление пользователя.");
        if (newUser.getId() == null) {
            log.error("Id не указан.");
            throw new ValidationException("Id должен быть указан.");
        }
        if (users.containsKey(newUser.getId())) {
            for (User user1 : users.values()) {
                if (newUser.getEmail() != null && newUser.getEmail().equals(user1.getEmail())) {
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
        throw new ValidationException("Пользователь с id = " + newUser.getId() + " не найден");
    }
}
