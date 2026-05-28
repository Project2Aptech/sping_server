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
    public UserDTO.UserDetailResponse findById(Integer id) {
        return userRepository.findById(id)
                .map(this::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserDTO.UserDetailResponse update(Integer id, UserDTO.UpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (request.displayName() != null) user.setDisplayName(request.displayName());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.birthDate() != null) user.setBirthDate(request.birthDate());

        return toDetailResponse(userRepository.save(user));
    }

    public UserDTO.UserDetailResponse updateRole(Integer id, UserDTO.AdminUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (request.role() != null) user.setRole(request.role());
        if (request.accountType() != null) user.setAccountType(request.accountType());

        return toDetailResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public Page<UserDTO.UserDetailResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toDetailResponse);
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

    public void deactivate(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public UserDTO.UserDetailResponse uploadAvatar(Integer userId, MultipartFile file)
            throws IOException, java.io.IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        String avatarUrl = cloudinaryService.uploadImage(file);
        user.setAvatarUrl(avatarUrl);

        return toDetailResponse(userRepository.save(user));
    }
}
