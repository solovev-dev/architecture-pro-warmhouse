package com.warmhouse.telemetry.model;

import java.time.Instant;

public class TelemetryMessage {

    private String deviceId;
    private String metric;
    private double value;
    private String unit;
    private Instant receivedAt;

    public TelemetryMessage() {
    }

    public TelemetryMessage(String deviceId, String metric, double value, String unit, Instant receivedAt) {
        this.deviceId = deviceId;
        this.metric = metric;
        this.value = value;
        this.unit = unit;
        this.receivedAt = receivedAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
