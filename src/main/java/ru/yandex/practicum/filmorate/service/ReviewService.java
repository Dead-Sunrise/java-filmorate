package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.DuplicateException;
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

    public Review create(Review review) {
        return storage.create(review);
    }

    public boolean delete(int id) {
        return storage.delete(id);
    }

    public Review update(Review review) {
        Review oldReview = findById(review.getId());
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
        int rowsAffected = 0;
        try {
            rowsAffected = storage.addLikeOrDislike(reviewId, userId, isLike);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException("Пользователь уже оценил отзыв");
        }
        if (rowsAffected <= 0) {
            throw new InternalServerException("Не удалось поставить лайк/дизлайк");
        }
        rowsAffected = storage.changeUseful(review, isLike);
        if (rowsAffected <= 0) {
            storage.deleteLikeOrDislike(reviewId, userId);
            throw new InternalServerException("Не удалось поставить лайк/дизлайк");
        }
    }

    public void deleteLikeOrDislike(int reviewId, int userId) {
        boolean success = storage.deleteLikeOrDislike(reviewId, userId);
        if (!success) throw new InternalServerException("Не удалось удалить лайк/дизлайк");
    }

}
