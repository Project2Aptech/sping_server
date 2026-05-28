package org.example.spring_server.repository;

import org.example.spring_server.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre, Integer> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    Optional<Genre> findBySlug(String slug);
}