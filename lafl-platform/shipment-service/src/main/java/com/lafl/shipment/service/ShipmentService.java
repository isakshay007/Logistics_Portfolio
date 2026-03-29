package com.lafl.shipment.service;

import com.lafl.shipment.api.ShipmentStatusUpdateRequest;
import com.lafl.shipment.domain.Shipment;
import com.lafl.shipment.domain.TrackingEvent;
import com.lafl.shipment.event.ShipmentEventPublisher;
import com.lafl.shipment.repository.ShipmentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final ShipmentEventPublisher eventPublisher;

    public ShipmentService(ShipmentRepository shipmentRepository, ShipmentEventPublisher eventPublisher) {
        this.shipmentRepository = shipmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Cacheable(value = "shipments", key = "#reference")
    @Transactional(readOnly = true)
    public Optional<Shipment> findByReference(String reference) {
        return shipmentRepository.findByReferenceIgnoreCase(reference.trim())
            .map(shipment -> {
                // Initialize both collections while the transaction is open to avoid lazy-loading errors.
                shipment.getEvents().size();
                shipment.getIssues().size();
                return shipment;
            });
    }

    @CacheEvict(value = "shipments", key = "#reference")
    @Transactional
    public Optional<Shipment> updateStatus(String reference, ShipmentStatusUpdateRequest request) {
        Optional<Shipment> optionalShipment = shipmentRepository.findByReferenceIgnoreCase(reference.trim());
        if (optionalShipment.isEmpty()) {
            return Optional.empty();
        }

        Shipment shipment = optionalShipment.get();
        shipment.setStatus(request.status());
        shipment.setCurrentLocation(request.currentLocation());
        shipment.setProgress(request.progress());
        shipment.setLastUpdated(Instant.now().toString());
        shipment.addEvent(new TrackingEvent(
            request.note() == null || request.note().isBlank() ? "Status updated" : request.note(),
            request.currentLocation(),
            shipment.getLastUpdated()));

        Shipment saved = shipmentRepository.save(shipment);
        eventPublisher.publishStatusUpdated(saved);
        return Optional.of(saved);
    }
}
