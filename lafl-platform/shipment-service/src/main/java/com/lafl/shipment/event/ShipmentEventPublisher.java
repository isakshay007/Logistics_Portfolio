package com.lafl.shipment.event;

import com.lafl.shipment.domain.Shipment;

public interface ShipmentEventPublisher {

    void publishStatusUpdated(Shipment shipment);
}
