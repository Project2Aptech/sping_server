package org.example.spring_server.repository;

import org.example.spring_server.entity.Subscription;
import org.example.spring_server.enums.enumeration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Page<Subscription> findByUserId(Integer userId, Pageable pageable);
    Optional<Subscription> findByUserIdAndStatus(Integer userId, enumeration.SubscriptionStatus status);
    boolean existsByUserIdAndStatusAndExpiresAtAfter(
            Integer userId, enumeration.SubscriptionStatus status, LocalDateTime now);
}