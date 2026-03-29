package com.lafl.user.service;

import com.lafl.user.api.AuthResponse;
import com.lafl.user.api.LoginRequest;
import com.lafl.user.api.SignupRequest;
import com.lafl.user.domain.UserAccount;
import com.lafl.user.event.UserEventPublisher;
import com.lafl.user.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserAuthService {

    private static final long TOKEN_EXPIRY_SECONDS = 86_400L;

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserAccountRepository userAccountRepository;
    private final UserEventPublisher userEventPublisher;

    public UserAuthService(PasswordEncoder passwordEncoder,
                           TokenService tokenService,
                           UserAccountRepository userAccountRepository,
                           UserEventPublisher userEventPublisher) {
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.userAccountRepository = userAccountRepository;
        this.userEventPublisher = userEventPublisher;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with that email already exists.");
        }

        Set<String> roles = Set.of("ROLE_CLIENT");
        UserAccount user = new UserAccount(
            UUID.randomUUID().toString(),
            request.name().trim(),
            email,
            request.company().trim(),
            request.phone() == null ? "" : request.phone().trim(),
            request.interest() == null ? "" : request.interest().trim(),
            passwordEncoder.encode(request.password()),
            roles
        );

        UserAccount saved = userAccountRepository.save(user);
        String token = tokenService.issueToken(saved.getId(), roles);
        userEventPublisher.publishUserRegistered(saved);
        return new AuthResponse(token, "Bearer", TOKEN_EXPIRY_SECONDS, saved.getEmail(), roles);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserAccount user = userAccountRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = tokenService.issueToken(user.getId(), user.getRoles());
        return new AuthResponse(token, "Bearer", TOKEN_EXPIRY_SECONDS, user.getEmail(), user.getRoles());
    }

    long registeredUserCount() {
        return userAccountRepository.count();
    }

    private String normalizeEmail(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
