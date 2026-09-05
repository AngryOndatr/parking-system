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

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * E2E test: Double payment attempt.
 *
 * Verifies that paying for the same parking event twice is rejected.
 *
 * IMPORTANT: The OpenAPI spec documents 409 for "Payment already exists",
 * but the actual GlobalExceptionHandler.handleTicketAlreadyPaid returns 400.
 * This test expects 400 to match the actual implementation.
 *
 * Flow (4 steps):
 *   1. POST /api/gate/entry        → 201, ticketCode issued
 *   2. POST /api/billing/test-event → 201, billing parking event created
 *   3. POST /api/billing/pay (1st)  → 201, status=COMPLETED
 *   4. POST /api/billing/pay (2nd)  → 400, "Ticket already paid: ..."
 */
public class DoublePaymentAttemptTest {

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

        System.out.println("DoublePaymentAttemptTest - API Gateway URL: " + apiGatewayUrl);

        waitForServices();
    }

    private static void waitForServices() {
        System.out.println("Waiting for services to be ready...");

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

        System.out.println("All services are ready for double payment test");
    }

    @Test
    void doublePaymentAttempt() {
        // Generate unique license plate
        String licensePlate = "DOUBLEPAY-" + System.currentTimeMillis();

        // =================================================================
        // Step 1: Vehicle Entry — generate ticketCode
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
                .body("ticketCode", notNullValue())
                .body("parkingEventId", notNullValue())
                .extract();

        String ticketCode = entryResponse.path("ticketCode");
        System.out.println("Ticket code: " + ticketCode);

        // =================================================================
        // Step 2: Create billing parking event
        // =================================================================
        System.out.println("\n=== Step 2: Create billing parking event ===");

        var testEventResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"ticketCode\":\"%s\", \"licensePlate\":\"%s\", \"entryMinutesAgo\":60}",
                        ticketCode, licensePlate))
                .log().all()
                .when()
                .post("/api/billing/test-event")
                .then()
                .log().all()
                .statusCode(201)
                .body("parkingEventId", notNullValue())
                .extract();

        Integer billingEventId = testEventResponse.path("parkingEventId");
        System.out.println("Billing parking event ID: " + billingEventId);

        // =================================================================
        // Step 3: First payment — should succeed (201)
        // =================================================================
        System.out.println("\n=== Step 3: First Payment ===");

        long txnId1 = System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"parkingEventId\":%d, \"amount\":100.00, \"paymentMethod\":\"CARD\", \"transactionId\":\"TXN-FIRST-%d\"}",
                        billingEventId, txnId1))
                .log().all()
                .when()
                .post("/api/billing/pay")
                .then()
                .log().all()
                .statusCode(201)
                .body("status", equalTo("COMPLETED"))
                .body("parkingEventId", equalTo(billingEventId));

        System.out.println("First payment succeeded");

        // =================================================================
        // Step 4: Second payment — should be REJECTED
        // Expected: 400 with "Ticket already paid: ..."
        // (OpenAPI spec says 409, but actual code returns 400)
        // =================================================================
        System.out.println("\n=== Step 4: Second Payment (should be rejected) ===");

        long txnId2 = System.currentTimeMillis() + 1;

        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"parkingEventId\":%d, \"amount\":100.00, \"paymentMethod\":\"CARD\", \"transactionId\":\"TXN-SECOND-%d\"}",
                        billingEventId, txnId2))
                .log().all()
                .when()
                .post("/api/billing/pay")
                .then()
                .log().all()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", org.hamcrest.Matchers.containsString("Ticket already paid"));

        System.out.println("Second payment correctly rejected with 400");

        // =================================================================
        // Step 5: Verify payment status is still PAID (from first payment)
        // =================================================================
        System.out.println("\n=== Step 5: Verify payment status still PAID ===");

        given()
                .queryParam("parkingEventId", billingEventId)
                .when()
                .get("/api/billing/status")
                .then()
                .log().all()
                .statusCode(200)
                .body("isPaid", equalTo(true));

        System.out.println("Payment status confirmed: still PAID from first attempt");

        System.out.println("\n=== Double Payment Attempt Test Completed Successfully ===");
    }

    @AfterAll
    static void tearDown() {
        if (environment != null) {
            environment.stop();
        }
    }
}
