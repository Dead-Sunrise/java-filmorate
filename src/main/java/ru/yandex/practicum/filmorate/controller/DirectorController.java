package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getAll() {
        return directorService.getAll();
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        return directorService.getById(id);
    }

    @PostMapping
    public Director create(@RequestBody Director director) {
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        directorService.deleteDirector(id);
    }
}