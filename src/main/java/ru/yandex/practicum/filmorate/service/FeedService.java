package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;

    public List<Event> getFeedByUserId(Long userId) {
        return feedStorage.getFeedByUserId(userId);
    }

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        Event event = Event.builder()
                .timestamp(Instant.now().toEpochMilli())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();
        feedStorage.addEvent(event);
    }
}