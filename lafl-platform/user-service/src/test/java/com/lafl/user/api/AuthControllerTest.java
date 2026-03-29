package com.lafl.user.api;

import com.lafl.user.service.InvalidCredentialsException;
import com.lafl.user.service.UserAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserAuthService userAuthService;

    @Test
    void signupReturnsCreated() throws Exception {
        when(userAuthService.signup(any())).thenReturn(
            new AuthResponse("token", "Bearer", 86_400, "user@example.com", Set.of("ROLE_CLIENT")));

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"name\":\"User\"," +
                    "\"email\":\"user@example.com\"," +
                    "\"company\":\"LAFL\"," +
                    "\"password\":\"password123\"" +
                    "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginReturnsTokenForCorrectPassword() throws Exception {
        when(userAuthService.login(any())).thenReturn(
            new AuthResponse("jwt-token", "Bearer", 86_400, "user@example.com", Set.of("ROLE_USER")));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"email\":\"user@example.com\"," +
                    "\"password\":\"password123\"" +
                    "}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("jwt-token"))
            .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void loginReturnsUnauthorizedForWrongPassword() throws Exception {
        when(userAuthService.login(any()))
            .thenThrow(new InvalidCredentialsException("Invalid email or password."));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{" +
                    "\"email\":\"user@example.com\"," +
                    "\"password\":\"wrong-pass\"" +
                    "}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }
}
