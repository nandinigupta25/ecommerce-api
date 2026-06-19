package com.ecommerce.api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Role entity representing user roles in the system.
 * Roles control access to different API endpoints.
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Role name enum — stored as string in DB.
     * Values: ROLE_ADMIN, ROLE_CUSTOMER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private ERole name;

    public enum ERole {
        ROLE_ADMIN,
        ROLE_CUSTOMER
    }
}
