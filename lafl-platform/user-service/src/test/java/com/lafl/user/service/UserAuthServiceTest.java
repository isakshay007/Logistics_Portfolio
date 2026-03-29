package com.lafl.user.service;

import com.lafl.user.api.LoginRequest;
import com.lafl.user.api.SignupRequest;
import com.lafl.user.domain.UserAccount;
import com.lafl.user.event.UserEventPublisher;
import com.lafl.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAuthServiceTest {

    private UserAccountRepository repository;
    private UserEventPublisher userEventPublisher;
    private UserAuthService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserAccountRepository.class);
        userEventPublisher = mock(UserEventPublisher.class);
        service = new UserAuthService(
            new BCryptPasswordEncoder(),
            (subject, roles) -> "token-for-" + subject,
            repository,
            userEventPublisher);
    }

    @Test
    void signupRegistersUserAndReturnsBearerToken() {
        when(repository.existsByEmail("akshay@example.com")).thenReturn(false);
        when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.signup(new SignupRequest(
            "Akshay User", "akshay@example.com", "LAFL", "+1", "Shipment Visibility", "password123"
        ));

        assertEquals("Bearer", response.tokenType());
        assertEquals("akshay@example.com", response.email());
        assertEquals(86_400L, response.expiresIn());
        verify(userEventPublisher).publishUserRegistered(any(UserAccount.class));
    }

    @Test
    void loginReturnsJwtWhenPasswordIsCorrect() {
        String encoded = new BCryptPasswordEncoder().encode("password123");
        UserAccount existing = new UserAccount(
            "user-1", "Akshay User", "akshay@example.com", "LAFL", "+1", "Shipment Visibility", encoded,
            Set.of("ROLE_USER")
        );
        when(repository.findByEmail("akshay@example.com")).thenReturn(Optional.of(existing));

        var response = service.login(new LoginRequest("akshay@example.com", "password123"));

        assertEquals("Bearer", response.tokenType());
        assertEquals("token-for-user-1", response.token());
        assertEquals(86_400L, response.expiresIn());
    }

    @Test
    void loginRejectsWrongPassword() {
        String encoded = new BCryptPasswordEncoder().encode("password123");
        UserAccount existing = new UserAccount(
            "user-1", "Akshay User", "akshay@example.com", "LAFL", "+1", "Shipment Visibility", encoded,
            Set.of("ROLE_CLIENT")
        );
        when(repository.findByEmail("akshay@example.com")).thenReturn(Optional.of(existing));

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
            () -> service.login(new LoginRequest("akshay@example.com", "wrong-pass")));

        assertEquals("Invalid email or password.", exception.getMessage());
    }
}
