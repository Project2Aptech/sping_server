package org.example.spring_server.repository;

import org.example.spring_server.entity.User;
import org.example.spring_server.enums.enumeration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByResetToken(String resetToken);
    Page<User> findByRole(enumeration.UserRole role, Pageable pageable);
    Page<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(
            String username, String displayName, Pageable pageable);
}