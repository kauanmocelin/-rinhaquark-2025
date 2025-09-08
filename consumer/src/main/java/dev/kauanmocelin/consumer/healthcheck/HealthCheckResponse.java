package dev.kauanmocelin.consumer.healthcheck;

public record HealthCheckResponse(boolean failing, int minResponseTime) {
}
