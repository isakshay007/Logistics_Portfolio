package com.lafl.notification.event;

import com.lafl.notification.service.NotificationSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest",
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@EmbeddedKafka(partitions = 1, topics = {
    "user.registered",
    "quote.submitted",
    "contact.submitted",
    "shipment.status.updated"
})
class NotificationKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private NotificationSender notificationSender;

    @Test
    void consumesAllTopicsAndRoutesEachToCorrectSenderMethod() {
        kafkaTemplate.send("user.registered", "user-1", """
            {"eventType":"user.registered","timestamp":"2026-03-28T12:00:00Z","userId":"user-1","email":"user@example.com","name":"User","company":"LAFL"}
            """);
        kafkaTemplate.send("quote.submitted", "quote-1", """
            {"eventType":"quote.submitted","timestamp":"2026-03-28T12:01:00Z","quoteId":"quote-1","email":"quote@example.com","company":"LAFL","serviceType":"Air Freight","origin":"JFK","destination":"LHR","status":"Pending Review"}
            """);
        kafkaTemplate.send("contact.submitted", "msg-1", """
            {"eventType":"contact.submitted","timestamp":"2026-03-28T12:02:00Z","contactId":"msg-1","email":"contact@example.com","name":"Akshay","company":"LAFL","message":"Need callback"}
            """);
        kafkaTemplate.send("shipment.status.updated", "LAFL-24017", """
            {"eventType":"shipment.status.updated","timestamp":"2026-03-28T12:03:00Z","shipmentReference":"LAFL-24017","status":"Delayed","currentLocation":"Antwerp Hub","progress":63}
            """);

        verify(notificationSender, timeout(10_000)).sendUserRegistered(argThat(event ->
            "user.registered".equals(event.eventType()) && "user-1".equals(event.userId())));
        verify(notificationSender, timeout(10_000)).sendQuoteSubmitted(argThat(event ->
            "quote.submitted".equals(event.eventType()) && "quote-1".equals(event.quoteId())));
        verify(notificationSender, timeout(10_000)).sendContactSubmitted(argThat(event ->
            "contact.submitted".equals(event.eventType()) && "msg-1".equals(event.contactId())));
        verify(notificationSender, timeout(10_000)).sendShipmentStatusUpdated(argThat(event ->
            "shipment.status.updated".equals(event.eventType())
                && "LAFL-24017".equals(event.shipmentReference())));
    }
}
