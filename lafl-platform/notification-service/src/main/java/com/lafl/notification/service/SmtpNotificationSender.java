package com.lafl.notification.service;

import com.lafl.notification.events.ContactSubmittedEvent;
import com.lafl.notification.events.QuoteSubmittedEvent;
import com.lafl.notification.events.ShipmentStatusUpdatedEvent;
import com.lafl.notification.events.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SmtpNotificationSender implements NotificationSender {

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String mailTo;

    public SmtpNotificationSender(JavaMailSender mailSender,
                                  @Value("${lafl.mail.from}") String mailFrom,
                                  @Value("${lafl.mail.to}") String mailTo) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
    }

    @Override
    public void sendUserRegistered(UserRegisteredEvent event) {
        send(
            "LAFL User Registered: " + event.userId(),
            "A new user account was created.\n\n"
                + "Name: " + event.name() + "\n"
                + "Email: " + event.email() + "\n"
                + "Company: " + event.company() + "\n"
                + "Timestamp: " + event.timestamp()
        );
    }

    @Override
    public void sendQuoteSubmitted(QuoteSubmittedEvent event) {
        send(
            "LAFL Quote Submitted: " + event.quoteId(),
            "A new quote request was submitted.\n\n"
                + "Company: " + event.company() + "\n"
                + "Contact Email: " + event.email() + "\n"
                + "Service Type: " + event.serviceType() + "\n"
                + "Route: " + event.origin() + " -> " + event.destination() + "\n"
                + "Status: " + event.status() + "\n"
                + "Timestamp: " + event.timestamp()
        );
    }

    @Override
    public void sendContactSubmitted(ContactSubmittedEvent event) {
        send(
            "LAFL Contact Submitted: " + event.contactId(),
            "A new contact request was submitted.\n\n"
                + "Name: " + event.name() + "\n"
                + "Email: " + event.email() + "\n"
                + "Company: " + event.company() + "\n"
                + "Message: " + event.message() + "\n"
                + "Timestamp: " + event.timestamp()
        );
    }

    @Override
    public void sendShipmentStatusUpdated(ShipmentStatusUpdatedEvent event) {
        send(
            "LAFL Shipment Status Updated: " + event.shipmentReference(),
            "A tracked shipment changed status.\n\n"
                + "Reference: " + event.shipmentReference() + "\n"
                + "Status: " + event.status() + "\n"
                + "Location: " + event.currentLocation() + "\n"
                + "Progress: " + event.progress() + "%\n"
                + "Timestamp: " + event.timestamp()
        );
    }

    private void send(String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(mailTo);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
