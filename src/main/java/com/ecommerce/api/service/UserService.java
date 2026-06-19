package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.ChangePasswordRequest;
import com.ecommerce.api.dto.request.UpdateProfileRequest;
import com.ecommerce.api.dto.response.OrderResponse;
import com.ecommerce.api.dto.response.UserResponse;

import java.util.List;

/**
 * Service interface for user profile and account management.
 */
public interface UserService {

    /**
     * Get the profile of the currently authenticated user.
     *
     * @param username current user's username
     * @return user profile DTO
     */
    UserResponse getProfile(String username);

    /**
     * Update profile fields (name, phone, address).
     *
     * @param username current user's username
     * @param request  fields to update
     * @return updated user profile
     */
    UserResponse updateProfile(String username, UpdateProfileRequest request);

    /**
     * Change the authenticated user's password after validating the current one.
     *
     * @param username current user's username
     * @param request  current + new + confirm passwords
     */
    void changePassword(String username, ChangePasswordRequest request);

    /**
     * Retrieve all orders placed by the authenticated user.
     *
     * @param username current user's username
     * @return list of orders
     */
    List<OrderResponse> getMyOrders(String username);

    /**
     * Admin: get any user by ID.
     *
     * @param userId target user's ID
     * @return user profile
     */
    UserResponse getUserById(Long userId);

    /**
     * Admin: get all users.
     *
     * @return list of all users
     */
    List<UserResponse> getAllUsers();
}
