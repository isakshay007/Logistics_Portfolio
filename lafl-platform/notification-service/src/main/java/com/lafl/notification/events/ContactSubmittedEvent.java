package com.lafl.notification.events;

public record ContactSubmittedEvent(
    String eventType,
    String timestamp,
    String contactId,
    String email,
    String name,
    String company,
    String message
) {
}
