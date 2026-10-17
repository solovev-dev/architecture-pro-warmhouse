package com.warmhouse.telemetry.kafka;

import com.warmhouse.telemetry.service.DeviceEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeviceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeviceEventConsumer.class);

    private final DeviceEventService deviceEventService;

    @Value("${telemetry.device-events-topic}")
    private String topic;

    public DeviceEventConsumer(DeviceEventService deviceEventService) {
        this.deviceEventService = deviceEventService;
    }

    @KafkaListener(topics = "${telemetry.device-events-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void onMessage(String payload) {
        log.debug("Received device event from {}: {}", topic, payload);
        deviceEventService.processDeviceEvent(payload);
    }
}
