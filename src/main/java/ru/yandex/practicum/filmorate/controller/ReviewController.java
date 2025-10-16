package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService service;

    @GetMapping("/{id}")
    public Review findOne(@PathVariable int id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        service.delete(id);
    }

    @PostMapping
    public Review create(@RequestBody Review review) {
        return service.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return service.update(review);
    }

    @GetMapping
    public List<Review> findAll(
            @RequestParam(required = false) Integer filmId,
            @RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            count = 10;
        }
        if (filmId != null) {
            return service.findByFilmId(filmId, count);
        }
        return service.findAll(count);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        service.addLikeOrDislike(id, userId, true);
    }

    @PutMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        service.addLikeOrDislike(id, userId, false);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        service.deleteLikeOrDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        service.deleteLikeOrDislike(id, userId);
    }
}
