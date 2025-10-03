package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    Long id;
    @Email
    @NotNull
    @NotBlank
    String email;
    @Pattern(regexp = "\\S+")
    @NotNull
    @NotBlank
    String login;
    String name;
    @PastOrPresent
    LocalDate birthday;
    Set<Long> friends = new HashSet<>();
}
