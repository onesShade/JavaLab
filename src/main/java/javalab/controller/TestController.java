package javalab.controller;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final DataSource dataSource;

    @Autowired
    TestController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/test")
    public String testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return "Connection successful!";
        } catch (SQLException e) {
            return "Connection failed: " + e.getMessage();
        }
    }
}