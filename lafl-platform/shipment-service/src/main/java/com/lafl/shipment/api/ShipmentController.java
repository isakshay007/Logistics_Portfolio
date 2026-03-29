package com.lafl.shipment.api;

import com.lafl.shipment.domain.Shipment;
import com.lafl.shipment.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @GetMapping("/track")
    @Operation(summary = "Track a shipment by reference")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Shipment found"),
        @ApiResponse(responseCode = "404", description = "Shipment reference not found")
    })
    public ResponseEntity<?> trackShipment(@RequestParam String reference) {
        return shipmentService.findByReference(reference)
            .<ResponseEntity<?>>map(shipment -> ResponseEntity.ok(Map.of("shipment", shipment)))
            .orElseGet(() -> ResponseEntity.status(404)
                .body(Map.of("message", "Shipment reference not found.")));
    }

    @GetMapping("/{reference}")
    @Operation(summary = "Get shipment details by reference")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Shipment found"),
        @ApiResponse(responseCode = "404", description = "Shipment reference not found")
    })
    public ResponseEntity<?> getShipment(@PathVariable String reference) {
        return shipmentService.findByReference(reference)
            .<ResponseEntity<?>>map(shipment -> ResponseEntity.ok(Map.of("shipment", shipment)))
            .orElseGet(() -> ResponseEntity.status(404)
                .body(Map.of("message", "Shipment reference not found.")));
    }

    @PatchMapping("/{reference}/status")
    @Operation(summary = "Update shipment status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Shipment status updated"),
        @ApiResponse(responseCode = "404", description = "Shipment reference not found")
    })
    public ResponseEntity<?> updateStatus(@PathVariable String reference,
                                          @Valid @RequestBody ShipmentStatusUpdateRequest request) {
        return shipmentService.updateStatus(reference, request)
            .<ResponseEntity<?>>map(shipment -> ResponseEntity.ok(Map.of("shipment", shipment)))
            .orElseGet(() -> ResponseEntity.status(404)
                .body(Map.of("message", "Shipment reference not found.")));
    }
}
