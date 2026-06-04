package org.example.spring_server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.security.CustomUserDetails;
import org.example.spring_server.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<UserDTO.UserSummary>> getAll(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(query, pageable));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO.UserDetailResponse> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.findSelf(CustomUserDetails.extractId(userDetails)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO.UserDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.findPublicById(id));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserDTO.UserDetailResponse> updateMe(
            @RequestBody UserDTO.UpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.updateSelf(CustomUserDetails.extractId(userDetails), request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO.UserDetailResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UserDTO.UpdateRequest request) {
        return ResponseEntity.ok(userService.updateSelf(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<UserDTO.UserDetailResponse> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        return ResponseEntity.ok(userService.uploadAvatar(
                CustomUserDetails.extractId(userDetails), file));
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<Page<UserDTO.UserSummary>> getUsersByRole(
            @PathVariable enumeration.UserRole role,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.findByRole(role, pageable));
    }
}