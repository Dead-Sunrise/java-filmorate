package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.users.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void findAllUsersTest() { //Тест получения всех пользователей
        User user1 = User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build();
        User user2 = User.builder()
                .email("user2@mail.ru")
                .login("Login2")
                .name("User2")
                .birthday(LocalDate.of(2000, 1, 2))
                .friends(new HashSet<>())
                .build();
        userDbStorage.create(user1);
        userDbStorage.create(user2);
        Collection<User> users = userDbStorage.findAll();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@mail.ru", "user2@mail.ru");
    }

    @Test
    void getUserByIdTest() { //Тест получения конкретного пользователя по id
        User user = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        Optional<User> controlUser = userDbStorage.getUserById(user.getId());
        assertThat(controlUser).isPresent();
        assertThat(controlUser.get().getEmail()).isEqualTo("user1@mail.ru");
        assertThat(controlUser.get().getLogin()).isEqualTo("Login1");
    }

    @Test
    void getCommonFriendsTest() { //Тест получения списка общих друзей у двух пользователей
        User user1 = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        User user2 = userDbStorage.create(User.builder()
                .email("user2@mail.ru")
                .login("Login2")
                .name("User2")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        User user3 = userDbStorage.create(User.builder()
                .email("user3@mail.ru")
                .login("Login3")
                .name("User3")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        userDbStorage.addFriendship(user1.getId(), user3.getId());
        userDbStorage.addFriendship(user2.getId(), user3.getId());
        Collection<User> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends).extracting(User::getEmail)
                .containsExactly("user3@mail.ru");
    }

    @Test
    void getFriendsTest() { //Тест получения списка друзей у пользователя
        User user1 = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        User user2 = userDbStorage.create(User.builder()
                .email("user2@mail.ru")
                .login("Login2")
                .name("User2")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        User user3 = userDbStorage.create(User.builder()
                .email("user3@mail.ru")
                .login("Login3")
                .name("User3")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        userDbStorage.addFriendship(user1.getId(), user2.getId());
        userDbStorage.addFriendship(user1.getId(), user3.getId());
        Collection<User> friends = userDbStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(2);
        assertThat(friends).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user2@mail.ru", "user3@mail.ru");
    }

    @Test
    void createUserTest() { //Тест создания пользователя с пустым именем и датой рождения
        User user = User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("")
                .birthday(null)
                .friends(new HashSet<>())
                .build();
        User createdUser = userDbStorage.create(user);
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Login1");
        assertThat(createdUser.getBirthday()).isNull();
    }

    @Test
    void updateUserTest() { //Тест обновления пользователя
        User user = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        User updatedUser = userDbStorage.update(user.toBuilder()
                .email("NewUser1@mail.ru")
                .login("NewLogin1")
                .name("NewUser1")
                .birthday(LocalDate.of(1995, 1, 1))
                .build());
        assertThat(updatedUser.getEmail()).isEqualTo("NewUser1@mail.ru");
        assertThat(updatedUser.getLogin()).isEqualTo("NewLogin1");
        assertThat(updatedUser.getName()).isEqualTo("NewUser1");
        assertThat(updatedUser.getBirthday()).isEqualTo(LocalDate.of(1995, 1, 1));
    }

    @Test
    void deleteFriendshipTest() { //Тест удаления пользователяя из друзей
        User user1 = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        User user2 = userDbStorage.create(User.builder()
                .email("user2@mail.ru")
                .login("Login2")
                .name("User2")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        userDbStorage.addFriendship(user1.getId(), user2.getId());
        userDbStorage.deleteFriendship(user1.getId(), user2.getId());
        User foundUser = userDbStorage.getUserById(user1.getId()).orElseThrow();
        assertThat(foundUser.getFriends()).isEmpty();
    }

    @Test
    void deleteUserByIdTest() { //Тест удаления пользователя по id
        User user = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        Film film = Film.builder()
                .name("Film1")
                .description("Description1")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build();
        jdbcTemplate.update("INSERT INTO film_likes(film_id, user_id) VALUES (?, ?)", film.getId(), user.getId());
        userDbStorage.deleteUserById(user.getId());
        Optional<User> deletedUser = userDbStorage.getUserById(user.getId());
        assertThat(deletedUser).isEmpty();
        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE user_id = ?", Integer.class, user.getId());
        assertThat(likesCount).isZero();
    }
}