package com.lafl.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @GetMapping("/api/health")
    public Mono<Map<String, String>> apiHealth() {
        return Mono.just(Map.of(
            "status", "UP",
            "service", "gateway"
        ));
    }

    @GetMapping("/fallback/shipment")
    public Mono<Map<String, String>> shipmentFallback() {
        return Mono.just(Map.of(
            "message", "Shipment service temporarily unavailable.",
            "action", "Retry in a few seconds or contact operations."
        ));
    }
}
