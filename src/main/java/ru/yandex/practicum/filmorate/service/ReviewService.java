package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.films.ReviewDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDbStorage storage;
    private final FilmService filmService;
    private final UserService userService;

    public Review findById(int id) {
        return storage.findById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public Review findByFilmAndUserId(Review review) {
        return storage.findByFilmAndUserId(review).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public Review create(Review review) {
        if (userService.getUserById((long) review.getUserId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (filmService.getFilmById((long) review.getFilmId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        return storage.create(review);
    }

    public boolean delete(int id) {
        return storage.delete(id);
    }

    public Review update(Review review) {
        Review oldReview = findByFilmAndUserId(review);
        if (StringUtils.hasText(review.getContent())) {
            oldReview.setContent(review.getContent());
        }
        if (review.getIsPositive() != null) {
            oldReview.setIsPositive(review.getIsPositive());
        }
        if (review.getFilmId() != null) {
            oldReview.setFilmId(review.getFilmId());
        }
        return storage.update(oldReview);
    }

    public List<Review> findByFilmId(int filmId, int count) {
        if (filmService.getFilmById((long) filmId) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        return storage.findByFilmId(filmId, count);
    }

    public List<Review> findAll(int count) {
        return storage.findAll(count);
    }

    public void addLikeOrDislike(int reviewId, int userId, boolean isLike) {
        if (userService.getUserById((long) userId) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        Review review = storage.findById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        int rowsAffected;
        if (storage.likeOrDislikeExists(reviewId, userId)) {
            rowsAffected = storage.updateLikeOrDislike(reviewId, userId, isLike);
            if (rowsAffected <= 0) {
                throw new InternalServerException("Не удалось поставить лайк/дизлайк");
            }
            storage.changeUseful(review, isLike, 2);
            return;
        }
        rowsAffected = storage.addLikeOrDislike(reviewId, userId, isLike);
        if (rowsAffected <= 0) {
            throw new InternalServerException("Не удалось поставить лайк/дизлайк");
        }
        rowsAffected = storage.changeUseful(review, isLike, 1);
        if (rowsAffected <= 0) {
            storage.deleteLikeOrDislike(reviewId, userId);
            throw new InternalServerException("Не удалось поставить лайк/дизлайк");
        }
    }

    public void deleteLikeOrDislike(int reviewId, int userId, boolean isLike) {
        Review review = storage.findById(reviewId).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        boolean success = storage.deleteLikeOrDislike(reviewId, userId);
        if (!success) throw new InternalServerException("Не удалось удалить лайк/дизлайк");
        int rowsAffected = storage.changeUseful(review, !isLike, 1);
        if (rowsAffected <= 0) {
            storage.deleteLikeOrDislike(reviewId, userId);
            throw new InternalServerException("Не удалось удалить лайк/дизлайк");
        }
    }

}
