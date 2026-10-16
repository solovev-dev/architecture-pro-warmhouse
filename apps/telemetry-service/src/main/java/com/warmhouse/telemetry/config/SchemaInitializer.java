package com.warmhouse.telemetry.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

@Component
public class SchemaInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String sql = StreamUtils.copyToString(
                new ClassPathResource("schema.sql").getInputStream(),
                StandardCharsets.UTF_8
        );

        for (String statement : sql.split(";")) {
            String trimmed = statement.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            try {
                jdbcTemplate.execute(trimmed);
            } catch (Exception ex) {
                log.warn("Schema statement skipped/failed: {} — {}", firstLine(trimmed), ex.getMessage());
            }
        }
        log.info("TimescaleDB schema initialization completed");
    }

    private String firstLine(String sql) {
        int newline = sql.indexOf('\n');
        return newline > 0 ? sql.substring(0, newline) : sql;
    }
}
