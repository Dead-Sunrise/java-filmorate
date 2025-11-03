package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.films.FilmStorage;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    public void putLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
        feedService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.deleteFilmLike(filmId, userId);
        feedService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public void addFilmGenre(Long filmId, Long genreId) {
        filmStorage.addFilmGenre(filmId, genreId);
    }

    public void removeFilmGenre(Long filmId) {
        filmStorage.deleteFilmGenre(filmId);
    }

    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов должно быть положительным числом.");
        }
        List<Film> popularFilms;
        if (genreId != null && year != null) {
            popularFilms = filmStorage.getPopularFilmsByGenreAndYear(count, genreId, year).stream().toList();
        } else if (genreId != null) {
            popularFilms = filmStorage.getPopularFilmsByGenre(count, genreId).stream().toList();
        } else if (year != null) {
            popularFilms = filmStorage.getPopularFilmsByYear(count, year).stream().toList();
        } else {
            popularFilms = filmStorage.getPopularFilms(count).stream().toList();
        }
        return popularFilms;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId).stream().toList();
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll().stream().toList();
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return filmStorage.searchFilms(query.trim(), by);
    }

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public void removeFilmById(Long filmId) {
        filmStorage.deleteFilmById(filmId);
    }

    public void removeAllFilms() {
        filmStorage.deleteAllFilms();
    }

    public List<Film> getFilmsByDirector(Long directorId, List<String> sortBy) {
        List<Film> films = filmStorage.findFilmsByDirector(directorId);
        if (sortBy == null || sortBy.isEmpty()) {
            return films;
        }
        if (sortBy.contains("likes") && sortBy.contains("year")) {
            films.sort(Comparator
                    .comparingInt((Film f) -> f.getLikes().size()).reversed()
                    .thenComparing(Film::getReleaseDate)
            );
        } else if (sortBy.contains("year")) {
            films.sort(Comparator.comparing(Film::getReleaseDate));
        } else if (sortBy.contains("likes")) {
            films.sort(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed());
        }
        return films;
    }

}
