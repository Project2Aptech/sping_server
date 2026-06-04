package org.example.spring_server.service.auth;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.UserDTO;
import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.repository.UserRepository;
import org.example.spring_server.service.EmailService;
import org.example.spring_server.service.auth.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private  final EmailService emailService;

    public UserDTO.AuthResponse register(UserDTO.RegisterRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new DuplicateResourceException("Email already in use");
        if (userRepository.existsByUsername(request.username()))
            throw new DuplicateResourceException("Username already taken");

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setBirthDate(request.birthDate());
        user.setRole(enumeration.UserRole.USER);
        user.setAccountType(enumeration.AccountType.NORMAL);

        userRepository.save(user);

        String token = jwtUtils.generateToken(user.getEmail());
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getDisplayName());

        return new UserDTO.AuthResponse(token, toSummary(user));
    }

    public UserDTO.AuthResponse login(UserDTO.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Wrong username or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Wrong username or password"));

        String token = jwtUtils.generateToken(user.getEmail());
        return new UserDTO.AuthResponse(token, toSummary(user));
    }

    private UserDTO.UserSummary toSummary(User user) {
        return new UserDTO.UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getBirthDate(),
                user.getRole(),
                user.getAccountType()
        );
    }
}