package nus.iss.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
public class TestDbController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test-db")
    public ResponseEntity<String> testDatabaseConnection() {
        try {
            String sql = "SELECT COUNT(*) FROM doctor";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
            return ResponseEntity.ok("✅ Connected to SQL. Total doctors: " + count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ DB Error: " + e.getMessage());
        }
    }
}
