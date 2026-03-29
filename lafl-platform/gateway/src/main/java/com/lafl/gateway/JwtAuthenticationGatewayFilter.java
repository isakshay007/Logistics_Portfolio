package com.lafl.gateway;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

@Component
public class JwtAuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    private final SecretKey signingKey;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationGatewayFilter(@Value("${jwt.secret}") String jwtSecret) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        HttpMethod method = exchange.getRequest().getMethod();
        String path = exchange.getRequest().getURI().getPath();
        boolean requiresOpsRole = requiresOpsRole(method, path);
        boolean requiresValidJwt = requiresValidJwt(method, path);

        if (method == null || HttpMethod.OPTIONS.equals(method) || isPublicRoute(method, path)
            || (!requiresOpsRole && !requiresValidJwt)) {
            return chain.filter(exchange);
        }

        Claims claims = parseAndValidateClaims(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (claims == null || claims.getSubject() == null || claims.getSubject().isBlank()) {
            return writeStatus(exchange, HttpStatus.UNAUTHORIZED);
        }

        String role = extractRole(claims);
        if (requiresOpsRole && !hasRole(claims, "ROLE_OPS")) {
            return writeStatus(exchange, HttpStatus.FORBIDDEN);
        }

        ServerHttpRequest forwardedRequest = exchange.getRequest()
            .mutate()
            .header(USER_ID_HEADER, claims.getSubject())
            .header(USER_ROLE_HEADER, role)
            .build();
        return chain.filter(exchange.mutate().request(forwardedRequest).build());
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicRoute(HttpMethod method, String path) {
        if (pathMatcher.match("/actuator/**", path)) {
            return true;
        }
        return (HttpMethod.GET.equals(method) && pathMatcher.match("/api/v1/shipments/track", path))
            || (HttpMethod.POST.equals(method) && pathMatcher.match("/api/v1/quotes/**", path))
            || (HttpMethod.POST.equals(method) && pathMatcher.match("/api/v1/contacts/**", path))
            || (HttpMethod.POST.equals(method) && pathMatcher.match("/api/v1/auth/**", path));
    }

    private boolean requiresValidJwt(HttpMethod method, String path) {
        return HttpMethod.POST.equals(method) && pathMatcher.match("/api/v1/shipments/**", path);
    }

    private boolean requiresOpsRole(HttpMethod method, String path) {
        return (HttpMethod.GET.equals(method) && pathMatcher.match("/api/v1/ops/**", path))
            || (HttpMethod.POST.equals(method) && pathMatcher.match("/api/v1/notifications/**", path));
    }

    private Claims parseAndValidateClaims(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isBlank()) {
            return null;
        }

        try {
            return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null;
        }
    }

    private String extractRole(Claims claims) {
        Object roleClaim = claims.get("role");
        if (roleClaim instanceof String role && !role.isBlank()) {
            return role;
        }

        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> roles && !roles.isEmpty()) {
            Object first = roles.iterator().next();
            if (first instanceof String role && !role.isBlank()) {
                return role;
            }
        }

        if (rolesClaim instanceof String role && !role.isBlank()) {
            return role;
        }

        return "";
    }

    private boolean hasRole(Claims claims, String requiredRole) {
        Object roleClaim = claims.get("role");
        if (requiredRole.equals(roleClaim)) {
            return true;
        }

        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> roles) {
            for (Object role : roles) {
                if (requiredRole.equals(role)) {
                    return true;
                }
            }
        }

        return requiredRole.equals(rolesClaim);
    }

    private Mono<Void> writeStatus(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
