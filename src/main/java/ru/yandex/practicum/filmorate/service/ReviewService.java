package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.films.ReviewDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDbStorage storage;
    private final FilmService filmService;
    private final UserService userService;
    private final FeedService feedService;

    public Review findById(int id) {
        return storage.findById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public Review create(Review review) {
        validateReview(review);
        if (userService.getUserById((long) review.getUserId()) == null) {
            throw new NotFoundException("Пользователь не найден");
        }
        if (filmService.getFilmById((long) review.getFilmId()) == null) {
            throw new NotFoundException("Фильм не найден");
        }
        Review createdReview = storage.create(review);
        feedService.addEvent((long) review.getUserId(), EventType.REVIEW, Operation.ADD, (long) createdReview.getReviewId());
        return createdReview;
    }

    public boolean delete(int id) {
        Review review = findById(id);
        boolean result = storage.delete(id);
        if (result) {
            feedService.addEvent((long) review.getUserId(), EventType.REVIEW, Operation.REMOVE, (long) id);
        }
        return result;
    }

    public Review update(Review review) {
        Optional<Review> existingReviewOpt = storage.findById(review.getReviewId());
        if (existingReviewOpt.isEmpty()) {
            throw new NotFoundException("Отзыв не найден");
        }
        Review oldReview = existingReviewOpt.get();
        if (StringUtils.hasText(review.getContent())) {
            oldReview.setContent(review.getContent());
        }
        if (review.getIsPositive() != null) {
            oldReview.setIsPositive(review.getIsPositive());
        }
        Review updatedReview = storage.update(oldReview);
        feedService.addEvent((long) updatedReview.getUserId(), EventType.REVIEW, Operation.UPDATE, (long) updatedReview.getReviewId());
        return updatedReview;
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

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Содержание отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Поле isPositive обязательно");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("UserId обязательно");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("FilmId обязательно");
        }
    }
}
