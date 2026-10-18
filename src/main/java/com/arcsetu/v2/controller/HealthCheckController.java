package com.arcsetu.v2.controller;

import com.arcsetu.v2.model.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v2/health")
public class HealthCheckController {

    @GetMapping
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.ok("FinVane ERP Spring Boot Backend is operational", Map.of(
                "status", "UP",
                "version", "27.20-enterprise",
                "architecture", "ARCSET-U",
                "javaVersion", System.getProperty("java.version")
        ));
    }
}
