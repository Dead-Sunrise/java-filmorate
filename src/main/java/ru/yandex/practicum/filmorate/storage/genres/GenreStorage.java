package ru.yandex.practicum.filmorate.storage.genres;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {
    public Collection<Genre> findAll();

    public Optional<Genre> getGenreById(Long id);
}
