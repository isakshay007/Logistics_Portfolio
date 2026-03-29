package com.lafl.notification.service;

import com.lafl.notification.events.ContactSubmittedEvent;
import com.lafl.notification.events.QuoteSubmittedEvent;
import com.lafl.notification.events.ShipmentStatusUpdatedEvent;
import com.lafl.notification.events.UserRegisteredEvent;
import org.springframework.stereotype.Service;

@Service
public class NotificationDispatchService {

    private final NotificationSender notificationSender;

    public NotificationDispatchService(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    public void handleUserRegistered(UserRegisteredEvent event) {
        notificationSender.sendUserRegistered(event);
    }

    public void handleQuoteSubmitted(QuoteSubmittedEvent event) {
        notificationSender.sendQuoteSubmitted(event);
    }

    public void handleContactSubmitted(ContactSubmittedEvent event) {
        notificationSender.sendContactSubmitted(event);
    }

    public void handleShipmentStatusUpdated(ShipmentStatusUpdatedEvent event) {
        notificationSender.sendShipmentStatusUpdated(event);
    }
}
