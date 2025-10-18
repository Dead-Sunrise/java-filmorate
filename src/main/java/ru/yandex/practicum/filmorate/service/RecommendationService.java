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

        Map<Long, List<Long>> userLikesMap = userStorage.getAllUserLikes();
        List<Long> likedFilms = userLikesMap.getOrDefault(userId, List.of());

        Long similarUserId = null;
        int maxIntersection = 0;

        for (Map.Entry<Long, List<Long>> entry : userLikesMap.entrySet()) {
            Long otherUserId = entry.getKey();
            if (Objects.equals(userId, otherUserId))
                continue;

            List<Long> otherLikes = entry.getValue();
            Set<Long> intersection = new HashSet<>(likedFilms);
            intersection.retainAll(otherLikes);

            if (intersection.size() > maxIntersection) {
                maxIntersection = intersection.size();
                similarUserId = otherUserId;
            }
        }

        if (similarUserId == null) {
            return List.of();
        }

        Set<Long> unseenLikedFilms = new HashSet<>(userLikesMap.get(similarUserId));
        likedFilms.forEach(unseenLikedFilms::remove);

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
