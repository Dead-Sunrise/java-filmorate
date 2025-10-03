package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("/users GET Запрос на получение списка пользователей.");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Optional<User> getUserById(@PathVariable Long id) {
        log.info("/users/{id} GET Запрос на получение данных конкретного пользователя по id");
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getAllFriends(@PathVariable Long id) {
        log.info("/users/{id}/friends GET Запрос на получение списка друзей пользователя");
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    public Collection<User> getCommonFriends(@PathVariable Long id,
                                             @PathVariable Long friendId) {
        log.info("/users/{id}/friends/common/{friendId} GET Запрос на получение списка общих друзей двух пользователей");
        return userService.getCommonFriends(id, friendId);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("/users POST Запрос на создание нового пользователя");
        return userService.createUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("/users PUT Запрос на обновление пользователя.");
        return userService.updateUser(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id,
                          @PathVariable Long friendId) {
        log.info("users/{id}/friends/{friendId} PUT Запрос на добавление в друзья");
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        log.info("/users/{id}/friends/{friendId} DELETE Запрос на удаление пользователя из друзей");
        userService.removeFriend(id, friendId);
    }
}
