package com.parking.api_gateway.observability.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(value = "management.tracing.enabled", havingValue = "true", matchIfMissing = false)
@Slf4j
public class OpenTelemetryConfig {

    @Value("${management.otlp.tracing.endpoint:http://parking_otel_collector:4318/v1/traces}")
    private String otlpEndpoint;
    
    @Value("${spring.application.name:api-gateway}")
    private String serviceName;
    
    @Value("${management.tracing.service.version:1.0.0}")
    private String serviceVersion;

    @Bean
    public OpenTelemetry openTelemetry() {
        log.info("Configuring OpenTelemetry for service: {} version: {} endpoint: {}",
                 serviceName, serviceVersion, otlpEndpoint);

        Resource resource = Resource.getDefault()
            .merge(Resource.create(
                Attributes.builder()
                    .put("service.name", serviceName)
                    .put("service.version", serviceVersion)
                    .put("deployment.environment",
                         System.getProperty("spring.profiles.active", "development"))
                    .build()));

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(
                createSpanExporter())
                .build())
            .setResource(resource)
            .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }

    private io.opentelemetry.sdk.trace.export.SpanExporter createSpanExporter() {
        log.info("Configuring OTLP HTTP exporter to: {}", otlpEndpoint);
        return OtlpHttpSpanExporter.builder()
            .setEndpoint(otlpEndpoint)
            .build();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, serviceVersion);
    }
}