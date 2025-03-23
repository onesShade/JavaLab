package javalab.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Default controller", description = "Controller to see if app is up")
public class HomeController {

    @GetMapping
    @Operation(summary = "Default respond")
    public String getGreeting() {
        return "App is up!";
    }
}