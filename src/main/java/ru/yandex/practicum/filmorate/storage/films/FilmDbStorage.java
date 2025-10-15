package ru.yandex.practicum.filmorate.storage.films;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;

@Repository
@AllArgsConstructor
@Qualifier("db")
public class FilmDbStorage implements FilmStorage {
    private final FilmRowMapper filmRowMapper;
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;
    private static final String FIND_ALL_FILMS = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            """;

    private static final String FIND_FILM_BY_ID = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            WHERE f.id = ?
            """;

    private static final String FIND_POPULAR_FILMS = """
            SELECT f.*, m.name AS mpa_name, COUNT(fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name
            ORDER BY likes_count DESC
            LIMIT ?
            """;
    private static final String FIND_GENRES_BY_FILM_ID = """
            SELECT g.* FROM genres g
            JOIN film_genre fg ON g.id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.id""";
    private static final String FIND_LIKES_BY_FILM_ID = """
            SELECT user_id FROM film_likes
            WHERE film_id = ?""";
    private static final String FIND_COMMON_FILMS = """
            SELECT f.*, m.name AS mpa_name
            FROM films f
            JOIN mpa_ratings m ON f.mpa_id = m.id
            WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = f.id) DESC
            """;
    private static final String ADD_FILM = """
            INSERT INTO films(name, description, release_date, duration, mpa_id)
            VALUES (?, ?, ?, ?, ?)""";
    private static final String ADD_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES(?, ?)";
    private static final String ADD_FILM_LIKE = "INSERT INTO film_likes(film_id, user_id) VALUES(?, ?)";
    private static final String UPDATE_FILM = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
            WHERE id = ?""";
    private static final String DELETE_FILM_BY_ID = "DELETE FROM films WHERE id = ?";
    private static final String DELETE_ALL_FILMS = "DELETE FROM films";
    private static final String DELETE_FILM_GENRE = "DELETE FROM film_genre WHERE film_id = ?";
    private static final String DELETE_FILM_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(FIND_ALL_FILMS, filmRowMapper);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        try {
            Film film = jdbcTemplate.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
            if (film != null) {
                loadFilmDetails(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Collection<Film> getPopularFilms(Integer count) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_FILMS, filmRowMapper, count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    public Collection<Film> getCommonFilms(Long userId, Long friendId) {
        try {
            if (!userExists(userId) || !userExists(friendId)) {
                throw new NotFoundException("Один из пользователей не найден.");
            }
            List<Film> films = jdbcTemplate.query(FIND_COMMON_FILMS, filmRowMapper, userId, friendId);
            films.forEach(this::loadFilmDetails);
            return films;
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    ;

    public void addFilmGenre(Long filmId, Long genreId) {
        if (!filmExists(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        if (!genreExists(genreId)) {
            throw new NotFoundException("Жанр с ID " + genreId + " не найден");
        }
        if (isFilmGenreExists(filmId, genreId)) {
            return;
        }
        jdbcTemplate.update(ADD_FILM_GENRE, filmId, genreId);
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmExists(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (isLikeExists(filmId, userId)) {
            return;
        }
        jdbcTemplate.update(ADD_FILM_LIKE, filmId, userId);
    }

    @Override
    public Film create(Film film) {
        validateFilm(film);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(ADD_FILM, new String[]{"id"});
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            if (film.getReleaseDate() != null) {
                preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            } else {
                preparedStatement.setNull(3, Types.DATE);
            }
            preparedStatement.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                preparedStatement.setLong(5, film.getMpa().getId());
            } else {
                preparedStatement.setNull(5, Types.INTEGER);
            }
            return preparedStatement;
        }, keyHolder);
        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);
        saveFilmGenres(filmId, film.getGenres());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        validateFilm(film);
        jdbcTemplate.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        updateFilmGenres(film.getId(), film.getGenres());
        return getFilmById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден после обновления"));
    }

    @Override
    public void deleteFilmById(Long id) {
        if (getFilmById(id).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        jdbcTemplate.update(DELETE_FILM_GENRE, id);
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id = ?", id);
        jdbcTemplate.update(DELETE_FILM_BY_ID, id);
    }

    public void deleteAllFilms() {
        jdbcTemplate.update("DELETE FROM film_genre");
        jdbcTemplate.update("DELETE FROM film_likes");
        jdbcTemplate.update(DELETE_ALL_FILMS);
    }

    public void deleteFilmGenre(Long filmId) {
        jdbcTemplate.update(DELETE_FILM_GENRE, filmId);
    }

    public void deleteFilmLike(Long filmId, Long userId) {
        jdbcTemplate.update(DELETE_FILM_LIKE, filmId, userId);
    }

    private void loadFilmDetails(Film film) {
        Long filmId = film.getId();
        Set<Genre> genres = new HashSet<>(jdbcTemplate.query(FIND_GENRES_BY_FILM_ID, genreRowMapper, filmId));
        film.setGenres(genres);
        Set<Long> likes = new HashSet<>(jdbcTemplate.query(FIND_LIKES_BY_FILM_ID,
                (rs, rowNum) -> rs.getLong("user_id"), filmId));
        film.setLikes(likes);
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        for (Genre genre : genres) {
            jdbcTemplate.update(ADD_FILM_GENRE, filmId, genre.getId());
        }
    }

    private void updateFilmGenres(Long filmId, Set<Genre> newGenres) {
        jdbcTemplate.update(DELETE_FILM_GENRE, filmId);
        saveFilmGenres(filmId, newGenres);
    }

    private boolean filmExists(Long filmId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM films WHERE id = ?", Integer.class, filmId);
        return count > 0;
    }

    private boolean genreExists(Long genreId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genres WHERE id = ?", Integer.class, genreId);
        return count > 0;
    }

    private boolean userExists(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        return count > 0;
    }

    private boolean isFilmGenreExists(Long filmId, Long genreId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_genre WHERE film_id = ? AND genre_id = ?",
                Integer.class, filmId, genreId);
        return count > 0;
    }

    private boolean isLikeExists(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class, filmId, userId);
        return count > 0;
    }

    private void validateFilm(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            String mpaSql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
            Integer mpaCount = jdbcTemplate.queryForObject(mpaSql, Integer.class, film.getMpa().getId());
            if (mpaCount == 0) {
                throw new NotFoundException("Указанный MPA рейтинг не существует");
            }
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (!genreExists(genre.getId())) {
                    throw new NotFoundException("Жанр с ID " + genre.getId() + " не существует");
                }
            }
        }
        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, Month.DECEMBER, 28))) {
                throw new ValidationException("Недопустимая дата фильма");
            }
        }
    }
}
