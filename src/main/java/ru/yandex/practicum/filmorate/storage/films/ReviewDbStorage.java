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
public class ReviewDbStorage implements ReviewStorage{
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper mapper;

    private static final String FIND_BY_ID_QUERY = """
            SELECT *
            FROM reviews
            WHERE review_id = ?
            """;

    private static final String FIND_BY_FILM_AND_USER_ID_QUERY = """
            SELECT *
            FROM reviews
            WHERE film_id = ? AND user_id = ?
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
            WHERE review_id = ?
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

    private static final String FIND_LIKE_OR_DISLIKE_QUERY = """
            SELECT COUNT(*)
            FROM review_likes
            WHERE review_id = ? AND user_id = ?
            """;

    public static final String UPDATE_LIKE_OR_DISLIKE = """
            UPDATE review_likes
            SET is_like = ?
            WHERE review_id = ? AND user_id = ?
            """;
    @Override
    public Optional<Review> findById(int id) {
        return jdbc.query(FIND_BY_ID_QUERY, mapper, id).stream()
                .findFirst();
    }

    @Override
    public Optional<Review> findByFilmAndUserId(Review review) {
        return jdbc.query(FIND_BY_FILM_AND_USER_ID_QUERY, mapper, review.getFilmId(),
                        review.getUserId()).stream()
                .findFirst();
    }

    @Override
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
        return findById(id).get();
    }

    @Override
    public boolean delete(int id) {
        int rowsAffected = jdbc.update(DELETE_QUERY, id);
        return rowsAffected > 0;
    }

    @Override
    public Review update(Review review) {
        int rowsAffected = jdbc.update(UPDATE_QUERY, review.getContent(), review.getIsPositive(),
                review.getFilmId(), review.getReviewId());
        if (rowsAffected <= 0) throw new InternalServerException("Не удалось обновить отзыв");
        return findById(review.getReviewId()).get();
    }

    @Override
    public List<Review> findByFilmId(int filmId, int count) {
        return jdbc.query(FIND_BY_FILM_ID_QUERY, mapper, filmId, count);
    }

    @Override
    public List<Review> findAll(int count) {
        return jdbc.query(FIND_ALL_QUERY, mapper, count);
    }

    @Override
    public int addLikeOrDislike(int reviewId, int userId, boolean isLike) {
        return jdbc.update(ADD_LIKE_OR_DISLIKE_QUERY, reviewId, userId, isLike);
    }

    @Override
    public int updateLikeOrDislike(int reviewId, int userId, boolean isLike) {
        return jdbc.update(UPDATE_LIKE_OR_DISLIKE, isLike, reviewId, userId);
    }

    @Override
    public int changeUseful(Review review, boolean isLike, int delta) {
        if (isLike) {
            return jdbc.update(CHANGE_USEFUL_QUERY, review.getUseful() + delta, review.getReviewId());
        }
        return jdbc.update(CHANGE_USEFUL_QUERY, review.getUseful() - delta, review.getReviewId());
    }

    @Override
    public boolean deleteLikeOrDislike(int reviewId, int userId) {
        int rowsAffected = jdbc.update(DELETE_LIKE_OR_DISLIKE_QUERY, reviewId, userId);
        return rowsAffected > 0;
    }

    @Override
    public boolean likeOrDislikeExists(int reviewId, int userId) {
        int rowsFound = jdbc.queryForObject(FIND_LIKE_OR_DISLIKE_QUERY, Integer.class, reviewId, userId);
        return rowsFound > 0;
    }
}
