package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    Long id;
    @NotNull
    @NotBlank
    String name;
    @Size(max = 200)
    String description;
    LocalDate releaseDate;
    @Positive
    Integer duration;
    Set<Long> likes = new HashSet<>();
}
