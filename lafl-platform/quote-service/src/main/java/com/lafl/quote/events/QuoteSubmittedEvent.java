package com.lafl.quote.events;

public record QuoteSubmittedEvent(
    String eventType,
    String timestamp,
    String quoteId,
    String email,
    String company,
    String serviceType,
    String origin,
    String destination,
    String status
) {
}
