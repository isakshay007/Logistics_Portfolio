package com.lafl.notification.service;

import com.lafl.notification.events.ContactSubmittedEvent;
import com.lafl.notification.events.QuoteSubmittedEvent;
import com.lafl.notification.events.ShipmentStatusUpdatedEvent;
import com.lafl.notification.events.UserRegisteredEvent;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationDispatchServiceTest {

    @Test
    void handleUserRegisteredRoutesToSender() {
        NotificationSender sender = mock(NotificationSender.class);
        NotificationDispatchService service = new NotificationDispatchService(sender);
        UserRegisteredEvent event = new UserRegisteredEvent(
            "user.registered", "2026-03-28T12:00:00Z", "user-1", "user@example.com", "User", "LAFL");

        service.handleUserRegistered(event);

        verify(sender).sendUserRegistered(event);
    }

    @Test
    void handleQuoteSubmittedRoutesToSender() {
        NotificationSender sender = mock(NotificationSender.class);
        NotificationDispatchService service = new NotificationDispatchService(sender);
        QuoteSubmittedEvent event = new QuoteSubmittedEvent(
            "quote.submitted", "2026-03-28T12:00:00Z", "quote-1", "quote@example.com", "LAFL",
            "Air Freight", "JFK", "LHR", "Pending Review");

        service.handleQuoteSubmitted(event);

        verify(sender).sendQuoteSubmitted(event);
    }

    @Test
    void handleContactSubmittedRoutesToSender() {
        NotificationSender sender = mock(NotificationSender.class);
        NotificationDispatchService service = new NotificationDispatchService(sender);
        ContactSubmittedEvent event = new ContactSubmittedEvent(
            "contact.submitted", "2026-03-28T12:05:00Z", "msg-1", "contact@example.com",
            "Akshay", "LAFL", "Need callback");

        service.handleContactSubmitted(event);

        verify(sender).sendContactSubmitted(event);
    }

    @Test
    void handleShipmentStatusUpdatedRoutesToSender() {
        NotificationSender sender = mock(NotificationSender.class);
        NotificationDispatchService service = new NotificationDispatchService(sender);
        ShipmentStatusUpdatedEvent event = new ShipmentStatusUpdatedEvent(
            "shipment.status.updated", "2026-03-28T12:10:00Z", "LAFL-24017", "Delayed", "Antwerp Hub", 63);

        service.handleShipmentStatusUpdated(event);

        verify(sender).sendShipmentStatusUpdated(event);
    }
}
