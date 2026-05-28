package org.example.spring_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.LikedSongDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.LikedSongService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/liked-songs")
@RequiredArgsConstructor
public class LikedSongController {

    private final LikedSongService likedSongService;

    @GetMapping
    public ResponseEntity<Page<LikedSongDTO.LikedSongResponse>> getMyLikedSongs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(likedSongService.findByUser(getUserId(userDetails), pageable));
    }

    @GetMapping("/{songId}/status")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Integer songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(likedSongService.isLiked(getUserId(userDetails), songId));
    }

    @PostMapping("/{songId}")
    public ResponseEntity<Void> like(
            @PathVariable Integer songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        likedSongService.like(getUserId(userDetails), songId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> unlike(
            @PathVariable Integer songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        likedSongService.unlike(getUserId(userDetails), songId);
        return ResponseEntity.noContent().build();
    }

    private Integer getUserId(UserDetails userDetails) {
        return CustomUserDetails.extractId(userDetails);
    }
}