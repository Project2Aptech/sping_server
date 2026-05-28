package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SubscriptionDTO;
import org.example.spring_server.entity.Subscription;
import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.repository.SubscriptionRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final BigDecimal PRO_PRICE     = new BigDecimal("4.99");
    private static final BigDecimal PREMIUM_PRICE = new BigDecimal("9.99");

    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.SubscriptionResponse> findByUser(Integer userId, Pageable pageable) {
        return subscriptionRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(Integer userId) {
        return subscriptionRepository.existsByUserIdAndStatusAndExpiresAtAfter(
                userId, enumeration.SubscriptionStatus.ACTIVE, LocalDateTime.now());
    }

    public SubscriptionDTO.SubscriptionResponse subscribe(Integer userId,
                                                          SubscriptionDTO.SubscriptionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // cancel any existing active subscription first
        subscriptionRepository.findByUserIdAndStatus(userId, enumeration.SubscriptionStatus.ACTIVE)
                .ifPresent(existing -> {
                    existing.setStatus(enumeration.SubscriptionStatus.CANCELLED);
                    subscriptionRepository.save(existing);
                });

        BigDecimal amount = request.planType() == enumeration.PlanType.PRO
                ? PRO_PRICE : PREMIUM_PRICE;

        LocalDateTime now = LocalDateTime.now();

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanType(request.planType());
        subscription.setAmount(amount);
        subscription.setStartedAt(now);
        subscription.setExpiresAt(now.plusMonths(1));
        subscription.setStatus(enumeration.SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(subscription);

        // update user account type
        enumeration.AccountType newType = request.planType() == enumeration.PlanType.PRO
                ? enumeration.AccountType.PRO
                : enumeration.AccountType.PREMIUM;
        user.setAccountType(newType);
        userRepository.save(user);

        // send confirmation email
        emailService.sendSubscriptionConfirmationEmail(
                user.getEmail(),
                user.getDisplayName(),
                request.planType().name(),
                subscription.getExpiresAt().toLocalDate().toString()
        );

        return toResponse(subscription);
    }

    public SubscriptionDTO.SubscriptionResponse cancel(Integer userId) {
        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, enumeration.SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active subscription found"));

        subscription.setStatus(enumeration.SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);

        // downgrade user account type
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setAccountType(enumeration.AccountType.NORMAL);
        userRepository.save(user);

        // send cancellation email
        emailService.sendSubscriptionCancelledEmail(user.getEmail(), user.getDisplayName());

        return toResponse(subscription);
    }

    private SubscriptionDTO.SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionDTO.SubscriptionResponse(
                s.getId(),
                s.getUser().getId(),
                s.getPlanType(),
                s.getAmount(),
                s.getStartedAt(),
                s.getExpiresAt(),
                s.getStatus(),
                s.getCreatedAt()
        );
    }
}