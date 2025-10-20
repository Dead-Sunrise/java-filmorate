package ru.yandex.practicum.filmorate.storage.films;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Optional<Film> getFilmById(Long filmId);

    Collection<Film> getCommonFilms(Long userId, Long friendId);

    Collection<Film> getPopularFilmsByGenreAndYear(int count, Long genreId, Integer year);

    Collection<Film> getPopularFilmsByGenre(int count, Long genreId);

    Collection<Film> getPopularFilmsByYear(int count, Integer year);

    Collection<Film> getPopularFilms(Integer count);

    List<Film> searchFilms(String query, String by);

    void addFilmGenre(Long filmId, Long genreId);

    void addLike(Long filmId, Long userId);

    Film create(Film film);

    Film update(Film newFilm);

    void deleteFilmById(Long filmId);

    void deleteAllFilms();

    void deleteFilmGenre(Long filmId);

    void deleteFilmLike(Long filmId, Long userId);

    List<Film> findFilmsByDirector(Long directorId);

}
