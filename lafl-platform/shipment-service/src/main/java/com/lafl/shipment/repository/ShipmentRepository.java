package com.lafl.shipment.repository;

import com.lafl.shipment.domain.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByReferenceIgnoreCase(String reference);
}
