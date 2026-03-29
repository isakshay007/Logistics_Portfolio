package com.lafl.notification.api;

import com.fasterxml.jackson.databind.JsonNode;

public record NotificationTriggerRequest(
    String eventType,
    JsonNode payload
) {
}
