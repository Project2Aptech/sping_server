package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor

public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserDTO.UserPublicResponse>> getAll(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(query, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO.UserSelfResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.findSelf(CustomUserDetails.extractId(userDetails)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.UserPublicResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findPublicById(id));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDTO.UserSelfResponse> updateMe(
            @RequestBody UserDTO.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateSelf(CustomUserDetails.extractId(userDetails), request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO.UserSelfResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        return ResponseEntity.ok(userService.updateSelf(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO.UserDetailResponse> updateRole(
            @PathVariable Integer id,
            @RequestBody UserDTO.AdminUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(id, request));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<UserDTO.UserSelfResponse> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(userService.uploadAvatar(
                CustomUserDetails.extractId(userDetails), file));
    }
}