package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        log.info("/films/{id} GET Запрос на получение данных конкретного фильма по id");
        return filmService.getFilmById(id);
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("/films GET Запрос на получение списка фильмов.");
        return filmService.getAllFilms();
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("/films/popular?count={count} GET Запрос на получение популярных фильмов по количеству лайков");
        return filmService.getPopularFilms(count);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam("userId") Long userId,
                                     @RequestParam("friendId") Long friendId) {
        log.info("""
                /films/common?userId={userId}&friendId={friendId}
                GET Запрос на получение общих фильмов пользователей с ID {} и {}""", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
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
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("/films/{id}/like/{userId} PUT Запрос на добавление лайка");
        filmService.putLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("/films/{id}/like/{userId} DELETE Запрос на удаление лайка");
        filmService.removeLike(id, userId);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(@PathVariable Long directorId, @RequestParam(required = false) List<String> sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
}
