package javalab.controller;

import java.util.HashMap;
import java.util.Map;
import javalab.logger.NoLogging;
import javalab.service.VisitCounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/counter")
public class VisitCounterController {

    VisitCounterService visitCounterService;

    @Autowired
    public VisitCounterController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @PostMapping
    @NoLogging
    public void increment() {
        visitCounterService.increment();
    }

    @GetMapping
    public Map<String, Long> getCounter() {
        Map<String, Long> map = new HashMap<>();
        map.put("visit count", visitCounterService.getCount());
        return map;
    }
}