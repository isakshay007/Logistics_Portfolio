package com.lafl.shipment.events;

public record ShipmentStatusUpdatedEvent(
    String eventType,
    String timestamp,
    String shipmentReference,
    String status,
    String currentLocation,
    int progress
) {
}
