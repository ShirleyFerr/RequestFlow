package com.requestflow.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.OffsetDateTime;
import java.util.Map;

@Path("/health/check")
@Produces(MediaType.APPLICATION_JSON)
public class HealthCheckResource {

    @GET
    public Map<String, Object> check() {
        return Map.of(
                "status", "UP",
                "service", "request-flow-api",
                "timestamp", OffsetDateTime.now()
        );
    }
}
