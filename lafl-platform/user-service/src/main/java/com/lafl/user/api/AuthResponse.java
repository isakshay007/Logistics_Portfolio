package com.lafl.user.api;

import java.util.Set;

public record AuthResponse(String token, String tokenType, long expiresIn, String email, Set<String> roles) {
}
