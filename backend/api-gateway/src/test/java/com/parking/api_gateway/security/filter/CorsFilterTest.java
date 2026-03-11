package com.parking.api_gateway.security.filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
/**
 * Unit tests for CorsFilter (Issue #79).
 *
 * Verifies that:
 * - OPTIONS preflight from an allowed origin returns 200 with correct CORS headers
 *   and does NOT invoke the downstream filter chain (SecurityFilter is bypassed).
 * - A regular GET from an allowed origin receives CORS headers and passes through.
 * - A request from an unknown origin does NOT receive CORS headers.
 */
@DisplayName("CorsFilter - CORS preflight and header injection (Issue #79)")
class CorsFilterTest {
    private CorsFilter corsFilter;
    @BeforeEach
    void setUp() {
        corsFilter = new CorsFilter();
        // Inject @Value field that Spring would normally set
        ReflectionTestUtils.setField(corsFilter, "corsAllowedOrigins",
                "http://localhost:5173,http://localhost:3000,http://192.168.*,null");
    }
    // -----------------------------------------------------------------------
    // Test 1: OPTIONS preflight from allowed origin -> 200, correct headers,
    //         filter chain NOT invoked
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("OPTIONS /api/auth/login from localhost:5173 returns 200 with CORS headers")
    void preflight_allowedOrigin_returns200WithCorsHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "http://localhost:5173");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "Authorization, Content-Type");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(request, response, chain);
        // Status must be 200
        assertThat(response.getStatus()).isEqualTo(200);
        // Correct CORS headers
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
        assertThat(response.getHeader("Access-Control-Allow-Methods")).contains("POST");
        assertThat(response.getHeader("Access-Control-Allow-Headers")).contains("Authorization");
        assertThat(response.getHeader("Access-Control-Expose-Headers")).isEqualTo("Authorization");
        assertThat(response.getHeader("Access-Control-Max-Age")).isEqualTo("3600");
        // Filter chain must NOT have been invoked (SecurityFilter bypassed)
        assertThat(chain.getRequest()).isNull();
    }
    // -----------------------------------------------------------------------
    // Test 2: OPTIONS preflight from localhost:3000 also passes
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("OPTIONS preflight from localhost:3000 returns 200")
    void preflight_port3000_returns200() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/gate/entry");
        request.addHeader("Origin", "http://localhost:3000");
        request.addHeader("Access-Control-Request-Method", "POST");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(request, response, chain);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:3000");
        assertThat(chain.getRequest()).isNull();
    }
    // -----------------------------------------------------------------------
    // Test 3: Regular GET from allowed origin — headers added, chain invoked
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("GET from allowed origin adds CORS headers and passes to filter chain")
    void get_allowedOrigin_addsCorsHeadersAndPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/management/spots/available");
        request.addHeader("Origin", "http://localhost:5173");
        request.addHeader("Authorization", "Bearer some.jwt.token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(request, response, chain);
        // CORS headers present
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("http://localhost:5173");
        // Filter chain WAS invoked
        assertThat(chain.getRequest()).isNotNull();
    }
    // -----------------------------------------------------------------------
    // Test 4: Request from unknown origin — no CORS headers, chain still invoked
    // -----------------------------------------------------------------------
    @Test
    @DisplayName("Request from unknown origin does not add CORS headers")
    void request_unknownOrigin_noCorsHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        request.addHeader("Origin", "http://evil.example.com");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        corsFilter.doFilter(request, response, chain);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
        // Non-OPTIONS still passes to filter chain
        assertThat(chain.getRequest()).isNotNull();
    }

    // -----------------------------------------------------------------------
    // Test 5: OPTIONS preflight from null origin (file:// — devops/test-login.html)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("OPTIONS preflight from null origin (file://) returns 200 with CORS headers")
    void preflight_nullOrigin_returns200WithCorsHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
        request.addHeader("Origin", "null");  // browser sends literal "null" for file://
        request.addHeader("Access-Control-Request-Method", "POST");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        corsFilter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo("null");
        assertThat(chain.getRequest()).isNull(); // SecurityFilter bypassed
    }
}