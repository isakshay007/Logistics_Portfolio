package com.lafl.quote.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.quote.api.ContactCreateRequest;
import com.lafl.quote.api.QuoteCreateRequest;
import com.lafl.quote.domain.ContactRecord;
import com.lafl.quote.domain.QuoteRecord;
import com.lafl.quote.event.LoggingQuoteEventPublisher;
import com.lafl.quote.event.QuoteEventPublisher;
import com.lafl.quote.repository.ContactRecordRepository;
import com.lafl.quote.repository.IssueSnapshotRepository;
import com.lafl.quote.repository.QuoteRecordRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuoteOpsServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createQuotePublishesKafkaEvent() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        QuoteRecordRepository quoteRepository = mock(QuoteRecordRepository.class);
        ContactRecordRepository contactRepository = mock(ContactRecordRepository.class);
        IssueSnapshotRepository issueRepository = mock(IssueSnapshotRepository.class);
        when(quoteRepository.save(any(QuoteRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteEventPublisher publisher = new LoggingQuoteEventPublisher(
            kafkaTemplate,
            objectMapper,
            "quote.submitted",
            "contact.submitted"
        );
        QuoteOpsService service = new QuoteOpsService(publisher, quoteRepository, contactRepository, issueRepository);

        service.createQuote(new QuoteCreateRequest(
            "LAFL", "Akshay", "akshay@example.com", "Air Freight", "JFK", "LHR", "Boxes", "High priority"
        ));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("quote.submitted"), keyCaptor.capture(), payloadCaptor.capture());

        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertTrue(keyCaptor.getValue().startsWith("quote-"));
        assertEquals("quote.submitted", payload.get("eventType").asText());
        assertEquals(keyCaptor.getValue(), payload.get("quoteId").asText());
        assertEquals("akshay@example.com", payload.get("email").asText());
    }

    @Test
    void createContactPublishesKafkaEvent() throws Exception {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        QuoteRecordRepository quoteRepository = mock(QuoteRecordRepository.class);
        ContactRecordRepository contactRepository = mock(ContactRecordRepository.class);
        IssueSnapshotRepository issueRepository = mock(IssueSnapshotRepository.class);
        when(contactRepository.save(any(ContactRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        QuoteEventPublisher publisher = new LoggingQuoteEventPublisher(
            kafkaTemplate,
            objectMapper,
            "quote.submitted",
            "contact.submitted"
        );
        QuoteOpsService service = new QuoteOpsService(publisher, quoteRepository, contactRepository, issueRepository);

        service.createContact(new ContactCreateRequest(
            "Akshay", "akshay@example.com", "LAFL", "Need urgent callback"
        ));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("contact.submitted"), keyCaptor.capture(), payloadCaptor.capture());

        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertTrue(keyCaptor.getValue().startsWith("msg-"));
        assertEquals("contact.submitted", payload.get("eventType").asText());
        assertEquals(keyCaptor.getValue(), payload.get("contactId").asText());
        assertEquals("akshay@example.com", payload.get("email").asText());
    }
}
