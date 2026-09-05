package com.parking.e2e;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;
import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * E2E test: One-time visitor with real billing flow.
 *
 * Verifies the full payment cycle using real billing endpoints:
 *   1. POST /api/gate/entry             → 201, ticketCode issued
 *   2. POST /api/billing/test-event    → 201, creates parking_events row (billing service needs its own row)
 *   3. POST /api/billing/calculate     → 200, real fee calculation (requires tariff + parking event)
 *   4. POST /api/billing/pay           → 201, real payment recording
 *   5. GET  /api/billing/status        → 200, isPaid=true
 *   6. POST /api/gate/exit            → 200, gate opens
 *
 * Pre-condition: tariffs table is seeded by V7 migration (ONE_TIME tariff, 50.00 UAH/hr).
 * The e2e docker-compose runs Flyway so V7 is applied automatically.
 */
public class VisitorRealBillingFlowTest {

    private static DockerComposeContainer<?> environment;

    @BeforeAll
    static void setup() {
        System.out.println("Working directory: " + System.getProperty("user.dir"));

        environment = new DockerComposeContainer<>(new File("docker-compose-e2e.yml"))
                .withExposedService("api-gateway", 8080,
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(5)))
                .withLocalCompose(false);

        environment.start();

        String apiGatewayUrl = "http://" + environment.getServiceHost("api-gateway", 8080)
                + ":" + environment.getServicePort("api-gateway", 8080);
        RestAssured.baseURI = apiGatewayUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        System.out.println("VisitorRealBillingFlowTest - API Gateway URL: " + apiGatewayUrl);

        waitForServices();
    }

    private static void waitForServices() {
        System.out.println("Waiting for services to be ready...");

        // API Gateway
        await().atMost(Duration.ofMinutes(5)).pollInterval(Duration.ofSeconds(10)).until(() -> {
            try {
                given().when().get("/actuator/health").then().statusCode(200);
                System.out.println("API Gateway is ready");
                return true;
            } catch (Exception e) {
                System.out.println("API Gateway not ready yet: " + e.getMessage());
                return false;
            }
        });

        // Gate Control Service
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(10)).until(() -> {
            try {
                int status = given().when().get("/api/gate/events").getStatusCode();
                if (status == 200 || status == 404) {
                    System.out.println("Gate Control Service is ready (status=" + status + ")");
                    return true;
                }
                System.out.println("Gate Control Service returned: " + status);
                return false;
            } catch (Exception e) {
                System.out.println("Gate Control Service not ready: " + e.getMessage());
                return false;
            }
        });

        // Billing Service
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(10)).until(() -> {
            try {
                int status = given().queryParam("parkingEventId", 0).when().get("/api/billing/status").getStatusCode();
                if (status == 200 || status == 404) {
                    System.out.println("Billing Service is ready (status=" + status + ")");
                    return true;
                }
                System.out.println("Billing Service returned: " + status);
                return false;
            } catch (Exception e) {
                System.out.println("Billing Service not ready: " + e.getMessage());
                return false;
            }
        });

        System.out.println("All services are ready for real billing flow test");
    }

    @Test
    void visitorRealBillingFlow() {
        // Generate unique license plate
        String licensePlate = "REALBILL-" + System.currentTimeMillis();

        // =================================================================
        // Step 1: Vehicle Entry — generates ticketCode (via gate_events)
        // =================================================================
        System.out.println("\n=== Step 1: Vehicle Entry ===");
        System.out.println("License Plate: " + licensePlate);

        var entryResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"licensePlate\":\"%s\", \"entryMethod\":\"SCAN\", \"gateId\":\"ENTRY-1\"}",
                        licensePlate))
                .log().all()
                .when()
                .post("/api/gate/entry")
                .then()
                .log().all()
                .statusCode(201)
                .body("gateStatus", equalTo("OPENED"))
                .body("licensePlate", equalTo(licensePlate))
                .body("ticketCode", notNullValue())
                .body("parkingEventId", notNullValue())
                .extract();

        String ticketCode = entryResponse.path("ticketCode");
        System.out.println("Ticket code: " + ticketCode);

        // =================================================================
        // Step 2: Create billing parking event (via test-event endpoint)
        // The billing service needs its own parking_events row to calculate fee.
        // Gate service creates gate_events, not parking_events.
        // =================================================================
        System.out.println("\n=== Step 2: Create billing parking event ===");

        var testEventResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"ticketCode\":\"%s\", \"licensePlate\":\"%s\", \"entryMinutesAgo\":120}",
                        ticketCode, licensePlate))
                .log().all()
                .when()
                .post("/api/billing/test-event")
                .then()
                .log().all()
                .statusCode(201)
                .body("parkingEventId", notNullValue())
                .body("ticketCode", equalTo(ticketCode))
                .body("licensePlate", equalTo(licensePlate))
                .extract();

        Integer billingEventId = testEventResponse.path("parkingEventId");
        System.out.println("Billing parking event ID: " + billingEventId);

        // Build times for calculate request
        OffsetDateTime entryTime = OffsetDateTime.now().minusHours(2);
        OffsetDateTime exitTime = OffsetDateTime.now();

        // =================================================================
        // Step 3: Calculate fee (real endpoint)
        // Uses ONE_TIME tariff (50.00 UAH/hr), minimum 1 hour.
        // With 2 hours, expected fee = 100.00 UAH.
        // =================================================================
        System.out.println("\n=== Step 3: Calculate Fee ===");
        System.out.println("Entry time: " + entryTime);
        System.out.println("Exit time:  " + exitTime);

        var calcResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"parkingEventId\":%d, \"entryTime\":\"%s\", \"exitTime\":\"%s\", \"tariffType\":\"ONE_TIME\", \"isSubscriber\":false}",
                        billingEventId, entryTime, exitTime))
                .log().all()
                .when()
                .post("/api/billing/calculate")
                .then()
                .log().all()
                .statusCode(200)
                .body("parkingEventId", equalTo(billingEventId))
                .body("totalFee", notNullValue())
                .extract();

        Double totalFee = calcResponse.path("totalFee");
        String tariffApplied = calcResponse.path("tariffApplied");
        Integer durationMinutes = calcResponse.path("durationMinutes");

        System.out.println("Calculated fee: " + totalFee + " UAH");
        System.out.println("Tariff applied: " + tariffApplied);
        System.out.println("Duration: " + durationMinutes + " minutes");

        // Fee should be at least 50.00 (1 hour minimum) — assert via response body
        // Note: We already extracted totalFee above; this guards against the calculator
        // returning 0 or negative due to a regression.
        if (totalFee == null || totalFee < 50.0) {
            throw new AssertionError("Expected totalFee >= 50.0, but got: " + totalFee);
        }

        // =================================================================
        // Step 4: Process real payment
        // =================================================================
        System.out.println("\n=== Step 4: Process Real Payment ===");

        var payResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"parkingEventId\":%d, \"amount\":%.2f, \"paymentMethod\":\"CARD\", \"transactionId\":\"TXN-REALBILL-%d\"}",
                        billingEventId, totalFee, System.currentTimeMillis()))
                .log().all()
                .when()
                .post("/api/billing/pay")
                .then()
                .log().all()
                .statusCode(201)
                .body("paymentId", notNullValue())
                .body("status", equalTo("COMPLETED"))
                .body("parkingEventId", equalTo(billingEventId))
                .extract();

        Long paymentId = payResponse.path("paymentId");
        System.out.println("Payment ID: " + paymentId);

        // =================================================================
        // Step 5: Verify payment status
        // =================================================================
        System.out.println("\n=== Step 5: Verify Payment Status ===");

        given()
                .queryParam("parkingEventId", billingEventId)
                .when()
                .get("/api/billing/status")
                .then()
                .log().all()
                .statusCode(200)
                .body("isPaid", equalTo(true))
                .body("parkingEventId", equalTo(billingEventId));

        System.out.println("Payment status verified: PAID");

        // =================================================================
        // Step 6: Successful exit
        // =================================================================
        System.out.println("\n=== Step 6: Successful Exit ===");

        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"ticketCode\":\"%s\", \"licensePlate\":\"%s\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}",
                        ticketCode, licensePlate))
                .log().all()
                .when()
                .post("/api/gate/exit")
                .then()
                .log().all()
                .statusCode(200)
                .body("gateStatus", equalTo("OPENED"))
                .body("paymentRequired", equalTo(false))
                .body("isPaid", equalTo(true));

        System.out.println("\n=== Visitor Real Billing Flow Test Completed Successfully ===");
    }

    @AfterAll
    static void tearDown() {
        if (environment != null) {
            environment.stop();
        }
    }
}
