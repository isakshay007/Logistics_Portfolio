package com.lafl.notification.service;

import com.lafl.notification.events.ContactSubmittedEvent;
import com.lafl.notification.events.QuoteSubmittedEvent;
import com.lafl.notification.events.ShipmentStatusUpdatedEvent;
import com.lafl.notification.events.UserRegisteredEvent;

public interface NotificationSender {

    void sendUserRegistered(UserRegisteredEvent event);

    void sendQuoteSubmitted(QuoteSubmittedEvent event);

    void sendContactSubmitted(ContactSubmittedEvent event);

    void sendShipmentStatusUpdated(ShipmentStatusUpdatedEvent event);
}
