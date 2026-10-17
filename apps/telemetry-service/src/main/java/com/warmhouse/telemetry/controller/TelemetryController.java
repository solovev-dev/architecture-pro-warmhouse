package com.warmhouse.telemetry.controller;

import com.warmhouse.telemetry.model.Device;
import com.warmhouse.telemetry.model.TelemetryMessage;
import com.warmhouse.telemetry.storage.DeviceRepository;
import com.warmhouse.telemetry.storage.TelemetryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class TelemetryController {

    private final TelemetryRepository telemetryRepository;
    private final DeviceRepository deviceRepository;

    public TelemetryController(TelemetryRepository telemetryRepository, DeviceRepository deviceRepository) {
        this.telemetryRepository = telemetryRepository;
        this.deviceRepository = deviceRepository;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/api/v1/telemetry")
    public List<TelemetryMessage> listAll() {
        return telemetryRepository.findAll();
    }

    @GetMapping("/api/v1/telemetry/{deviceId}")
    public ResponseEntity<List<TelemetryMessage>> listByDevice(@PathVariable String deviceId) {
        return ResponseEntity.ok(telemetryRepository.findByDeviceId(deviceId));
    }

    @GetMapping("/api/v1/devices")
    public List<Device> listDevices() {
        return deviceRepository.findAll();
    }
}
