package com.example.foodNow.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseFix implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Drop the restrictive check constraint. Hibernate might recreate it correctly
            // on next full ddl-auto=create,
            // or we just live without it for now.
            jdbcTemplate.execute("ALTER TABLE deliveries DROP CONSTRAINT IF EXISTS deliveries_status_check");
            System.out.println("SUCCESSFULLY DROPPED CONSTRAINT deliveries_status_check");
        } catch (Exception e) {
            System.err.println("Failed to drop constraint: " + e.getMessage());
        }
    }
}
