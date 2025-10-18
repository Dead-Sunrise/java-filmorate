package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director add(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        String sql = "UPDATE directors SET name=? WHERE director_id=?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return director;
    }

    @Override
    public Optional<Director> getById(int id) {
        List<Director> list = jdbcTemplate.query("SELECT * FROM directors WHERE director_id=?", this::mapRowToDirector, id);
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(0));
    }

    @Override
    public Collection<Director> getAll() {
        return jdbcTemplate.query("SELECT * FROM directors", this::mapRowToDirector);
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM directors WHERE director_id=?";
        jdbcTemplate.update(sql, id);
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("director_id"), rs.getString("name"));
    }
}