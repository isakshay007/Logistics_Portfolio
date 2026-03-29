package com.lafl.shipment.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lafl.shipment.domain.Shipment;
import com.lafl.shipment.events.ShipmentStatusUpdatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaShipmentEventPublisher implements ShipmentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaShipmentEventPublisher(KafkaTemplate<String, String> kafkaTemplate,
                                       ObjectMapper objectMapper,
                                       @Value("${lafl.kafka.topic.shipment-status-updated:shipment.status.updated}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publishStatusUpdated(Shipment shipment) {
        try {
            ShipmentStatusUpdatedEvent event = new ShipmentStatusUpdatedEvent(
                "shipment.status.updated",
                shipment.getLastUpdated(),
                shipment.getReference(),
                shipment.getStatus(),
                shipment.getCurrentLocation(),
                shipment.getProgress()
            );
            kafkaTemplate.send(topic, shipment.getReference(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize shipment event", exception);
        }
    }
}
