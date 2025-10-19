package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService service;

    @GetMapping("/{id}")
    public Review findOne(@PathVariable int id) {
        log.info("GET запрос на получение отзыва с id = {}", id);
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("DELETE запрос на удаление отзыва с id = {}", id);
        service.delete(id);
    }

    @PostMapping
    public Review create(@RequestBody Review review) {
        log.info("POST запрос на создание нового отзыва");
        return service.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        log.info("PUT запрос на обновление отзыва");
        return service.update(review);
    }

    @GetMapping
    public List<Review> findAll(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10")@Positive int count) {
        if (filmId != null) {
            log.info("GET запрос на получение отзывов к фильму с id = {} в количестве = {}", filmId, count);
            return service.findByFilmId(filmId, count);
        }
        log.info("GET запрос на получение всех отзывов в количестве = {}", count);
        return service.findAll(count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("PUT запрос на добавление лайка к фильму с id = {} от пользователя с id = {}", id, userId);
        service.addLikeOrDislike(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("PUT запрос на добавление дизлайка к фильму с id = {} от пользователя с id = {}", id, userId);
        service.addLikeOrDislike(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        log.info("DELETE запрос на удаление лайка к фильму с id = {} от пользователя с id = {}", id, userId);
        service.deleteLikeOrDislike(id, userId, true);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        log.info("DELETE запрос на удаление дизлайка к фильму с id = {} от пользователя с id = {}", id, userId);
        service.deleteLikeOrDislike(id, userId, false);
    }
}
