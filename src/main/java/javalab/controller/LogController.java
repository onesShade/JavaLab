package javalab.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import javalab.model.Log;
import javalab.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Log controller")
@RequestMapping("/logs")
public class LogController {
    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/generate")
    public Log generateLog(@RequestParam String date) {
        return logService.generateLogs(date);
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Map<String, String>> getStatus(@PathVariable Long id) {
        Map<String, String> map = new HashMap<>();
        map.put("id", id.toString());
        map.put("status", logService.getLogStatus(id));
        map.put("logDate", logService.getLogDate(id));
        return ResponseEntity.ok(map);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<String> downloadLog(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"logs-" + logService.getLogDate(id) + ".txt\"")
                .body(logService.getLogBody(id));
    }
}