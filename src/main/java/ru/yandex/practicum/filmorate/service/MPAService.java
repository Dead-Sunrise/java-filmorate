package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.RatingMPA;
import ru.yandex.practicum.filmorate.storage.films.mpa.MPAStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class MPAService {

    private final MPAStorage mpaStorage;

    public List<RatingMPA> getAllMpa() {
        return mpaStorage.findAll().stream().toList();
    }

    public RatingMPA getMpaById(Long id) {
        return mpaStorage.getMPAById(id).orElseThrow(() -> new NotFoundException("Рейтинг по id не найден"));
    }
}
