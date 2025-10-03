package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.users.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    InMemoryUserStorage userController;

    @BeforeEach
    void newController() {
        userController = new InMemoryUserStorage();
    }

    @Test
    void createCorrectUserTest() { //тест создания пользователя с корректными вводными
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("Name");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        User createdUser = userController.create(user);
        assertEquals(1, userController.findAll().size());
        assertEquals(1, createdUser.getId());
    }

    @Test
    void createUserWithEmptyNameTest() { //тест создания пользователя с пустым именем(должно быть заменено на логин)
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        User createdUser = userController.create(user);
        assertEquals(user.getLogin(), createdUser.getName());
    }

    @Test
    void createUserWithExistingEmail() { //тест создания пользователя с существующим email
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("Name");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        User createdUser = userController.create(user);
        User user1 = new User();
        user1.setEmail("email@email.ru");
        user1.setName("Name1");
        user1.setLogin("Login1");
        user1.setBirthday(LocalDate.parse("2000-01-01"));
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.create(user1));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }

    @Test
    void correctUpdateUserTest() { // тест изменения пользователя с корректными вводными
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("Name");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        userController.create(user);
        User newUser = new User();
        newUser.setId(1L);
        newUser.setEmail("newemail@email.ru");
        newUser.setName("New name");
        newUser.setLogin("New_login");
        newUser.setBirthday(LocalDate.parse("2001-01-01"));
        User updatedUser = userController.update(newUser);
        assertEquals(1, userController.findAll().size());
        assertTrue(updatedUser.getEmail().equals("newemail@email.ru"));
    }

    @Test
    void updateUserWithInvalidId() { // тест изменения пользователя с указанием пустого и несуществующего id
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("Name");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        userController.create(user);
        User newUser = new User();
        newUser.setEmail("newemail@email.ru");
        newUser.setName("New name");
        newUser.setLogin("New_login");
        newUser.setBirthday(LocalDate.parse("2001-01-01"));
        ValidationException exception1 = assertThrows(ValidationException.class, () -> userController.update(newUser));
        assertEquals("Id должен быть указан.", exception1.getMessage());
        User newUser1 = new User();
        newUser1.setId(4L);
        newUser1.setEmail("newemail@email.ru");
        newUser1.setName("New name");
        newUser1.setLogin("New_login");
        newUser1.setBirthday(LocalDate.parse("2001-01-01"));
        NotFoundException exception2 = assertThrows(NotFoundException.class, () -> userController.update(newUser1));
        assertEquals("Пользователь с id = " + newUser1.getId() + " не найден", exception2.getMessage());
    }

    @Test
    void updateUserWithEmptyValues() { // тест изменения с указанием пустых email, login, name, birthday(должны быть заменены на начальные значения)
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("Name");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        userController.create(user);
        User newUser = new User();
        newUser.setId(1L);
        newUser.setEmail(null);
        newUser.setName(null);
        newUser.setLogin(null);
        User updateUser = userController.update(newUser);
        assertTrue(updateUser.getEmail().equals("email@email.ru"));
        assertTrue(updateUser.getName().equals("Name"));
        assertTrue(updateUser.getLogin().equals("Login"));
    }

    @Test
    void updateUserWithExistingEmail() { //тест обновления пользователя с существующим email
        User user = new User();
        user.setEmail("email@email.ru");
        user.setName("Name");
        user.setLogin("Login");
        user.setBirthday(LocalDate.parse("2000-01-01"));
        userController.create(user);
        User user1 = new User();
        user1.setEmail("email1@email.ru");
        user1.setName("Name1");
        user1.setLogin("Login1");
        user1.setBirthday(LocalDate.parse("2001-01-01"));
        userController.create(user1);
        User newUser1 = new User();
        newUser1.setId(2L);
        newUser1.setEmail("email@email.ru");
        newUser1.setName("New name");
        newUser1.setLogin("New_login");
        newUser1.setBirthday(LocalDate.parse("2001-01-01"));
        ValidationException exception = assertThrows(ValidationException.class, () -> userController.update(newUser1));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }
}
