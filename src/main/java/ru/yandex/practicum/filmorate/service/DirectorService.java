package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director createDirector(Director director) {
        validate(director);
        return directorStorage.add(director);
    }

    public Director updateDirector(Director director) {
        getById(director.getId());
        validate(director);
        return directorStorage.update(director);
    }

    public Director getById(int id) {
        return directorStorage.getById(id).orElseThrow(() -> new NotFoundException("Режиссёр не найден: " + id));
    }

    public Collection<Director> getAll() {
        return directorStorage.getAll();
    }

    public void deleteDirector(int id) {
        getById(id);
        directorStorage.delete(id);
    }

    private void validate(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым");
        }
    }
}