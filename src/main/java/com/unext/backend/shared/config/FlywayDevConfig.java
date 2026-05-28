package com.unext.backend.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@Configuration
@Profile("development")
public class FlywayDevConfig {

    @Bean
    public FlywayMigrationStrategy repairAndMigrateStrategy(DataSource dataSource) {
        return flyway -> {
            if (isDatabaseInBadState(dataSource)) {
                log.warn("Detected corrupt Flyway state (users table missing). Cleaning schema history and re-migrating.");
                flyway.clean();
            }
            flyway.migrate();
        };
    }

    private boolean isDatabaseInBadState(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, "public", "users", new String[]{"TABLE"});
            return !rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
