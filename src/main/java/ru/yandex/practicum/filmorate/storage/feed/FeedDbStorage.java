package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Event> getFeedByUserId(Long userId) {
        String sql = "SELECT ef.event_id, ef.user_id, et.name as event_type, eo.name as operation, " +
                "ef.entity_id, ef.time " +
                "FROM event_feed ef " +
                "JOIN event_type et ON ef.type_id = et.type_id " +
                "JOIN event_operation eo ON ef.operation_id = eo.operation_id " +
                "WHERE ef.user_id = ? " +
                "ORDER BY ef.time";

        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }

    @Override
    public Event addEvent(Event event) {
        String sql = "INSERT INTO event_feed (user_id, type_id, operation_id, entity_id, time) " +
                "VALUES (?, ?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
            ps.setLong(1, event.getUserId());
            ps.setInt(2, getEventTypeId(event.getEventType()));
            ps.setInt(3, getOperationId(event.getOperation()));
            ps.setLong(4, event.getEntityId());
            ps.setTimestamp(5, Timestamp.from(Instant.ofEpochMilli(event.getTimestamp())));
            return ps;
        }, keyHolder);

        event.setEventId(keyHolder.getKey().longValue());
        return event;
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .entityId(rs.getLong("entity_id"))
                .timestamp(rs.getTimestamp("time").getTime())
                .build();
    }

    private int getEventTypeId(EventType eventType) {
        return switch (eventType) {
            case LIKE -> 1;
            case REVIEW -> 2;
            case FRIEND -> 3;
        };
    }

    private int getOperationId(Operation operation) {
        return switch (operation) {
            case REMOVE -> 1;
            case ADD -> 2;
            case UPDATE -> 3;
        };
    }
}