package com.lafl.user.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Service
public class JwtTokenService implements TokenService {

    private final SecretKey signingKey;
    private final long ttlSeconds;

    public JwtTokenService(@Value("${jwt.secret}") String secret,
                           @Value("${jwt.ttl-seconds:86400}") long ttlSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public String issueToken(String subject, Set<String> roles) {
        Instant now = Instant.now();
        String role = roles == null || roles.isEmpty() ? "" : roles.iterator().next();
        return Jwts.builder()
            .subject(subject)
            .claim("role", role)
            .claim("roles", roles)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(ttlSeconds)))
            .signWith(signingKey, SignatureAlgorithm.HS256)
            .compact();
    }
}
