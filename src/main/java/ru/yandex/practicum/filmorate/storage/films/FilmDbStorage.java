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
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id""";
    private static final String FIND_FILM_BY_ID = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            WHERE f.id = ?""";
    private static final String FIND_POPULAR_FILMS = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name,
                COUNT(fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration,
                f.mpa_id, d.director_id, d.name, m.name
            ORDER BY likes_count DESC
            LIMIT ?""";
    private static final String FIND_COMMON_FILMS = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, f.director_id, d.name AS director_name, m.name AS mpa_name
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            JOIN mpa_ratings m ON f.mpa_id = m.id
            WHERE f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            AND f.id IN (SELECT film_id FROM film_likes WHERE user_id = ?)
            ORDER BY (SELECT COUNT(*) FROM film_likes WHERE film_id = f.id) DESC""";
    private static final String FIND_POPULAR_FILMS_BY_GENRE = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name,
            COUNT(DISTINCT fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            JOIN film_genre fg ON f.id = fg.film_id AND fg.genre_id = ?
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name, m.name
            ORDER BY likes_count DESC
            LIMIT ?
            """;
    private static final String FIND_POPULAR_FILMS_BY_YEAR = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name,
            COUNT(DISTINCT fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            WHERE EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name, m.name
            ORDER BY likes_count DESC
            LIMIT ?
            """;
    private static final String FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name,
            COUNT(DISTINCT fl.user_id) AS likes_count
            FROM films f
            LEFT JOIN directors d ON f.director_id = d.director_id
            JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id
            JOIN film_genre fg ON f.id = fg.film_id
            WHERE fg.genre_id = ?
            AND EXTRACT(YEAR FROM f.release_date) = ?
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name, m.name
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
    private static final String SEARCH_FILMS_BY_TITLE = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN directors d ON f.director_id = d.director_id
            WHERE LOWER(f.name) LIKE LOWER(?)
            ORDER BY f.id DESC""";
    private static final String SEARCH_FILMS_BY_DIRECTOR = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN directors d ON f.director_id = d.director_id
            WHERE LOWER(d.name) LIKE LOWER(?)
            ORDER BY f.id DESC""";
    private static final String SEARCH_FILMS_BY_TITLE_AND_DIRECTOR = """
            SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
            f.mpa_id, d.director_id, d.name AS director_name, m.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN directors d ON f.director_id = d.director_id
            WHERE LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?)
            ORDER BY f.id DESC""";
    private static final String ADD_FILM = """
            INSERT INTO films(name, description, release_date, duration, mpa_id, director_id)
            VALUES (?, ?, ?, ?, ?, ?)""";
    private static final String ADD_FILM_GENRE = "INSERT INTO film_genre(film_id, genre_id) VALUES(?, ?)";
    private static final String ADD_FILM_LIKE = "INSERT INTO film_likes(film_id, user_id) VALUES(?, ?)";
    private static final String UPDATE_FILM = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?, director_id = ?
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

    public Collection<Film> getPopularFilmsByGenre(int count, Long genreId) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_FILMS_BY_GENRE, filmRowMapper, genreId, count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    public Collection<Film> getPopularFilmsByYear(int count, Integer year) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_FILMS_BY_YEAR, filmRowMapper, year, count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    public Collection<Film> getPopularFilmsByGenreAndYear(int count, Long genreId, Integer year) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_FILMS_BY_GENRE_AND_YEAR, filmRowMapper, genreId, year, count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    public Collection<Film> getPopularFilms(Integer count) {
        List<Film> films = jdbcTemplate.query(FIND_POPULAR_FILMS, filmRowMapper, count);
        films.forEach(this::loadFilmDetails);
        return films;
    }

    public List<Film> searchFilms(String query, String by) {
        String search = "%" + query.toLowerCase() + "%";
        List<Film> films;
        if (by.equals("title")) {
            films = jdbcTemplate.query(SEARCH_FILMS_BY_TITLE, filmRowMapper, search);
        } else if (by.equals("director")) {
            films = jdbcTemplate.query(SEARCH_FILMS_BY_DIRECTOR, filmRowMapper, search);
        } else if (by.equals("title,director") || by.equals("director,title")) {
            films = jdbcTemplate.query(SEARCH_FILMS_BY_TITLE_AND_DIRECTOR, filmRowMapper, search, search);
        } else {
            throw new ValidationException("Параметр поиска должен быть title или director или title,director");
        }
        films.forEach(this::loadFilmDetails);
        return films;
    }

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
            PreparedStatement ps = connection.prepareStatement(ADD_FILM, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            if (film.getReleaseDate() != null) {
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            Long directorId = null;
            if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
                directorId = Long.valueOf(film.getDirectors().iterator().next().getId());
            }
            ps.setObject(6, directorId, Types.BIGINT);
            return ps;
        }, keyHolder);
        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);
        saveFilmGenres(filmId, film.getGenres());
        return getFilmById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден после создания"));
    }

    @Override
    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }
        validateFilm(film);
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(UPDATE_FILM);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            if (film.getReleaseDate() != null) {
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            } else {
                ps.setNull(3, Types.DATE);
            }
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null) {
                ps.setLong(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            Long directorId = (film.getDirectors() != null && !film.getDirectors().isEmpty()) ? Long.valueOf(film.getDirectors().iterator().next().getId()) : null;
            ps.setObject(6, directorId, Types.BIGINT);
            ps.setLong(7, film.getId());
            return ps;
        });
        updateFilmGenres(film.getId(), film.getGenres());
        return getFilmById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден после обновления"));
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
        if (!filmExists(filmId)) {
            throw new NotFoundException("Фильм по id не найден");
        }
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь по id не найден");
        }
        jdbcTemplate.update(DELETE_FILM_LIKE, filmId, userId);
    }

    @Override
    public List<Film> findFilmsByDirector(Long directorId) {
        if (!directorExists(directorId)) {
            throw new NotFoundException("Режиссер не найден");
        }
        String sql = """
                    SELECT f.id AS film_id, f.name AS film_name, f.description, f.release_date, f.duration,
                           f.mpa_id, d.director_id AS director_id, d.name AS director_name, m.name AS mpa_name
                    FROM films f
                    LEFT JOIN directors d ON f.director_id = d.director_id
                    LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                    WHERE d.director_id = ?
                """;
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, directorId);
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

    private void loadFilmDetails(Film film) {
        Long filmId = film.getId();
        Set<Genre> genres = new LinkedHashSet<>(jdbcTemplate.query(FIND_GENRES_BY_FILM_ID, genreRowMapper, filmId));
        film.setGenres(genres);
        Set<Long> likes = new HashSet<>(jdbcTemplate.query(FIND_LIKES_BY_FILM_ID, (rs, rowNum) -> rs.getLong("user_id"), filmId));
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
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_genre WHERE film_id = ? AND genre_id = ?", Integer.class, filmId, genreId);
        return count > 0;
    }

    private boolean isLikeExists(Long filmId, Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?", Integer.class, filmId, userId);
        return count > 0;
    }

    private boolean directorExists(Long directorId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM directors WHERE director_id = ?", Integer.class, directorId);
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
