package org.example.spring_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.ArtistEarningDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.ArtistEarningService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/earnings")
@RequiredArgsConstructor
public class ArtistEarningController {

    private final ArtistEarningService artistEarningService;

    @GetMapping
    public ResponseEntity<Page<ArtistEarningDTO.ArtistEarningResponse>> getMyEarnings(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(artistEarningService.findByArtist(
                CustomUserDetails.extractId(userDetails), pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<ArtistEarningDTO.ArtistEarningSummary> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(artistEarningService.getSummary(
                CustomUserDetails.extractId(userDetails)));
    }
}