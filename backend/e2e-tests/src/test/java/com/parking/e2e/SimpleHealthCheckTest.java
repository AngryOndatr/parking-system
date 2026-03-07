package com.parking.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

public class SimpleHealthCheckTest {

    private static DockerComposeContainer<?> environment;

    @BeforeAll
    static void setup() {
        System.out.println("Starting Simple Health Check Test...");

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

        System.out.println("API Gateway URL: " + apiGatewayUrl);

        // Wait for services
        await().atMost(Duration.ofMinutes(3)).pollInterval(Duration.ofSeconds(10)).until(() -> {
            try {
                given().when().get("/actuator/health").then().statusCode(200);
                System.out.println("API Gateway is ready");
                return true;
            } catch (Exception e) {
                System.out.println("API Gateway not ready: " + e.getMessage());
                return false;
            }
        });
    }

    @Test
    void testHealthEndpoint() {
        System.out.println("\n=== Testing Health Endpoint ===");

        given()
                .when()
                .get("/actuator/health")
                .then()
                .log().all()
                .statusCode(200);

        System.out.println("✅ Health endpoint works!");
    }

    @Test
    void testGateEntryEndpoint() {
        System.out.println("\n=== Testing Gate Entry Endpoint ===");

        String requestBody = "{\"licensePlate\":\"TEST-123\", \"entryMethod\":\"SCAN\", \"gateId\":\"ENTRY-1\"}";
        System.out.println("Request body: " + requestBody);

        var response = given()
                .contentType("application/json")
                .body(requestBody)
                .log().all()
                .when()
                .post("/api/v1/gate/entry");

        System.out.println("\nResponse status: " + response.getStatusCode());
        System.out.println("Response body: " + response.asString());

        response.then().log().all();

        // Extract ticket code for testing exit
        String ticketCode = response.jsonPath().getString("ticketCode");
        String licensePlate = response.jsonPath().getString("licensePlate");

        // Test exit without payment
        System.out.println("\n=== Testing Gate Exit Endpoint (without payment) ===");
        String exitRequestBody = "{\"ticketCode\":\"" + ticketCode + "\", \"licensePlate\":\"" + licensePlate + "\", \"exitMethod\":\"SCAN\", \"gateId\":\"EXIT-1\"}";
        System.out.println("Exit request body: " + exitRequestBody);

        var exitResponse = given()
                .contentType("application/json")
                .body(exitRequestBody)
                .log().all()
                .when()
                .post("/api/v1/gate/exit");

        System.out.println("\nExit response status: " + exitResponse.getStatusCode());
        System.out.println("Exit response body: " + exitResponse.asString());

        exitResponse.then().log().all();
    }

    @AfterAll
    static void tearDown() {
        if (environment != null) {
            environment.stop();
        }
    }
}

