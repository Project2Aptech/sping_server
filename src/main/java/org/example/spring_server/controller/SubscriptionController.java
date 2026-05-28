package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SubscriptionDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<Page<SubscriptionDTO.SubscriptionResponse>> getMySubscriptions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(subscriptionService.findByUser(
                CustomUserDetails.extractId(userDetails), pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Boolean> hasActive(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subscriptionService.hasActiveSubscription(
                CustomUserDetails.extractId(userDetails)));
    }

    @PostMapping
    public ResponseEntity<SubscriptionDTO.SubscriptionResponse> subscribe(
            @Valid @RequestBody SubscriptionDTO.SubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subscriptionService.subscribe(
                CustomUserDetails.extractId(userDetails), request));
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<SubscriptionDTO.SubscriptionResponse> cancel(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(subscriptionService.cancel(
                CustomUserDetails.extractId(userDetails)));
    }
}