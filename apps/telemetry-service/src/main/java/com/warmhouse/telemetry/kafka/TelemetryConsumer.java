package com.warmhouse.telemetry.kafka;

import com.warmhouse.telemetry.service.TelemetryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryConsumer.class);

    private final TelemetryProcessor processor;

    @Value("${telemetry.topic}")
    private String topic;

    public TelemetryConsumer(TelemetryProcessor processor) {
        this.processor = processor;
    }

    @KafkaListener(topics = "${telemetry.topic}")
    public void onMessage(String payload) {
        log.debug("Received message from topic {}: {}", topic, payload);
        processor.processRawMessage(payload);
    }
}
