package org.example.spring_server.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.config.VNPayConfig;
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
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VNPayService vnPayService;

    // Pricing in VND
    private static final Map<enumeration.PlanType, Long> VND_PRICES = Map.of(
            enumeration.PlanType.PRO,     49000L,
            enumeration.PlanType.PREMIUM, 99000L
    );

    public String createPaymentUrl(Integer userId,
                                   enumeration.PlanType planType,
                                   HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // txnRef encodes userId + planType so we can decode it on return
        String txnRef   = userId + "_" + planType.name() + "_" + VNPayConfig.getRandomNumber(6);
        long amount     = VND_PRICES.get(planType);
        String orderInfo = "Nang cap " + planType.name() + " cho " + user.getUsername();

        return vnPayService.createPaymentUrl(amount, txnRef, orderInfo, request);
    }

    public void handleVNPayReturn(Map<String, String> params) {
        // 1. Verify signature
        if (!vnPayService.verifyReturn(params))
            throw new RuntimeException("Invalid VNPAY signature");

        // 2. Check payment response code — "00" = success
        if (!"00".equals(params.get("vnp_ResponseCode"))) return;

        // 3. Decode txnRef → userId + planType
        String[] parts  = params.get("vnp_TxnRef").split("_");
        Integer userId  = Integer.parseInt(parts[0]);
        enumeration.PlanType planType = enumeration.PlanType.valueOf(parts[1]);

        // 4. Reuse existing subscribe logic
        subscribe(userId, new SubscriptionDTO.SubscriptionRequest(planType));
    }

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

        enumeration.AccountType newType = request.planType() == enumeration.PlanType.PRO
                ? enumeration.AccountType.PRO
                : enumeration.AccountType.PREMIUM;
        user.setAccountType(newType);
        userRepository.save(user);

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

    @Transactional
    public void checkAndExpireIfNeeded(Integer userId) {
        subscriptionRepository
                .findByUserIdAndStatus(userId, enumeration.SubscriptionStatus.ACTIVE)
                .ifPresent(sub -> {
                    if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
                        sub.setStatus(enumeration.SubscriptionStatus.EXPIRED);
                        subscriptionRepository.save(sub);

                        User user = sub.getUser();
                        user.setAccountType(enumeration.AccountType.NORMAL);
                        userRepository.save(user);
                    }
                });
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.SubscriptionResponse> findAllAdmin(Pageable pageable) {
        return subscriptionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionDTO.SubscriptionResponse> findByUserAdmin(Integer userId, Pageable pageable) {
        return subscriptionRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
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