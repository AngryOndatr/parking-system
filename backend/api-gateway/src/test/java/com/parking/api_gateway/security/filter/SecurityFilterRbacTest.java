package com.parking.api_gateway.security.filter;

import com.parking.api_gateway.observability.service.ObservabilityService;
import com.parking.api_gateway.security.service.JwtTokenService;
import com.parking.api_gateway.security.service.SecurityAuditService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RBAC route protection in SecurityFilter (Issue #78).
 *
 * <p>The filter bypasses security for internal Docker IPs (127.0.0.1),
 * so these tests set a non-internal remote address to exercise the full
 * JWT + RBAC pipeline.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityFilter — RBAC route protection (Issue #78)")
class SecurityFilterRbacTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private SecurityAuditService auditService;

    @Mock
    private ObservabilityService observabilityService;

    private SecurityFilter securityFilter;

    /** A routable non-Docker IP so the filter does NOT bypass security. */
    private static final String EXTERNAL_IP = "203.0.113.5";

    /** Dummy Bearer token value – actual validation is mocked. */
    private static final String BEARER_TOKEN = "Bearer dummy.jwt.token";

    @BeforeEach
    void setUp() {
        securityFilter = new SecurityFilter(jwtTokenService, auditService, observabilityService);
    }

    // -----------------------------------------------------------------------
    // isRoleAllowed — pure unit tests (no HTTP round-trip needed)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("OPERATOR is denied on admin-only management write route")
    void operatorDeniedOnManagementWriteRoute() {
        // OPERATOR is not in {ADMIN, MANAGER} for POST /api/management/
        boolean allowed = securityFilter.isRoleAllowed("POST", "/api/management/spots/123", "OPERATOR");
        assertThat(allowed).isFalse();
    }

    @Test
    @DisplayName("ADMIN is allowed on every protected route")
    void adminAllowedOnAllProtectedRoutes() {
        assertThat(securityFilter.isRoleAllowed("POST",   "/api/gate/entry",        "ADMIN")).isTrue();
        assertThat(securityFilter.isRoleAllowed("POST",   "/api/billing/pay",        "ADMIN")).isTrue();
        assertThat(securityFilter.isRoleAllowed("GET",    "/api/clients/1",             "ADMIN")).isTrue();
        assertThat(securityFilter.isRoleAllowed("DELETE", "/api/clients/1",             "ADMIN")).isTrue();
        assertThat(securityFilter.isRoleAllowed("POST",   "/api/management/spots",      "ADMIN")).isTrue();
        assertThat(securityFilter.isRoleAllowed("GET",    "/api/reporting/logs",        "ADMIN")).isTrue();
    }

    // -----------------------------------------------------------------------
    // Full HTTP pipeline — 403 for insufficient role
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("OPERATOR receives 403 when calling management write endpoint")
    void operatorGets403OnManagementWrite() throws Exception {
        Claims claims = buildClaims("operator_user", "OPERATOR");
        when(jwtTokenService.validateAccessToken(anyString(), anyString()))
                .thenReturn(Mono.just(claims));

        MockHttpServletRequest request = buildRequest("POST", "/api/management/spots", EXTERNAL_IP);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("Access denied");
    }

    // -----------------------------------------------------------------------
    // Full HTTP pipeline — 200 for ADMIN on all routes
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("ADMIN passes through management write endpoint")
    void adminPassesManagementWrite() throws Exception {
        Claims claims = buildClaims("admin_user", "ADMIN");
        when(jwtTokenService.validateAccessToken(anyString(), anyString()))
                .thenReturn(Mono.just(claims));

        MockHttpServletRequest request = buildRequest("POST", "/api/management/spots", EXTERNAL_IP);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        securityFilter.doFilterInternal(request, response, chain);

        // Filter chain was invoked → response status defaults to 200
        assertThat(response.getStatus()).isEqualTo(200);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Claims buildClaims(String username, String role) {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(username);
        when(claims.get("userId")).thenReturn(1L);
        when(claims.get("role")).thenReturn(role);
        return claims;
    }

    private static MockHttpServletRequest buildRequest(String method, String path, String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteAddr);
        request.addHeader("Authorization", BEARER_TOKEN);
        return request;
    }
}
