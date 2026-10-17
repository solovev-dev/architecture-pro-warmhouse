package com.warmhouse.telemetry.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warmhouse.telemetry.model.Device;
import com.warmhouse.telemetry.storage.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DeviceEventService {

    private static final Logger log = LoggerFactory.getLogger(DeviceEventService.class);

    private final DeviceRepository deviceRepository;
    private final ObjectMapper objectMapper;

    public DeviceEventService(DeviceRepository deviceRepository, ObjectMapper objectMapper) {
        this.deviceRepository = deviceRepository;
        this.objectMapper = objectMapper;
    }

    public void processDeviceEvent(String rawPayload) {
        try {
            JsonNode node = objectMapper.readTree(rawPayload);
            String eventType = textOrDefault(node, "eventType", "");
            if (!"device.created".equals(eventType)) {
                log.debug("Ignoring unsupported device event type: {}", eventType);
                return;
            }

            String deviceId = textOrDefault(node, "deviceId", "");
            if (deviceId.isBlank()) {
                log.warn("device.created event without deviceId, skipping");
                return;
            }

            Device device = new Device(
                    deviceId,
                    textOrDefault(node, "name", "unknown"),
                    textOrDefault(node, "type", "unknown"),
                    textOrDefault(node, "location", "unknown"),
                    textOrDefault(node, "source", "unknown"),
                    Instant.now()
            );
            deviceRepository.upsert(device);
            log.info("Registered device {} from source {}", deviceId, device.getSource());
        } catch (Exception ex) {
            log.warn("Failed to process device event: {}", ex.getMessage());
        }
    }

    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        if (node.has(field) && !node.get(field).isNull()) {
            return node.get(field).asText(defaultValue);
        }
        return defaultValue;
    }
}
