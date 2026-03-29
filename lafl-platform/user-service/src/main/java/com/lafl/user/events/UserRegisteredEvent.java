package com.lafl.user.events;

public record UserRegisteredEvent(
    String eventType,
    String timestamp,
    String userId,
    String email,
    String name,
    String company
) {
}
