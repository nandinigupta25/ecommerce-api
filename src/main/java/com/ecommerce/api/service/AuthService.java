package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.LoginRequest;
import com.ecommerce.api.dto.request.RegisterRequest;
import com.ecommerce.api.dto.response.AuthResponse;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Register a new customer account.
     *
     * @param request registration details
     * @return JWT token and user info
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and return a JWT token.
     *
     * @param request login credentials
     * @return JWT token and user info
     */
    AuthResponse login(LoginRequest request);
}
