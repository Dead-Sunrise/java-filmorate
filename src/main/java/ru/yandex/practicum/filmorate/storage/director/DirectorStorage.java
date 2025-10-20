package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {

    Director add(Director director);

    Director update(Director director);

    Optional<Director> getById(int id);

    Collection<Director> getAll();

    void delete(int id);
}