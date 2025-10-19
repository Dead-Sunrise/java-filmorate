package ru.yandex.practicum.filmorate.storage.films;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Optional<Review> findById(int id);

    Optional<Review> findByFilmAndUserId(Review review);

    Review create(Review review);

    boolean delete(int id);

    Review update(Review review);

    List<Review> findByFilmId(int filmId, int count);

    List<Review> findAll(int count);

    int addLikeOrDislike(int reviewId, int userId, boolean isLike);

    int updateLikeOrDislike(int reviewId, int userId, boolean isLike);

    int changeUseful(Review review, boolean isLike, int delta);

    boolean deleteLikeOrDislike(int reviewId, int userId);

    boolean likeOrDislikeExists(int reviewId, int userId);
}

