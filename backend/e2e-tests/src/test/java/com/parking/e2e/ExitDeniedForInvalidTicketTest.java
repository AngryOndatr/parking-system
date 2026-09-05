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
import static org.hamcrest.Matchers.containsString;

/**
 * E2E test: Gate exit denied for invalid / missing ticket.
 *
 * Verifies the denied-exit error path when a visitor presents:
 *   a) An invalid/bogus ticket code
 *   b) No ticket at all (and is not a subscriber)
 *
 * Both cases should result in gate DENY (CLOSED) with paymentRequired=true.
 *
 * Note: The OpenAPI spec documents 404 for unknown ticket, but the actual
 * implementation returns 200 with action=DENY (gate stays CLOSED).
 * This test documents the actual behavior, not the spec.
 */
public class ExitDeniedForInvalidTicketTest {

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

        System.out.println("ExitDeniedForInvalidTicketTest - API Gateway URL: " + apiGatewayUrl);

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

        System.out.println("All services are ready for exit denied test");
    }

    @Test
    void exitDeniedWithBogusTicket() {
        String licensePlate = "DENY-BOGUS-" + System.currentTimeMillis();

        // =================================================================
        // Try to exit with a completely bogus ticket code.
        // Expected: gate stays CLOSED, payment required, exit denied.
        // =================================================================
        System.out.println("\n=== Exit with bogus ticket ===");
        System.out.println("License Plate: " + licensePlate);
        System.out.println("Ticket Code:   FAKE-BOGUS-TICKET-999");

        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"ticketCode\":\"FAKE-BOGUS-TICKET-999\", \"licensePlate\":\"%s\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}",
                        licensePlate))
                .log().all()
                .when()
                .post("/api/gate/exit")
                .then()
                .log().all()
                .statusCode(200)
                // Gate should remain closed (DENY action)
                .body("gateStatus", equalTo("CLOSED"))
                // Payment is required to open
                .body("paymentRequired", equalTo(true))
                // isPaid should be false
                .body("isPaid", equalTo(false))
                // Message should indicate payment is required
                .body("message", containsString("Payment required"));

        System.out.println("✅ Exit with bogus ticket correctly denied");
    }

    @Test
    void exitDeniedWithoutTicket() {
        // Use a license plate that is NOT a subscriber and has no ticket
        String licensePlate = "DENY-NOTICKET-" + System.currentTimeMillis();

        // =================================================================
        // Try to exit with NO ticket code and plate that is not a subscriber.
        // Expected: gate stays CLOSED, payment required, exit denied.
        // =================================================================
        System.out.println("\n=== Exit without ticket ===");
        System.out.println("License Plate: " + licensePlate);

        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"licensePlate\":\"%s\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}",
                        licensePlate))
                .log().all()
                .when()
                .post("/api/gate/exit")
                .then()
                .log().all()
                .statusCode(200)
                .body("gateStatus", equalTo("CLOSED"))
                .body("paymentRequired", equalTo(true))
                .body("isPaid", equalTo(false));

        System.out.println("✅ Exit without ticket correctly denied");
    }

    @Test
    void exitDeniedWithValidTicketNotPaid() {
        // Create a real ticket via entry, then try to exit without paying
        String licensePlate = "DENY-UNPAID-" + System.currentTimeMillis();

        // =================================================================
        // Step 1: Create a valid ticket via entry
        // =================================================================
        System.out.println("\n=== Create valid ticket via entry ===");

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
                .body("ticketCode", org.hamcrest.Matchers.notNullValue())
                .extract();

        String ticketCode = entryResponse.path("ticketCode");
        System.out.println("Created ticket: " + ticketCode);

        // =================================================================
        // Step 2: Try to exit WITHOUT paying — should be denied
        // =================================================================
        System.out.println("\n=== Exit without payment (should be denied) ===");

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
                .body("gateStatus", equalTo("CLOSED"))
                .body("paymentRequired", equalTo(true))
                .body("isPaid", equalTo(false));

        System.out.println("✅ Valid unpaid ticket correctly denied exit");
    }

    @AfterAll
    static void tearDown() {
        if (environment != null) {
            environment.stop();
        }
    }
}
