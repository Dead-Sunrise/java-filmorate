package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getAll() {
        log.info("GET-запрос: получение всех режиссёров");
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        log.info("GET-запрос: получение режиссёра с id={}", id);
        return directorService.getById(id);
    }

    @PostMapping
    public Director create(@RequestBody Director director) {
        log.info("POST-запрос: добавление нового режиссёра — {}", director);
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.info("PUT-запрос: обновление данных режиссёра — {}", director);
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("DELETE-запрос: удаление режиссёра с id={}", id);
        directorService.deleteDirector(id);
    }
}