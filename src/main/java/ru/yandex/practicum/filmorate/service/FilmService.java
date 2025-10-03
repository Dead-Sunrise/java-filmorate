package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.films.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.users.UserDbStorage;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    public void putLike(Long filmId, Long userId) {
        filmDbStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmDbStorage.deleteFilmLike(filmId, userId);
    }

    public void addFilmGenre(Long filmId, Long genreId) {
        filmDbStorage.addFilmGenre(filmId, genreId);
    }

    public void removeFilmGenre(Long filmId) {
        filmDbStorage.deleteFilmGenre(filmId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmDbStorage.getPopularFilms(count);
    }

    public Optional<Film> getFilmById(Long filmId) {
        return filmDbStorage.getFilmById(filmId);
    }

    public Collection<Film> getAllFilms() {
        return filmDbStorage.findAll();
    }

    public Film createFilm(Film film) {
        return filmDbStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmDbStorage.update(newFilm);
    }

    public void removeFilmById(Long filmId) {
        filmDbStorage.deleteFilmById(filmId);
    }

    public void removeAllFilms() {
        filmDbStorage.deleteAllFilms();
    }
}
