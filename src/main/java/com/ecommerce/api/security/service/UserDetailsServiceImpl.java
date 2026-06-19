package com.ecommerce.api.security.service;

import com.ecommerce.api.entity.User;
import com.ecommerce.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Called by the AuthenticationManager to load a user during login.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username. Spring Security calls this during every authentication.
     *
     * @param username the username submitted in the login form
     * @return a fully populated UserDetails object
     * @throws UsernameNotFoundException if no user with that username exists
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + username));

        return UserDetailsImpl.build(user);
    }
}
