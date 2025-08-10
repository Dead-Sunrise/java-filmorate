package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @GetMapping
    public Collection<Film> findAll() {
        log.info("/films GET Запрос на получение списка фильмов.");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("/films POST Запрос на добавление нового фильма");
        try {
            for (Film film1 : films.values()) {
                if (film.getName().equals(film1.getName())) {
                    log.error("Фильм с таким названием уже есть: {}", film.getName());
                    throw new ValidationException("Фильм с таким названием уже есть.");
                }
            }
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                log.error("Дата релиза раньше 28.12.1895: {}", film.getReleaseDate());
                throw new ValidationException("Дата релиза не может быть раньше 28.12.1895.");
            }
            film.setId(getNextId());
            films.put(film.getId(), film);
            log.info("Фильм успешно добавлен: {}", film.getName());
            return film;
        } catch (RuntimeException e) {
            log.error("Ошибка при создании пользователя.");
            throw e;
        }
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("/films PUT Запрос на изменение фильма");
        if (newFilm.getId() == null) {
            log.error("Id не указан");
            throw new ValidationException("Id должен быть указан.");
        }
        if (films.containsKey(newFilm.getId())) {
            for (Film film1 : films.values()) {
                if (newFilm.getName() != null && newFilm.getName().equals(film1.getName()) && !newFilm.getId().equals(film1.getId())) {
                    log.error("Фильм с таким названием уже существует: {}", newFilm.getName());
                    throw new ValidationException("Фильм с таким названием уже есть.");
                }
            }
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null) {
                log.debug("Указано новое значение name, изменение значения: {}", newFilm.getName());
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null) {
                log.debug("Указано новое значение description, изменение значения: {}", newFilm.getDescription());
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null) {
                log.debug("Указано новое значение releaseDate, изменение значения: {}", newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() != null) {
                log.debug("Указано новое значение duration, изменение значения: {}", newFilm.getDuration());
                oldFilm.setDuration(newFilm.getDuration());
            }
            log.info("Данные о фильме успешно обновлены");
            return oldFilm;
        }
        log.error("Указан несуществующий id фильма {}:", newFilm.getId());
        throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден.");
    }
}