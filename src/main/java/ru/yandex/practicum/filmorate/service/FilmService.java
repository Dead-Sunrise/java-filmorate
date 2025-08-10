package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void putLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);
        film.getLikes().add(user.getId());
        filmStorage.update(film);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (!film.getLikes().contains(userId)) {
            throw new NotFoundException("Лайк не поставлен.");
        }
        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public Collection<Film> getPopularFilms(int count) {
        return filmStorage.findAll()
                .stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.update(newFilm);
    }
}
