package com.warmhouse.telemetry.model;

import java.time.Instant;

public class Device {

    private String deviceId;
    private String name;
    private String type;
    private String location;
    private String source;
    private Instant createdAt;

    public Device() {
    }

    public Device(String deviceId, String name, String type, String location, String source, Instant createdAt) {
        this.deviceId = deviceId;
        this.name = name;
        this.type = type;
        this.location = location;
        this.source = source;
        this.createdAt = createdAt;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
