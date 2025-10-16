package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.films.FilmStorage;
import ru.yandex.practicum.filmorate.storage.users.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public List<Film> getRecommendations(Long userId) {
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Map<Long, Set<Long>> userLikesMap = userStorage.getAllUserLikes();
        Set<Long> likedFilms = userLikesMap.getOrDefault(userId, Set.of());

        Long similarUserId = null;
        int maxIntersection = 0;

        for (Map.Entry<Long, Set<Long>> entry : userLikesMap.entrySet()) {
            Long otherUserId = entry.getKey();
            if (Objects.equals(userId, otherUserId))
                continue;

            Set<Long> otherLikes = entry.getValue();
            Set<Long> intersection = new HashSet<>(likedFilms);
            intersection.retainAll(otherLikes);

            if (intersection.size() > maxIntersection) {
                maxIntersection = intersection.size();
                similarUserId = otherUserId;
            }
        }

        if (similarUserId == null || maxIntersection == 0) {
            return List.of();
        }

        Set<Long> unseenLikedFilms = new HashSet<>(userLikesMap.get(similarUserId));
        unseenLikedFilms.removeAll(likedFilms);

        return unseenLikedFilms.stream()
                .map(filmStorage::getFilmById)
                .flatMap(Optional::stream)
                .filter(film -> film.getName() != null &&
                        film.getDescription() != null &&
                        film.getReleaseDate() != null &&
                        film.getDuration() != null)
                .collect(Collectors.toList());
    }
}
