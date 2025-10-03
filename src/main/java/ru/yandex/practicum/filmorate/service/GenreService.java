package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genres.GenreDbStorage;

import java.util.Collection;

@Service
@AllArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public Collection<Genre> getAllGenres() {
        return genreDbStorage.findAll();
    }

    public Genre getGenreById(Long id) {
        return genreDbStorage.getGenreById(id).orElseThrow(() -> new NotFoundException("Жанр по id не найден"));
    }
}
