package com.warmhouse.telemetry.storage;

import com.warmhouse.telemetry.model.TelemetryMessage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class TelemetryRepository {

    private static final RowMapper<TelemetryMessage> ROW_MAPPER = (rs, rowNum) -> new TelemetryMessage(
            rs.getString("device_id"),
            rs.getString("metric"),
            rs.getDouble("value"),
            rs.getString("unit"),
            rs.getTimestamp("time").toInstant()
    );

    private final JdbcTemplate jdbcTemplate;

    public TelemetryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TelemetryMessage message) {
        jdbcTemplate.update(
                """
                INSERT INTO telemetry (time, device_id, metric, value, unit)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (time, device_id, metric) DO UPDATE SET
                    value = EXCLUDED.value,
                    unit = EXCLUDED.unit
                """,
                Timestamp.from(message.getReceivedAt()),
                message.getDeviceId(),
                message.getMetric(),
                message.getValue(),
                message.getUnit()
        );
    }

    public List<TelemetryMessage> findAll() {
        return jdbcTemplate.query(
                """
                SELECT time, device_id, metric, value, unit
                FROM telemetry
                ORDER BY time DESC
                LIMIT 1000
                """,
                ROW_MAPPER
        );
    }

    public List<TelemetryMessage> findByDeviceId(String deviceId) {
        return jdbcTemplate.query(
                """
                SELECT time, device_id, metric, value, unit
                FROM telemetry
                WHERE device_id = ?
                ORDER BY time DESC
                LIMIT 1000
                """,
                ROW_MAPPER,
                deviceId
        );
    }
}
