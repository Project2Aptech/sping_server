package org.example.spring_server.controller;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.*;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.service.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final SongService songService;
    private final AlbumService albumService;
    private final PlaylistService playlistService;
    private final ArtistEarningService artistEarningService;

    @GetMapping("/songs")
    public ResponseEntity<Page<SongDTO.SongDetailResponse>> getAllSongs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(songService.findAllWithDetails(pageable));
    }

    @GetMapping("/playlists")
    public ResponseEntity<Page<PlaylistDTO.PlaylistSummaryResponse>> getAllPlaylists(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(playlistService.findAll(pageable));
    }

    @GetMapping
    public ResponseEntity<Page<AlbumDTO.AlbumSummaryResponse>> getAll(
            @PageableDefault(size = 20, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(albumService.findAll(pageable));
    }
    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO.UserDetailResponse>> getAllUsers(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.findAllAdmin(query, pageable));
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserDTO.UserDetailResponse>> getUsersByRole(
            @PathVariable enumeration.UserRole role,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.findByRoleAdmin(role, pageable));
    }

    @PatchMapping("/users/{id}/role")
    public ResponseEntity<UserDTO.UserDetailResponse> updateUserRole(
            @PathVariable Integer id,
            @RequestBody UserDTO.AdminUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<UserDTO.UserDetailResponse> deactivateUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.setActive(id, false));
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<UserDTO.UserDetailResponse> activateUser(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.setActive(id, true));
    }

    @DeleteMapping("/songs/{id}")
    public ResponseEntity<Void> deleteSong(@PathVariable Integer id) {
        songService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/songs/{id}/publish")
    public ResponseEntity<Void> publishSong(@PathVariable Integer id) {
        songService.publish(id, null);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/earnings/{artistId}")
    public ResponseEntity<Page<ArtistEarningDTO.ArtistEarningResponse>> getByArtist(
            @PathVariable Integer artistId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(artistEarningService.findByArtist(artistId, pageable));
    }

    public ResponseEntity<UserDTO.UserDetailResponse> updateRole(
            @PathVariable Integer id,
            @RequestBody UserDTO.AdminUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }
}