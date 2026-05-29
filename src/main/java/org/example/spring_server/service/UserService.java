package org.example.spring_server.service;

import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.mapper.UserMapper;
import org.example.spring_server.repository.UserRepository;
import org.example.spring_server.service.cloudinary.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CloudinaryService cloudinaryService;

    @Transactional(readOnly = true)
    public UserDTO.UserPublicResponse findPublicById(Integer id) {
        return userRepository.findById(id)
                .map(this::toPublicResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public UserDTO.UserSelfResponse findSelf(Integer currentUserId) {
        return userRepository.findById(currentUserId)
                .map(this::toSelfResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO.UserPublicResponse> findAll(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                            query, query, pageable)
                    .map(this::toPublicResponse);
        }
        return userRepository.findAll(pageable).map(this::toPublicResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO.UserDetailResponse> findAllAdmin(String query, Pageable pageable) {
        if (query != null && !query.isBlank()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
                            query, query, pageable)
                    .map(this::toDetailResponse);
        }
        return userRepository.findAll(pageable).map(this::toDetailResponse);
    }

    public UserDTO.UserDetailResponse updateRole(Integer id, UserDTO.AdminUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (request.role() != null) user.setRole(request.role());
        if (request.accountType() != null) user.setAccountType(request.accountType());

        return toDetailResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO.UserDetailResponse> findByRole(enumeration.UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(this::toDetailResponse);
    }

    public void delete(Integer id) {
        if (!userRepository.existsById(id))
            throw new ResourceNotFoundException("User not found: " + id);
        userRepository.deleteById(id);
    }

    public UserDTO.UserDetailResponse setActive(Integer id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(active);
        return toDetailResponse(userRepository.save(user));
    }

    public void deactivate(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public UserDTO.UserSelfResponse updateSelf(Integer id, UserDTO.UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (request.displayName() != null) user.setDisplayName(request.displayName());
        if (request.bio() != null)         user.setBio(request.bio());
        if (request.avatarUrl() != null)   user.setAvatarUrl(request.avatarUrl());
        if (request.birthDate() != null)   user.setBirthDate(request.birthDate());

        return toSelfResponse(userRepository.save(user));
    }

    public UserDTO.UserSelfResponse uploadAvatar(Integer userId, MultipartFile file)
            throws IOException, java.io.IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String avatarUrl = cloudinaryService.uploadImage(file);
        user.setAvatarUrl(avatarUrl);

        return toSelfResponse(userRepository.save(user));
    }

    private UserDTO.UserDetailResponse toDetailResponse(User user) {
        return new UserDTO.UserDetailResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getBirthDate(),
                user.getRole(),
                user.getAccountType(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    private UserDTO.UserPublicResponse toPublicResponse(User user) {
        return new UserDTO.UserPublicResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getBirthDate()
        );
    }

    private UserDTO.UserSelfResponse toSelfResponse(User user) {
        return new UserDTO.UserSelfResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getBirthDate()
        );
    }
}
