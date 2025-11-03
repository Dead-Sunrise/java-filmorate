package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date releaseDate = rs.getDate("release_date");
        LocalDate releaseLocalDate = (releaseDate != null) ? releaseDate.toLocalDate() : null;
        RatingMPA mpa = null;
        Long mpaId = rs.getLong("mpa_id");
        if (!rs.wasNull()) {
            mpa = RatingMPA.builder().id(mpaId).name(rs.getString("mpa_name")).build();
        }
        Long directorId = rs.getLong("director_id");
        Director director = null;
        if (!rs.wasNull()) {
            director = new Director(directorId.intValue(), rs.getString("director_name"));
        }
        Set<Director> directors = (director != null) ? Set.of(director) : Collections.emptySet();
        return Film.builder().id(rs.getLong("film_id")).name(rs.getString("film_name")).description(rs.getString("description")).releaseDate(releaseLocalDate).duration(rs.getInt("duration")).mpa(mpa).directors(directors).build();
    }
}