package org.example.spring_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.ArtistFollowDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.ArtistFollowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/artists")
@RequiredArgsConstructor
public class ArtistFollowController {

    private final ArtistFollowService artistFollowService;

    @GetMapping("/following")
    public ResponseEntity<Page<ArtistFollowDTO.ArtistFollowResponse>> getFollowing(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                artistFollowService.findFollowing(CustomUserDetails.extractId(userDetails), pageable));
    }

    @GetMapping("/{artistId}/followers/count")
    public ResponseEntity<Long> getFollowerCount(@PathVariable Integer artistId) {
        return ResponseEntity.ok(artistFollowService.getFollowerCount(artistId));
    }

    @GetMapping("/{artistId}/following/status")
    public ResponseEntity<Boolean> isFollowing(
            @PathVariable Integer artistId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                artistFollowService.isFollowing(CustomUserDetails.extractId(userDetails), artistId));
    }

    @PostMapping("/{artistId}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable Integer artistId,
            @AuthenticationPrincipal UserDetails userDetails) {
        artistFollowService.follow(CustomUserDetails.extractId(userDetails), artistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{artistId}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable Integer artistId,
            @AuthenticationPrincipal UserDetails userDetails) {
        artistFollowService.unfollow(CustomUserDetails.extractId(userDetails), artistId);
        return ResponseEntity.noContent().build();
    }
}