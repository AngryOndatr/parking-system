package com.parking.api_gateway.controller;

import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility for proxy controllers.
 *
 * Problem: RestTemplate (SimpleClientHttpRequestFactory / HttpURLConnection) uses
 * HTTP/1.1 chunked responses from downstream services. When we copy
 * Transfer-Encoding: chunked (and Content-Encoding: gzip) from the downstream
 * response headers back to our own ResponseEntity, Spring Boot's servlet layer adds
 * its own chunked framing on top → double-chunked → Vite proxy parse error:
 * "Invalid character in chunk size".
 *
 * Fix: strip hop-by-hop headers (RFC 7230 §6.1) from the downstream response before
 * forwarding them to the browser.  Spring will set Transfer-Encoding / Content-Length
 * correctly for the final outgoing response.
 *
 * Also strip Accept-Encoding from forwarded request headers so that RestTemplate's
 * HttpURLConnection can transparently decompress gzip responses (when Accept-Encoding
 * is set explicitly, Java disables auto-decompression → raw gzip bytes forwarded as
 * plain text → mangled body).
 */
public final class ProxyUtils {

    private ProxyUtils() {}

    /** Hop-by-hop headers that must not be forwarded in either direction. */
    private static final Set<String> HOP_BY_HOP = new HashSet<>(Arrays.asList(
            "transfer-encoding",
            "content-encoding",   // backend may gzip; RestTemplate already decoded it
            "content-length",     // Spring will recalculate
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "upgrade"
    ));

    /**
     * Request-side headers that must not be forwarded to the downstream service.
     * Includes all hop-by-hop headers plus accept-encoding (to keep auto-decompression
     * in HttpURLConnection active).
     */
    private static final Set<String> SKIP_REQUEST = new HashSet<>(HOP_BY_HOP);
    static {
        SKIP_REQUEST.add("accept-encoding"); // allow HttpURLConnection to auto-decompress
        SKIP_REQUEST.add("host");            // each hop has its own host
    }

    /**
     * Returns true if the given request header should be forwarded to the downstream
     * service (case-insensitive).
     */
    public static boolean shouldForwardRequestHeader(String name) {
        return !SKIP_REQUEST.contains(name.toLowerCase());
    }

    /**
     * Returns a copy of {@code headers} with all hop-by-hop headers removed.
     * Use this when building the ResponseEntity returned to the client.
     */
    public static HttpHeaders filterResponseHeaders(HttpHeaders headers) {
        HttpHeaders filtered = new HttpHeaders();
        headers.forEach((name, values) -> {
            if (!HOP_BY_HOP.contains(name.toLowerCase())) {
                filtered.put(name, values);
            }
        });
        return filtered;
    }
}

