package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.PlaylistDTO;
import org.example.spring_server.dto.SongDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.PlaylistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public ResponseEntity<Page<PlaylistDTO.PlaylistSummaryResponse>> getPublic(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(playlistService.findPublic(pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PlaylistDTO.PlaylistSummaryResponse>> getMyPlaylists(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(playlistService.findByUser(
                CustomUserDetails.extractId(userDetails), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDTO.PlaylistResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(playlistService.findById(id));
    }

    @GetMapping("/{id}/songs")
    public ResponseEntity<Page<SongDTO.SongSummaryResponse>> getSongs(
            @PathVariable Integer id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(playlistService.findSongs(id, pageable));
    }

    @PostMapping
    public ResponseEntity<PlaylistDTO.PlaylistResponse> create(
            @Valid @RequestBody PlaylistDTO.PlaylistRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playlistService.create(CustomUserDetails.extractId(userDetails), request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PlaylistDTO.PlaylistResponse> update(
            @PathVariable Integer id,
            @RequestBody PlaylistDTO.PlaylistUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(playlistService.update(
                id, request,
                CustomUserDetails.extractId(userDetails),
                CustomUserDetails.isAdmin(userDetails)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.delete(
                id,
                CustomUserDetails.extractId(userDetails),
                CustomUserDetails.isAdmin(userDetails));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> addSong(
            @PathVariable Integer id,
            @PathVariable Integer songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.addSong(
                id, songId,
                CustomUserDetails.extractId(userDetails),
                CustomUserDetails.isAdmin(userDetails));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/songs/{songId}")
    public ResponseEntity<Void> removeSong(
            @PathVariable Integer id,
            @PathVariable Integer songId,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.removeSong(
                id, songId,
                CustomUserDetails.extractId(userDetails),
                CustomUserDetails.isAdmin(userDetails));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> follow(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.follow(CustomUserDetails.extractId(userDetails), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollow(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails) {
        playlistService.unfollow(CustomUserDetails.extractId(userDetails), id);
        return ResponseEntity.noContent().build();
    }
}