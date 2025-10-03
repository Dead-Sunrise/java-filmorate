package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MPARowMapper implements RowMapper<RatingMPA> {
    @Override
    public RatingMPA mapRow(ResultSet rs, int rowNum) throws SQLException {
        return RatingMPA.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }
}