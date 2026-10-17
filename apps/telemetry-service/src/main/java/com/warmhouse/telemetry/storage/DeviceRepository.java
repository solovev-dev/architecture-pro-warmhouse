package com.warmhouse.telemetry.storage;

import com.warmhouse.telemetry.model.Device;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class DeviceRepository {

    private static final RowMapper<Device> ROW_MAPPER = (rs, rowNum) -> new Device(
            rs.getString("device_id"),
            rs.getString("name"),
            rs.getString("type"),
            rs.getString("location"),
            rs.getString("source"),
            rs.getTimestamp("created_at").toInstant()
    );

    private final JdbcTemplate jdbcTemplate;

    public DeviceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsert(Device device) {
        jdbcTemplate.update(
                """
                INSERT INTO devices (device_id, name, type, location, source, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (device_id) DO UPDATE SET
                    name = EXCLUDED.name,
                    type = EXCLUDED.type,
                    location = EXCLUDED.location,
                    source = EXCLUDED.source
                """,
                device.getDeviceId(),
                device.getName(),
                device.getType(),
                device.getLocation(),
                device.getSource(),
                Timestamp.from(device.getCreatedAt() != null ? device.getCreatedAt() : Instant.now())
        );
    }

    public boolean exists(String deviceId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM devices WHERE device_id = ?",
                Integer.class,
                deviceId
        );
        return count != null && count > 0;
    }

    public Optional<Device> findById(String deviceId) {
        List<Device> devices = jdbcTemplate.query(
                "SELECT device_id, name, type, location, source, created_at FROM devices WHERE device_id = ?",
                ROW_MAPPER,
                deviceId
        );
        return devices.stream().findFirst();
    }

    public List<Device> findAll() {
        return jdbcTemplate.query(
                "SELECT device_id, name, type, location, source, created_at FROM devices ORDER BY created_at",
                ROW_MAPPER
        );
    }
}
