package com.lafl.notification.event;

import com.lafl.notification.service.NotificationDispatchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationEventHandlerTest {

    @Test
    void listenersDelegateToDispatchService() {
        NotificationDispatchService dispatchService = Mockito.mock(NotificationDispatchService.class);
        NotificationEventHandler handler = new NotificationEventHandler(dispatchService);

        handler.onQuoteSubmitted("""
            {
              "eventType":"quote.submitted",
              "timestamp":"2026-03-28T12:00:00Z",
              "quoteId":"quote-1",
              "email":"ops@example.com",
              "company":"LAFL",
              "serviceType":"Air Freight",
              "origin":"JFK",
              "destination":"LHR",
              "status":"Pending Review"
            }
            """);
        handler.onContactSubmitted("""
            {
              "eventType":"contact.submitted",
              "timestamp":"2026-03-28T12:05:00Z",
              "contactId":"msg-1",
              "email":"contact@example.com",
              "name":"Akshay",
              "company":"LAFL",
              "message":"Need callback"
            }
            """);
        handler.onUserRegistered("""
            {
              "eventType":"user.registered",
              "timestamp":"2026-03-28T12:10:00Z",
              "userId":"user-1",
              "email":"user@example.com",
              "name":"User",
              "company":"LAFL"
            }
            """);
        handler.onShipmentUpdated("""
            {
              "eventType":"shipment.status.updated",
              "timestamp":"2026-03-28T12:15:00Z",
              "shipmentReference":"LAFL-24017",
              "status":"Delayed",
              "currentLocation":"Antwerp Hub",
              "progress":63
            }
            """);

        Mockito.verify(dispatchService).handleQuoteSubmitted(Mockito.argThat(event ->
            "quote.submitted".equals(event.eventType()) && "quote-1".equals(event.quoteId())));
        Mockito.verify(dispatchService).handleContactSubmitted(Mockito.argThat(event ->
            "contact.submitted".equals(event.eventType()) && "msg-1".equals(event.contactId())));
        Mockito.verify(dispatchService).handleUserRegistered(Mockito.argThat(event ->
            "user.registered".equals(event.eventType()) && "user-1".equals(event.userId())));
        Mockito.verify(dispatchService).handleShipmentStatusUpdated(Mockito.argThat(event ->
            "shipment.status.updated".equals(event.eventType())
                && "LAFL-24017".equals(event.shipmentReference())));
    }
}
