package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.RatingMPA;

import java.util.Collection;
import java.util.Optional;

public interface MPAStorage {
    public Collection<RatingMPA> findAll();

    public Optional<RatingMPA> getMPAById(Long id);
}