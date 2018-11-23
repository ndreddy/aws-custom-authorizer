package com.ndrd.cloud.service;

public interface AuthorizerService {
    String authenticateUser(String username, String password);

    String authenticateToken(String accessToken);
}
