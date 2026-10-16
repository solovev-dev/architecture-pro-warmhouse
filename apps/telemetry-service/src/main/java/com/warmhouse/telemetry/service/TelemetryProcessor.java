package com.warmhouse.telemetry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warmhouse.telemetry.model.TelemetryMessage;
import com.warmhouse.telemetry.storage.DeviceRepository;
import com.warmhouse.telemetry.storage.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TelemetryProcessor {

    private static final Logger log = LoggerFactory.getLogger(TelemetryProcessor.class);

    private final DeviceRepository deviceRepository;
    private final TelemetryRepository telemetryRepository;
    private final ObjectMapper objectMapper;

    public TelemetryProcessor(
            DeviceRepository deviceRepository,
            TelemetryRepository telemetryRepository,
            ObjectMapper objectMapper
    ) {
        this.deviceRepository = deviceRepository;
        this.telemetryRepository = telemetryRepository;
        this.objectMapper = objectMapper;
    }

    public void processRawMessage(String rawPayload) {
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            String deviceId = firstNonBlank(
                    textOrNull(node, "deviceId"),
                    textOrNull(node, "device_id")
            );

            if (deviceId == null || deviceId.isBlank()) {
                log.warn("Telemetry without deviceId, skipping");
                return;
            }

            if (!deviceRepository.exists(deviceId)) {
                log.info("Device {} not registered, skipping telemetry", deviceId);
                return;
            }

            String metric = firstNonBlank(
                    textOrNull(node, "metric"),
                    nestedText(node, "payload", "metric"),
                    "value"
            );
            double value = firstDouble(node, "value");
            if (node.has("payload") && node.get("payload").has("temp")) {
                metric = metric == null || "value".equals(metric) ? "temp" : metric;
                value = node.get("payload").get("temp").asDouble(value);
            }
            String unit = firstNonBlank(textOrNull(node, "unit"), "");

            Instant receivedAt = Instant.now();
            if (node.has("timestamp") && !node.get("timestamp").isNull()) {
                try {
                    receivedAt = Instant.parse(node.get("timestamp").asText());
                } catch (Exception ignored) {
                    // keep now()
                }
            }

            TelemetryMessage message = new TelemetryMessage(deviceId, metric, value, unit, receivedAt);
            telemetryRepository.save(message);
            log.info("Stored telemetry for device {} metric={}", deviceId, metric);
        } catch (Exception ex) {
            log.warn("Failed to process telemetry payload: {}", ex.getMessage());
        }
    }

    private String textOrNull(JsonNode node, String field) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText();
        }
        return null;
    }

    private String nestedText(JsonNode node, String parent, String field) {
        if (node.has(parent) && node.get(parent).has(field) && !node.get(parent).get(field).isNull()) {
            return node.get(parent).get(field).asText();
        }
        return null;
    }

    private double firstDouble(JsonNode node, String field) {
        if (node.has(field) && node.get(field).isNumber()) {
            return node.get(field).asDouble();
        }
        return 0.0;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
