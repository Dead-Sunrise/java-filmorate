package ru.yandex.practicum.filmorate.storage.films.genres;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;
    private static final String FIND_ALL_GENRES = "SELECT * FROM genres";
    private static final String FIND_GENRE_BY_ID = "SELECT * FROM genres WHERE id = ?";

    @Override
    public Collection<Genre> findAll() {
        return jdbcTemplate.query(FIND_ALL_GENRES, genreRowMapper);
    }

    @Override
    public Optional<Genre> getGenreById(Long id) {
        try {
            Genre genre = jdbcTemplate.queryForObject(FIND_GENRE_BY_ID,
                    genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
