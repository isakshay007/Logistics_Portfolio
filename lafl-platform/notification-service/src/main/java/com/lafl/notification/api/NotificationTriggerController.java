package com.lafl.notification.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.notification.events.ContactSubmittedEvent;
import com.lafl.notification.events.QuoteSubmittedEvent;
import com.lafl.notification.events.ShipmentStatusUpdatedEvent;
import com.lafl.notification.events.UserRegisteredEvent;
import com.lafl.notification.service.NotificationDispatchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationTriggerController {

    private final NotificationDispatchService dispatchService;
    private final ObjectMapper objectMapper;

    public NotificationTriggerController(NotificationDispatchService dispatchService, ObjectMapper objectMapper) {
        this.dispatchService = dispatchService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map<String, String>> trigger(@RequestBody NotificationTriggerRequest request) {
        if (request == null || request.eventType() == null || request.eventType().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "eventType is required"));
        }
        if (request.payload() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "payload is required"));
        }

        String eventType = request.eventType().trim().toLowerCase(Locale.ROOT);
        JsonNode payload = request.payload();

        try {
            switch (eventType) {
                case "user.registered" -> dispatchService.handleUserRegistered(
                    objectMapper.treeToValue(payload, UserRegisteredEvent.class));
                case "quote.submitted" -> dispatchService.handleQuoteSubmitted(
                    objectMapper.treeToValue(payload, QuoteSubmittedEvent.class));
                case "contact.submitted" -> dispatchService.handleContactSubmitted(
                    objectMapper.treeToValue(payload, ContactSubmittedEvent.class));
                case "shipment.status.updated" -> dispatchService.handleShipmentStatusUpdated(
                    objectMapper.treeToValue(payload, ShipmentStatusUpdatedEvent.class));
                default -> {
                    return ResponseEntity.badRequest().body(Map.of("error", "Unsupported eventType: " + request.eventType()));
                }
            }
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid payload for eventType " + request.eventType()));
        }

        return ResponseEntity.ok(Map.of("status", "triggered", "eventType", eventType));
    }
}
