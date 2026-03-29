package com.lafl.user.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtTokenServiceTest {

    private static final String SECRET = "lafl-user-service-test-secret-2026-abcdef123456";

    @Test
    void issueTokenContainsSubRoleIatAndExpFor24Hours() {
        JwtTokenService service = new JwtTokenService(SECRET, 86_400);

        String token = service.issueToken("user@example.com", Set.of("ROLE_OPS"));
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertEquals("user@example.com", claims.getSubject());
        assertEquals("ROLE_OPS", claims.get("role"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(86_400L,
            (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000L);
    }
}
