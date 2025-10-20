package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {
    List<Event> getFeedByUserId(Long userId);
    Event addEvent(Event event);
}