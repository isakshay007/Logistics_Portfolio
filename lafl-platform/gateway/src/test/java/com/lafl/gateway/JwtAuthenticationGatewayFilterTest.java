package com.lafl.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationGatewayFilterTest {

    private static final String SECRET = "lafl-gateway-test-secret-2026-abcdef123456";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private JwtAuthenticationGatewayFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationGatewayFilter(SECRET);
        chain = mock(GatewayFilterChain.class);
    }

    @Test
    void validTokenPassesForShipmentPost() {
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        String token = issueToken("user-1", "ROLE_USER");
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/shipments/create")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());
        ServerHttpRequest forwardedRequest = exchangeCaptor.getValue().getRequest();
        assertEquals("user-1", forwardedRequest.getHeaders().getFirst("X-User-Id"));
        assertEquals("ROLE_USER", forwardedRequest.getHeaders().getFirst("X-User-Role"));
    }

    @Test
    void missingTokenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/shipments/create").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void roleUserOnOpsRouteReturnsForbidden() {
        String token = issueToken("user-1", "ROLE_USER");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/ops/overview")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void roleOpsOnOpsRouteReturnsOk() {
        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange exchange = invocation.getArgument(0);
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        });

        String token = issueToken("ops-1", "ROLE_OPS");
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/ops/overview")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void notificationTriggerMissingTokenReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/notifications/trigger").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void roleUserOnNotificationTriggerReturnsForbidden() {
        String token = issueToken("user-1", "ROLE_USER");
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/notifications/trigger")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertEquals(HttpStatus.FORBIDDEN, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void roleOpsOnNotificationTriggerReturnsOk() {
        when(chain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
            ServerWebExchange exchange = invocation.getArgument(0);
            exchange.getResponse().setStatusCode(HttpStatus.OK);
            return exchange.getResponse().setComplete();
        });

        String token = issueToken("ops-1", "ROLE_OPS");
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/notifications/trigger")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
        verify(chain).filter(any(ServerWebExchange.class));
    }

    private String issueToken(String subject, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(subject)
            .claim("role", role)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(86_400)))
            .signWith(KEY, SignatureAlgorithm.HS256)
            .compact();
    }
}
