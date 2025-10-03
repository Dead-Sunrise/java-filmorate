package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.mpa.MPADbStorage;

import java.util.Collection;

@Service
@AllArgsConstructor
public class MPAService {

    private final MPADbStorage mpaDbStorage;

    public Collection<RatingMPA> getAllMpa() {
        return mpaDbStorage.findAll();
    }

    public RatingMPA getMpaById(Long id) {
        return mpaDbStorage.getMPAById(id).orElseThrow(() -> new NotFoundException("Рейтинг по id не найден"));
    }
}
