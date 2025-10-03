package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.mappers.MPARowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class MPADbStorage implements MPAStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MPARowMapper mpaRowMapper;
    private final static String FIND_ALL_MPA = "SELECT * FROM mpa_ratings";
    private final static String FIND_MPA_BY_ID = "SELECT * FROM mpa_ratings WHERE id = ?";

    @Override
    public Collection<RatingMPA> findAll() {
        return jdbcTemplate.query(FIND_ALL_MPA, mpaRowMapper);
    }

    @Override
    public Optional<RatingMPA> getMPAById(Long id) {
        try {
            RatingMPA mpa = jdbcTemplate.queryForObject(FIND_MPA_BY_ID, mpaRowMapper, id);
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
