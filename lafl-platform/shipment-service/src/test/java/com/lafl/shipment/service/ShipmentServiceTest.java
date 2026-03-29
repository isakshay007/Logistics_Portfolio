package com.lafl.shipment.service;

import com.lafl.shipment.api.ShipmentStatusUpdateRequest;
import com.lafl.shipment.domain.Shipment;
import com.lafl.shipment.event.KafkaShipmentEventPublisher;
import com.lafl.shipment.event.ShipmentEventPublisher;
import com.lafl.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShipmentServiceTest {

    @Test
    void findByReferenceDelegatesToRepository() {
        ShipmentRepository repository = mock(ShipmentRepository.class);
        ShipmentEventPublisher publisher = mock(ShipmentEventPublisher.class);
        ShipmentService service = new ShipmentService(repository, publisher);

        Shipment shipment = new Shipment();
        shipment.setReference("LAFL-98241");
        shipment.setStatus("Customs Review");

        when(repository.findByReferenceIgnoreCase("lafl-98241")).thenReturn(Optional.of(shipment));

        Optional<Shipment> found = service.findByReference("lafl-98241");

        assertTrue(found.isPresent());
        assertEquals("Customs Review", found.get().getStatus());
    }

    @Test
    void updateStatusSavesAndPublishesEvent() {
        ShipmentRepository repository = mock(ShipmentRepository.class);
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        ShipmentEventPublisher publisher = new KafkaShipmentEventPublisher(
            kafkaTemplate,
            new com.fasterxml.jackson.databind.ObjectMapper(),
            "shipment.status.updated"
        );
        ShipmentService service = new ShipmentService(repository, publisher);

        Shipment shipment = new Shipment();
        shipment.setReference("LAFL-24017");
        shipment.setStatus("On Schedule");
        shipment.setCurrentLocation("Rotterdam");
        shipment.setProgress(72);

        when(repository.findByReferenceIgnoreCase("LAFL-24017")).thenReturn(Optional.of(shipment));
        when(repository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<Shipment> updated = service.updateStatus("LAFL-24017",
            new ShipmentStatusUpdateRequest("Delayed", "Antwerp Hub", 63, "Weather disruption"));

        assertTrue(updated.isPresent());
        assertEquals("Delayed", updated.get().getStatus());
        verify(repository).save(any(Shipment.class));

        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq("shipment.status.updated"), eq("LAFL-24017"), payloadCaptor.capture());
        assertTrue(payloadCaptor.getValue().contains("\"eventType\":\"shipment.status.updated\""));
        assertTrue(payloadCaptor.getValue().contains("\"shipmentReference\":\"LAFL-24017\""));
        assertTrue(payloadCaptor.getValue().contains("\"status\":\"Delayed\""));
    }
}
