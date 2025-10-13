package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.films.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public void putLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.deleteFilmLike(filmId, userId);
    }

    public void addFilmGenre(Long filmId, Long genreId) {
        filmStorage.addFilmGenre(filmId, genreId);
    }

    public void removeFilmGenre(Long filmId) {
        filmStorage.deleteFilmGenre(filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count).stream().toList();
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAll().stream().toList();
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
}
