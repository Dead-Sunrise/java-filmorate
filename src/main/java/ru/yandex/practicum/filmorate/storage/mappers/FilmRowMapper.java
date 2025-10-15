package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Date releaseDate = rs.getDate("release_date");
        LocalDate releaseLocalDate = (releaseDate != null) ? releaseDate.toLocalDate() : null;

        RatingMPA mpa = null;
        Long mpaId = rs.getLong("mpa_id");
        if (!rs.wasNull()) {
            mpa = RatingMPA.builder()
                    .id(mpaId)
                    .name(rs.getString("mpa_name"))
                    .build();
        }
        return new Film().toBuilder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(releaseLocalDate)
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();
    }
}
