package com.lafl.notification.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.notification.events.ContactSubmittedEvent;
import com.lafl.notification.events.QuoteSubmittedEvent;
import com.lafl.notification.events.ShipmentStatusUpdatedEvent;
import com.lafl.notification.events.UserRegisteredEvent;
import com.lafl.notification.service.NotificationDispatchService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventHandler {

    private final NotificationDispatchService dispatchService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NotificationEventHandler(NotificationDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @KafkaListener(topics = "user.registered", groupId = "notification-service")
    public void onUserRegistered(String payload) {
        dispatchService.handleUserRegistered(read(payload, UserRegisteredEvent.class));
    }

    @KafkaListener(topics = "quote.submitted", groupId = "notification-service")
    public void onQuoteSubmitted(String payload) {
        dispatchService.handleQuoteSubmitted(read(payload, QuoteSubmittedEvent.class));
    }

    @KafkaListener(topics = "contact.submitted", groupId = "notification-service")
    public void onContactSubmitted(String payload) {
        dispatchService.handleContactSubmitted(read(payload, ContactSubmittedEvent.class));
    }

    @KafkaListener(topics = "shipment.status.updated", groupId = "notification-service")
    public void onShipmentUpdated(String payload) {
        dispatchService.handleShipmentStatusUpdated(read(payload, ShipmentStatusUpdatedEvent.class));
    }

    private <T> T read(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize Kafka payload for " + type.getSimpleName(),
                exception);
        }
    }
}
