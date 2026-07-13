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
import static org.hamcrest.Matchers.nullValue;
/**
 * E2E test: Subscriber full parking cycle (Issue #73).
 *
 * Pre-condition: licensePlate=AA1234BB is seeded in init.sql with
 * an active ANNUAL subscription (client_id=1, is_active=true, end_date = NOW + 1 year).
 *
 * Scenario (3 steps):
 *   1. POST /api/gate/entry  → 201, isSubscriber=true, gateStatus=OPENED, no ticketCode
 *   2. POST /api/gate/exit   → 200, paymentRequired=false, gateStatus=OPENED
 *   3. Confirm no payment record exists for this plate (billing returns 404 by ticket)
 */
public class SubscriberE2ETest {
    private static final String SUBSCRIBER_PLATE = "AA1234BB";
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
        System.out.println("Subscriber E2E - API Gateway URL: " + apiGatewayUrl);
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
        // Gate Control Service — docker-compose guarantees client-service is healthy
        // before gate-control-service starts (depends_on: client-service: service_healthy)
        System.out.println("Waiting for Gate Control Service...");
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
        System.out.println("All services are ready for subscriber E2E test");
    }
    @Test
    void subscriberFullCycle() {
        // ----------------------------------------------------------------
        // Step 1: Subscriber Entry — gate opens, no ticket issued
        // ----------------------------------------------------------------
        System.out.println("\n=== Step 1: Subscriber Entry ===");
        System.out.println("License Plate: " + SUBSCRIBER_PLATE);
        var entryResponse = given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"licensePlate\":\"%s\", \"entryMethod\":\"SCAN\", \"gateId\":\"ENTRY-1\"}",
                        SUBSCRIBER_PLATE))
                .log().all()
                .when()
                .post("/api/gate/entry")
                .then()
                .log().all()
                .statusCode(201)
                .body("gateStatus",    equalTo("OPENED"))
                .body("licensePlate",  equalTo(SUBSCRIBER_PLATE))
                .body("isSubscriber",  equalTo(true))
                // ticketCode must be absent (JsonNullable.undefined serialises as absent key)
                .body("ticketCode",    nullValue())
                .extract();
        Long parkingEventId = entryResponse.path("parkingEventId") == null
                ? null : ((Number) entryResponse.path("parkingEventId")).longValue();
        System.out.println("Parking event ID: " + parkingEventId);
        // ----------------------------------------------------------------
        // Step 2: Subscriber Exit — no payment required
        // ----------------------------------------------------------------
        System.out.println("\n=== Step 2: Subscriber Exit ===");
        given()
                .contentType(ContentType.JSON)
                .body(String.format(
                        "{\"licensePlate\":\"%s\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}",
                        SUBSCRIBER_PLATE))
                .log().all()
                .when()
                .post("/api/gate/exit")
                .then()
                .log().all()
                .statusCode(200)
                .body("gateStatus",       equalTo("OPENED"))
                .body("paymentRequired",  equalTo(false))
                .body("isPaid",           equalTo(true));
        // ----------------------------------------------------------------
        // Step 3: Confirm no payment record exists (billing has no ticket for subscriber)
        // ----------------------------------------------------------------
        System.out.println("\n=== Step 3: Confirm no billing record for subscriber ===");
        // Subscribers never get a ticketCode, so billing should have no record.
        // We verify by querying with parkingEventId — billing-service returns 404
        // when no payment record exists for the given event.
        given()
                .queryParam("parkingEventId", parkingEventId)
                .when()
                .get("/api/billing/status")
                .then()
                .log().all()
                .statusCode(404);
        System.out.println("\n=== Subscriber E2E Test Completed Successfully (3 steps) ===");
    }
    @AfterAll
    static void tearDown() {
        if (environment != null) {
            environment.stop();
        }
    }
}