package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.service.MPAService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MPAController {

    private final MPAService mpaService;

    @GetMapping
    public Collection<RatingMPA> getAllMpa() {
        log.info("/mpa GET Запрос на получение всех mpa");
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public RatingMPA getMpaById(@PathVariable Long id) {
        log.info("/mpa/{id} GET Запрос на получение данных конкретного mpa по id");
        return mpaService.getMpaById(id);
    }
}