package com.lafl.user.service;

import java.util.Set;

public interface TokenService {

    String issueToken(String subject, Set<String> roles);
}
