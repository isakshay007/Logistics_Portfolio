package com.lafl.notification.events;

public record ShipmentStatusUpdatedEvent(
    String eventType,
    String timestamp,
    String shipmentReference,
    String status,
    String currentLocation,
    int progress
) {
}
