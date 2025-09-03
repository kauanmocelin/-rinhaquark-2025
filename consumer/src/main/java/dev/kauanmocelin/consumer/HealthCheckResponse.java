package dev.kauanmocelin.consumer;

public record HealthCheckResponse(boolean failing, int minResponseTime) {
}
