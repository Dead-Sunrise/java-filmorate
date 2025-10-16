package ru.yandex.practicum.filmorate.storage.films;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage {
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;

    private static final String FIND_BY_ID_QUERY = """
            SELECT *
            FROM reviews
            WHERE review_id = ?
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO reviews (message, is_positive, user_id, film_id)
            VALUES (?, ?, ?, ?)
            """;

    private static final String DELETE_QUERY = """
            DELETE FROM reviews
            WHERE review_id = ?
            """;

    private static final String UPDATE_QUERY = """
            UPDATE reviews
            SET message = ?, is_positive = ?, film_id = ?
            """;

    private static final String FIND_BY_FILM_ID_QUERY = """
            SELECT *
            FROM reviews
            WHERE film_id = ?
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String FIND_ALL_QUERY = """
            SELECT *
            FROM reviews
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String ADD_LIKE_OR_DISLIKE_QUERY = """
            INSERT INTO review_likes (review_id, user_id, is_like)
            VALUES (?, ?, ?)
            """;

    private static final String CHANGE_USEFUL_QUERY = """
            UPDATE reviews
            SET useful = ?
            WHERE review_id = ?
            """;

    private static final String DELETE_LIKE_OR_DISLIKE_QUERY = """
            DELETE FROM review_likes
            WHERE review_id = ? AND user_id = ?
            """;

    public Optional<Review> findById(int id) {
        return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID_QUERY, mapper, id));
    }

    public Review create(Review review) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);
        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id == null) throw new InternalServerException("Не удалось сохранить отзыв");
        review.setId(id);
        return review;
    }

    public boolean delete(int id) {
        int rowsAffected = jdbc.update(DELETE_QUERY, id);
        return rowsAffected > 0;
    }

    public Review update(Review review) {
        int rowsAffected = jdbc.update(UPDATE_QUERY, review.getContent(), review.getIsPositive(), review.getFilmId());
        if (rowsAffected < 0) throw new InternalServerException("Не удалось обновить отзыв");
        return review;
    }

    public List<Review> findByFilmId(int filmId, int count) {
        return jdbc.query(FIND_BY_FILM_ID_QUERY, mapper, filmId, count);
    }

    public List<Review> findAll(int count) {
        return jdbc.query(FIND_ALL_QUERY, mapper, count);
    }

    public int addLikeOrDislike(int reviewId, int userId, boolean isLike) {
        return jdbc.update(ADD_LIKE_OR_DISLIKE_QUERY, reviewId, userId, isLike);
    }

    public int changeUseful(Review review, boolean isLike) {
        if (isLike) {
            return jdbc.update(CHANGE_USEFUL_QUERY, review.getUseful() + 1, review.getId());
        }
        return jdbc.update(CHANGE_USEFUL_QUERY, review.getUseful() - 1, review.getId());
    }

    public boolean deleteLikeOrDislike(int reviewId, int userId) {
        int rowsAffected = jdbc.update(DELETE_LIKE_OR_DISLIKE_QUERY, reviewId, userId);
        return rowsAffected > 0;
    }

}
