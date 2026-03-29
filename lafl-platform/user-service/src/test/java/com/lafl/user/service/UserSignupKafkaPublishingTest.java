package com.lafl.user.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.user.api.SignupRequest;
import com.lafl.user.domain.UserAccount;
import com.lafl.user.event.KafkaUserEventPublisher;
import com.lafl.user.event.UserEventPublisher;
import com.lafl.user.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserSignupKafkaPublishingTest {

    @Test
    void signupPublishesUserRegisteredKafkaEvent() throws Exception {
        UserAccountRepository repository = mock(UserAccountRepository.class);
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = new ObjectMapper();
        UserEventPublisher eventPublisher = new KafkaUserEventPublisher(
            kafkaTemplate,
            objectMapper,
            "user.registered"
        );
        UserAuthService service = new UserAuthService(
            new BCryptPasswordEncoder(),
            (subject, roles) -> "token-for-" + subject,
            repository,
            eventPublisher
        );

        when(repository.existsByEmail("akshay@example.com")).thenReturn(false);
        when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.signup(new SignupRequest(
            "Akshay User", "akshay@example.com", "LAFL", "+1", "Shipment Visibility", "password123"
        ));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("user.registered"), keyCaptor.capture(), payloadCaptor.capture());

        JsonNode payload = objectMapper.readTree(payloadCaptor.getValue());
        assertEquals("user.registered", payload.get("eventType").asText());
        assertEquals(keyCaptor.getValue(), payload.get("userId").asText());
        assertEquals("akshay@example.com", payload.get("email").asText());
        assertEquals("Akshay User", payload.get("name").asText());
    }
}
