package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genres.GenreStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class GenreService {

    private final GenreStorage genreStorage;

    public List<Genre> getAllGenres() {
        return genreStorage.findAll().stream().toList();
    }

    public Genre getGenreById(Long id) {
        return genreStorage.getGenreById(id).orElseThrow(() -> new NotFoundException("Жанр по id не найден"));
    }
}
