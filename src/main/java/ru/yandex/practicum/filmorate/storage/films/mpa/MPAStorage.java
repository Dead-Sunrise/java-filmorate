package ru.yandex.practicum.filmorate.storage.films.mpa;

import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.Collection;
import java.util.Optional;

public interface MPAStorage {
    Collection<RatingMPA> findAll();

    Optional<RatingMPA> getMPAById(Long id);
}