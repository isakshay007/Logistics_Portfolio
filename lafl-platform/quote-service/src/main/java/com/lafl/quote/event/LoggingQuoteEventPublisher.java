package com.lafl.quote.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.quote.domain.ContactRecord;
import com.lafl.quote.domain.QuoteRecord;
import com.lafl.quote.events.ContactSubmittedEvent;
import com.lafl.quote.events.QuoteSubmittedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class LoggingQuoteEventPublisher implements QuoteEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(LoggingQuoteEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String quoteSubmittedTopic;
    private final String contactSubmittedTopic;

    public LoggingQuoteEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                      ObjectMapper objectMapper,
                                      @Value("${lafl.kafka.topic.quote-submitted:quote.submitted}") String quoteSubmittedTopic,
                                      @Value("${lafl.kafka.topic.contact-submitted:contact.submitted}") String contactSubmittedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.quoteSubmittedTopic = quoteSubmittedTopic;
        this.contactSubmittedTopic = contactSubmittedTopic;
    }

    @Override
    public void publishQuoteSubmitted(QuoteRecord quoteRecord) {
        QuoteSubmittedEvent event = new QuoteSubmittedEvent(
            "quote.submitted",
            quoteRecord.getCreatedAt(),
            quoteRecord.getId(),
            quoteRecord.getEmail(),
            quoteRecord.getCompany(),
            quoteRecord.getServiceType(),
            quoteRecord.getOrigin(),
            quoteRecord.getDestination(),
            quoteRecord.getStatus()
        );
        send(quoteSubmittedTopic, quoteRecord.getId(), event);
    }

    @Override
    public void publishContactSubmitted(ContactRecord contactRecord) {
        ContactSubmittedEvent event = new ContactSubmittedEvent(
            "contact.submitted",
            contactRecord.getCreatedAt(),
            contactRecord.getId(),
            contactRecord.getEmail(),
            contactRecord.getName(),
            contactRecord.getCompany(),
            contactRecord.getMessage()
        );
        send(contactSubmittedTopic, contactRecord.getId(), event);
    }

    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            logger.error("Unable to serialize event payload for topic {}", topic, exception);
        } catch (RuntimeException exception) {
            logger.warn("Kafka publish skipped for topic {} and key {}: {}", topic, key, exception.getMessage());
        }
    }
}
