package dev.kauanmocelin.payments.controller;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class PaymentsResourceTest {

    @Test
    void shouldReturn204WhenValidPaymentRequest() {
        var json = """
            {
              "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
              "amount": 100.00
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(204);
    }

    @Test
    void shouldReturn400WhenCorrelationIdIsMissing() {
        var json = """
            {
              "amount": 100.00
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenCorrelationIdIsInvalidFormat() {
        var json = """
            {
              "correlationId": "invalid-uuid",
              "amount": 100.00
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenAmountIsMissing() {
        var json = """
            {
              "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3"
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenAmountIsNegative() {
        var json = """
            {
              "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
              "amount": -0.01
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenAmountIsZero() {
        var json = """
            {
              "correlationId": "4a7901b8-7d26-4d9d-aa19-4dc1c7cf60b3",
              "amount": 0
            }
            """;

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenEmptyJson() {
        var json = "{}";

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.jsonPath().getList("violations")).isNotEmpty();
    }

    @Test
    void shouldReturn400WhenInvalidJson() {
        var json = "{ invalid json }";

        Response response = given()
            .contentType(ContentType.JSON)
            .body(json)
            .post("/payments");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn200WhenValidSummaryRequest() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        Response response = given()
            .queryParam("from", from.toString())
            .queryParam("to", to.toString())
            .get("/payments-summary");

        assertThat(response.statusCode()).isEqualTo(200);

        var jsonPath = response.jsonPath();
        assertThat(jsonPath.getInt("default.totalRequests")).isEqualTo(43236);

        BigDecimal totalAmountDefault = new BigDecimal(jsonPath.getString("default.totalAmount"));
        assertThat(totalAmountDefault).isEqualByComparingTo(new BigDecimal("4.1554234E+8"));

        BigDecimal totalAmountFallback = new BigDecimal(jsonPath.getString("fallback.totalAmount"));
        assertThat(totalAmountFallback).isEqualByComparingTo(new BigDecimal("329347.34"));
    }

    @Test
    void shouldReturn400WhenMissingSummaryQueryParams() {
        Response response = given()
            .get("/payments-summary");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenOnlyFromParam() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");

        Response response = given()
            .queryParam("from", from.toString())
            .get("/payments-summary");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenOnlyToParam() {
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        Response response = given()
            .queryParam("to", to.toString())
            .get("/payments-summary");

        assertThat(response.statusCode()).isEqualTo(400);
    }

    @Test
    void shouldReturn400WhenInvalidInstantFormat() {
        Response response = given()
            .queryParam("from", "not-an-instant")
            .queryParam("to", "also-not-an-instant")
            .get("/payments-summary");

        assertThat(response.statusCode()).isEqualTo(404);
    }
}