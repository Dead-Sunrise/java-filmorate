package ru.yandex.practicum.filmorate.storage.films;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> getFilmById(Long filmId);

    Collection<Film> getPopularFilms(Integer count);

    void addFilmGenre(Long filmId, Long genreId);

    void addLike(Long filmId, Long userId);

    Film create(Film film);

    Film update(Film newFilm);

    void deleteFilmById(Long filmId);

    void deleteAllFilms();

    void deleteFilmGenre(Long filmId);

    void deleteFilmLike(Long filmId, Long userId);
}
