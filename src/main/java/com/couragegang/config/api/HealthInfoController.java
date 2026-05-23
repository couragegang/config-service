package com.couragegang.config.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import java.util.Map;

@Controller
public final class HealthInfoController {

    @Get("/")
    public Map<String, String> root() {
        return Map.of(
                "service", "config-service",
                "health", "/v1/config/health",
                "metrics", "/v1/config/prometheus",
                "workspaces", "/v1/config/orgs/{orgId}/workspaces");
    }
}
