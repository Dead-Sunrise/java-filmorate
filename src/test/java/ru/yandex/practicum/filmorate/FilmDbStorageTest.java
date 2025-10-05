package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.films.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.users.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class, UserDbStorage.class, UserRowMapper.class})
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM films");
    }

    @Test
    void findAllFilmsTest() {//Тест получения всех фильмов
        Film film1 = Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build();
        Film film2 = Film.builder()
                .name("Film2")
                .description("Description2")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build();
        filmDbStorage.create(film1);
        filmDbStorage.create(film2);
        Collection<Film> films = filmDbStorage.findAll();
        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName)
                .containsExactlyInAnyOrder("Film1", "Film2");
    }

    @Test
    void getFilmByIdTest() {//Тест получения конкретного фильма по id
        Film film = Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build();
        Film savedFilm = filmDbStorage.create(film);
        Optional<Film> foundFilm = filmDbStorage.getFilmById(savedFilm.getId());
        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Film1");
    }

    @Test
    void getPopularFilmsTest() {//Тест получения популярных фильмов по количеству лайков
        Film film1 = filmDbStorage.create(Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
        Film film2 = filmDbStorage.create(Film.builder()
                .name("Film2")
                .description("Description2")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
        Film film3 = filmDbStorage.create(Film.builder()
                .name("Film3")
                .description("Description3")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
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
        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), 1L);
        Collection<Film> popularFilms = filmDbStorage.getPopularFilms(2);
        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms).extracting(Film::getName)
                .containsExactly("Film1", "Film2");
    }

    @Test
    void createFilmTest() {//Тест создания фильма(с null значениями, там где это допускается)
        Film film = Film.builder()
                .name("Film1")
                .description(null)
                .releaseDate(null)
                .duration(90)
                .mpa(null)
                .build();
        Film createdFilm = filmDbStorage.create(film);
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Film1");
        assertThat(createdFilm.getDescription()).isNull();
        assertThat(createdFilm.getReleaseDate()).isNull();
        assertThat(createdFilm.getMpa()).isNull();
    }

    @Test
    void updateFilmTest() {//Тест обновления данных фильма
        Film film = filmDbStorage.create(Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
        Film updatedFilm = filmDbStorage.update(film.toBuilder()
                .name("NewFilm1")
                .description("NewDescription1")
                .releaseDate(LocalDate.of(2021, 1, 1))
                .duration(100)
                .mpa(RatingMPA.builder().id(2L).build())
                .build());
        assertThat(updatedFilm.getName()).isEqualTo("NewFilm1");
        assertThat(updatedFilm.getDescription()).isEqualTo("NewDescription1");
        assertThat(updatedFilm.getReleaseDate()).isEqualTo(LocalDate.of(2021, 1, 1));
        assertThat(updatedFilm.getDuration()).isEqualTo(100);
        assertThat(updatedFilm.getMpa().getId()).isEqualTo(2L);
    }

    @Test
    void addLikeTest() {//Тест добавления лайка фильму
        Film film = filmDbStorage.create(Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
        User user = userDbStorage.create(User.builder()
                .email("user1@mail.ru")
                .login("Login1")
                .name("User1")
                .birthday(LocalDate.of(2000, 1, 1))
                .friends(new HashSet<>())
                .build());
        filmDbStorage.addLike(film.getId(), user.getId());
        Film foundFilm = filmDbStorage.getFilmById(film.getId()).orElseThrow();
        assertThat(foundFilm.getLikes()).contains(user.getId());
    }

    @Test
    void addFilmGenreTest() {//Тест добавления нового жанра фильму
        Film film = filmDbStorage.create(Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
        filmDbStorage.addFilmGenre(film.getId(), 1L);
        Film foundFilm = filmDbStorage.getFilmById(film.getId()).orElseThrow();
        assertThat(foundFilm.getGenres()).extracting(Genre::getId).contains(1L);
    }

    @Test
    void deleteFilmByIdTest() {//Тест удаления фильма по id
        Film film = filmDbStorage.create(Film.builder()
                .name("Film1")
                .description("Description1")
                .duration(90)
                .mpa(RatingMPA.builder().id(1L).build())
                .build());
        filmDbStorage.deleteFilmById(film.getId());
        Optional<Film> deletedFilm = filmDbStorage.getFilmById(film.getId());
        assertThat(deletedFilm).isEmpty();
    }
}

