package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("/films GET Запрос на получение списка фильмов.");
        return filmService.getAllFilms();
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("/films/popular?count={count} GET Запрос на получение популярных фильмов по количеству лайков");
        return filmService.getPopularFilms(count);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("/films POST Запрос на добавление нового фильма");
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("/films PUT Запрос на изменение фильма");
        return filmService.updateFilm(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        log.info("/films/{id}/like/{userId} PUT Запрос на добавление лайка");
        filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        log.info("/films/{id}/like/{userId} DELETE Запрос на удаление лайка");
        filmService.removeLike(id, userId);
    }
}