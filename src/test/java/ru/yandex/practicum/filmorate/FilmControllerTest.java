package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    FilmController filmController;

    @BeforeEach
    void newController() {
        filmController = new FilmController();
    }

    @Test
    void createCorrectFilmTest() {// тест добавления фильма с корректными данными
        Film film = new Film();
        film.setDescription("Description");
        film.setName("Name");
        film.setReleaseDate(LocalDate.parse("2000-01-01"));
        film.setDuration(100);
        Film createdFilm = filmController.create(film);
        assertEquals(1, filmController.findAll().size());
        assertTrue(createdFilm.getName().equals("Name"));
    }

    @Test
    void createFilmWithExistingNameTest() {// тест добавления фильма с существующим названием
        Film film = new Film();
        film.setDescription("Description");
        film.setName("Name");
        film.setReleaseDate(LocalDate.parse("2000-01-01"));
        film.setDuration(100);
        filmController.create(film);
        Film film1 = new Film();
        film1.setDescription("Description1");
        film1.setName("Name");
        film1.setReleaseDate(LocalDate.parse("2001-01-01"));
        film1.setDuration(10);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film1));
        assertEquals("Фильм с таким названием уже есть.", exception.getMessage());
    }

    @Test
    void createFilmWithInvalidDurationAndReleaseDateTest() {// тест добавления фильмов с описанием в 201 символ и датой релиза раньше 28.12.1895
        Film film = new Film();
        film.setDescription("Description............................................................................." +
                "............................................................................................" +
                "......................");
        film.setName("Name");
        film.setReleaseDate(LocalDate.parse("2000-01-01"));
        film.setDuration(100);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Превышена допустимая длинна описания, 200 символов.", exception.getMessage());
        Film film1 = new Film();
        film1.setDescription("Description");
        film1.setName("Name");
        film1.setReleaseDate(LocalDate.parse("1895-12-27"));
        film1.setDuration(100);
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.create(film1));
        assertEquals("Дата релиза не может быть раньше 28.12.1895.", exception1.getMessage());
    }

    @Test
    void updateCorrectFilm() {// тест изменения фильма с корректными данными
        Film film = new Film();
        film.setDescription("Description");
        film.setName("Name");
        film.setReleaseDate(LocalDate.parse("2000-01-01"));
        film.setDuration(100);
        filmController.create(film);
        Film newFilm = new Film();
        newFilm.setId(1L);
        newFilm.setDescription("New description");
        newFilm.setName("New name");
        newFilm.setReleaseDate(LocalDate.parse("2001-01-01"));
        newFilm.setDuration(10);
        Film updateFilm = filmController.update(newFilm);
        assertEquals(1, filmController.findAll().size());
        assertTrue(updateFilm.getName().equals("New name"));
    }

    @Test
    void updateFilmWithInvalidId() {// тест изменения фильма с указанием пустого и несуществующего id
        Film film = new Film();
        film.setDescription("Description");
        film.setName("Name");
        film.setReleaseDate(LocalDate.parse("2000-01-01"));
        film.setDuration(100);
        filmController.create(film);
        Film newFilm = new Film();
        newFilm.setDescription("New description");
        newFilm.setName("New name");
        newFilm.setReleaseDate(LocalDate.parse("2001-01-01"));
        newFilm.setDuration(10);
        ValidationException exception = assertThrows(ValidationException.class, () -> filmController.update(newFilm));
        assertEquals("Id должен быть указан.", exception.getMessage());
        Film newFilm1 = new Film();
        newFilm1.setId(4L);
        newFilm1.setDescription("New description");
        newFilm1.setName("New name");
        newFilm1.setReleaseDate(LocalDate.parse("2001-01-01"));
        newFilm1.setDuration(10);
        ValidationException exception1 = assertThrows(ValidationException.class, () -> filmController.update(newFilm1));
        assertEquals("Фильм с id = " + newFilm1.getId() + " не найден.", exception1.getMessage());
    }

    @Test
    void updateFilmWithEmptyValues() {// тест изменения с указанием пустых name, description, releaseDate, duration(должны быть заменены на начальные значения)
        Film film = new Film();
        film.setDescription("Description");
        film.setName("Name");
        film.setReleaseDate(LocalDate.parse("2000-01-01"));
        film.setDuration(100);
        filmController.create(film);
        Film newFilm = new Film();
        newFilm.setId(1L);
        Film updateFilm = filmController.update(newFilm);
        assertTrue(updateFilm.getDescription().equals("Description"));
        assertTrue(updateFilm.getName().equals("Name"));
    }
}