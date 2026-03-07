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

public class OneTimeVisitorE2ETest {

    private static DockerComposeContainer<?> environment;

    @BeforeAll
    static void setup() {
        System.out.println("Working directory: " + System.getProperty("user.dir"));
        System.out.println("Docker host: " + System.getenv("DOCKER_HOST"));

        File composeFile = new File("docker-compose-e2e.yml");
        System.out.println("Compose file exists: " + composeFile.exists());
        System.out.println("Compose file absolute path: " + composeFile.getAbsolutePath());

        // Start Testcontainers environment
        environment = new DockerComposeContainer<>(new File("docker-compose-e2e.yml"))
                .withExposedService("api-gateway", 8080,
                        Wait.forHttp("/actuator/health")
                                .forStatusCode(200)
                                .withStartupTimeout(Duration.ofMinutes(5)))
                .withLocalCompose(false); // Использовать контейнеризованный docker-compose

        environment.start();

        // Set RestAssured base URI to API Gateway
        String apiGatewayUrl = "http://" + environment.getServiceHost("api-gateway", 8080)
                + ":" + environment.getServicePort("api-gateway", 8080);
        RestAssured.baseURI = apiGatewayUrl;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        System.out.println("Using Testcontainers for E2E tests. API Gateway URL: " + apiGatewayUrl);

        // Wait for services to be ready
        waitForServices();
    }

    private static void waitForServices() {
        System.out.println("Waiting for services to be ready...");

        // Wait for API Gateway health
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

        // Wait for gate-control-service to be reachable via API Gateway
        System.out.println("Waiting for Gate Control Service to be ready...");
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(10)).until(() -> {
            try {
                int status = given().when().get("/api/v1/gate/events").getStatusCode();
                // 200 or 404 both mean gate-control-service is UP
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

        // Wait for billing-service to be reachable via API Gateway
        System.out.println("Waiting for Billing Service to be ready...");
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(10)).until(() -> {
            try {
                int status = given().queryParam("parkingEventId", 0).when().get("/api/v1/billing/status").getStatusCode();
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

        System.out.println("All services are ready for testing");
    }

    @Test
    void oneTimeVisitorFullCycle() {
        // Generate a short unique license plate (max 20 chars as per OpenAPI spec)
        long timestamp = System.currentTimeMillis();
        String shortTimestamp = String.valueOf(timestamp).substring(7); // Last 6 digits
        String licensePlate = "E2E-" + shortTimestamp; // E2E-123456 format (10 chars)

        // Step 1: Vehicle Entry - Generate Ticket
        System.out.println("\n=== Step 1: Vehicle Entry ===");
        System.out.println("License Plate: " + licensePlate);
        System.out.println("Request URL: " + RestAssured.baseURI + "/api/v1/gate/entry");

        // Extract both ticketCode and parkingEventId from entry response
        var entryResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"licensePlate\":\"" + licensePlate + "\", \"entryMethod\":\"SCAN\", \"gateId\":\"ENTRY-1\"}")
                .log().all() // Log request
                .when()
                .post("/api/v1/gate/entry")
                .then()
                .log().all() // Log response for debugging
                .statusCode(201)
                .body("gateStatus", equalTo("OPENED"))
                .body("licensePlate", equalTo(licensePlate))
                .body("parkingEventId", notNullValue())
                .extract();

        String ticketCode = entryResponse.path("ticketCode");
        Integer parkingEventId = entryResponse.path("parkingEventId");

        System.out.println("Ticket code: " + ticketCode);
        System.out.println("Parking event ID: " + parkingEventId);

        // Step 2: Attempt Exit without Payment - Should be Denied
        System.out.println("\n=== Step 2: Attempt Exit without Payment ===");
        given()
                .contentType(ContentType.JSON)
                .body("{\"ticketCode\":\"" + ticketCode + "\", \"licensePlate\":\"" + licensePlate + "\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}")
                .when()
                .post("/api/v1/gate/exit")
                .then()
                .log().all()
                .statusCode(200)
                .body("paymentRequired", equalTo(true))
                .body("message", notNullValue());

        // Step 3: Process Payment (using test endpoint for E2E)
        // NOTE: Step "check unpaid status" is intentionally omitted here.
        // Billing-service has its own DB and doesn't know about the gate event until payment is made.
        // The unpaid state is already proven by Step 2 (exit denied with paymentRequired=true).
        System.out.println("\n=== Step 3: Process Payment (Test Endpoint) ===");
        var paymentResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"ticketCode\":\"%s\", \"licensePlate\":\"%s\", \"amount\":100.0, \"paymentMethod\":\"CARD\", \"transactionId\":\"TXN-E2E-%d\"}",
                        ticketCode, licensePlate, System.currentTimeMillis()))
                .when()
                .post("/api/v1/billing/pay-test")
                .then()
                .log().all()
                .statusCode(201)
                .body("paymentId", notNullValue())
                .body("status", equalTo("COMPLETED"))
                .body("parkingEventId", notNullValue())
                .extract();

        Integer paymentId = paymentResponse.path("paymentId");
        Integer actualParkingEventId = paymentResponse.path("parkingEventId");

        System.out.println("Payment ID: " + paymentId);
        System.out.println("Actual Parking Event ID from payment: " + actualParkingEventId);

        // Step 4: Verify Payment Status (should be paid)
        System.out.println("\n=== Step 4: Verify Payment Status ===");
        given()
                .queryParam("parkingEventId", actualParkingEventId)
                .when()
                .get("/api/v1/billing/status")
                .then()
                .log().all()
                .statusCode(200)
                .body("isPaid", equalTo(true))
                .body("parkingEventId", equalTo(actualParkingEventId));

        // Step 5: Successful Exit
        System.out.println("\n=== Step 5: Successful Exit ===");
        var exitResponse = given()
                .contentType(ContentType.JSON)
                .body("{\"ticketCode\":\"" + ticketCode + "\", \"licensePlate\":\"" + licensePlate + "\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}")
                .when()
                .post("/api/v1/gate/exit")
                .then()
                .log().all();

        // Check status code
        int statusCode = exitResponse.extract().statusCode();
        if (statusCode != 200) {
            System.err.println("❌ Exit failed with status: " + statusCode);
            System.err.println("Response body: " + exitResponse.extract().body().asString());
        }

        exitResponse.statusCode(200)
                .body("gateStatus", equalTo("OPENED"))
                .body("isPaid", equalTo(true))
                .body("paymentRequired", equalTo(false))
                .body("message", notNullValue());

        System.out.println("\n=== Test Completed Successfully (5 steps) ===");
    }

    @AfterAll
    static void tearDown() {
        if (environment != null) {
            environment.stop();
        }
    }
}
