package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Get all orders for a specific user, newest first */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Paginated orders for admin */
    Page<Order> findAll(Pageable pageable);

    /** Find a specific order belonging to a user (security check) */
    Optional<Order> findByIdAndUserId(Long orderId, Long userId);
}
