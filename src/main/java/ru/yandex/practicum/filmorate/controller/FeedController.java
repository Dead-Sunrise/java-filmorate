package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;

    @GetMapping("/{id}/feed")
    public List<Event> getFeed(@PathVariable Long id) {
        log.info("GET запрос на получение ленты событий пользователя с id = {}", id);
        userService.getUserById(id);
        return feedService.getFeedByUserId(id);
    }
}