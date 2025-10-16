package ru.yandex.practicum.filmorate.exception;

public class DuplicateException extends RuntimeException {
    private String message;

    public DuplicateException(String message) {
        super(message);
    }
}
