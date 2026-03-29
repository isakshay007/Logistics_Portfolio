package com.lafl.user.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.user.domain.UserAccount;
import com.lafl.user.events.UserRegisteredEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class KafkaUserEventPublisher implements UserEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaUserEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${lafl.kafka.topic.user-registered:user.registered}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publishUserRegistered(UserAccount userAccount) {
        UserRegisteredEvent event = new UserRegisteredEvent(
            "user.registered",
            Instant.now().toString(),
            userAccount.getId(),
            userAccount.getEmail(),
            userAccount.getName(),
            userAccount.getCompany()
        );
        try {
            kafkaTemplate.send(topic, userAccount.getId(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize user registered event", exception);
        }
    }
}
