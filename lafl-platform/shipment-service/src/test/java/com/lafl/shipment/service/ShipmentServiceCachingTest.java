package com.lafl.shipment.service;

import com.lafl.shipment.api.ShipmentStatusUpdateRequest;
import com.lafl.shipment.domain.Shipment;
import com.lafl.shipment.event.ShipmentEventPublisher;
import com.lafl.shipment.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(ShipmentServiceCachingTest.TestConfig.class)
class ShipmentServiceCachingTest {

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        ShipmentRepository shipmentRepository() {
            return mock(ShipmentRepository.class);
        }

        @Bean
        ShipmentEventPublisher shipmentEventPublisher() {
            return mock(ShipmentEventPublisher.class);
        }

        @Bean
        ShipmentService shipmentService(ShipmentRepository repository, ShipmentEventPublisher publisher) {
            return new ShipmentService(repository, publisher);
        }

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("shipments");
        }
    }

    @jakarta.annotation.Resource
    private ShipmentService shipmentService;

    @jakarta.annotation.Resource
    private ShipmentRepository shipmentRepository;

    @jakarta.annotation.Resource
    private ShipmentEventPublisher shipmentEventPublisher;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        reset(shipmentRepository, shipmentEventPublisher);
        if (cacheManager.getCache("shipments") != null) {
            cacheManager.getCache("shipments").clear();
        }
    }

    @Test
    void cacheHitSkipsRepositoryCallOnSecondTrackInvocation() {
        Shipment shipment = shipment("LAFL-10001", "On Schedule", "Rotterdam", 72);
        when(shipmentRepository.findByReferenceIgnoreCase("LAFL-10001")).thenReturn(Optional.of(shipment));

        shipmentService.findByReference("LAFL-10001");
        shipmentService.findByReference("LAFL-10001");

        verify(shipmentRepository, times(1)).findByReferenceIgnoreCase("LAFL-10001");
    }

    @Test
    void cacheEvictedAfterStatusUpdateSoNextTrackHitsRepositoryAgain() {
        Shipment shipment = shipment("LAFL-24017", "On Schedule", "Rotterdam", 72);
        when(shipmentRepository.findByReferenceIgnoreCase("LAFL-24017"))
            .thenReturn(Optional.of(shipment), Optional.of(shipment), Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shipmentService.findByReference("LAFL-24017");
        Optional<Shipment> updated = shipmentService.updateStatus("LAFL-24017",
            new ShipmentStatusUpdateRequest("Delayed", "Antwerp Hub", 63, "Weather disruption"));
        shipmentService.findByReference("LAFL-24017");

        assertTrue(updated.isPresent());
        verify(shipmentRepository, times(3)).findByReferenceIgnoreCase("LAFL-24017");
    }

    private Shipment shipment(String reference, String status, String location, int progress) {
        Shipment shipment = new Shipment();
        shipment.setReference(reference);
        shipment.setStatus(status);
        shipment.setCurrentLocation(location);
        shipment.setProgress(progress);
        shipment.setLastUpdated("2026-03-28T12:00:00Z");
        return shipment;
    }
}
