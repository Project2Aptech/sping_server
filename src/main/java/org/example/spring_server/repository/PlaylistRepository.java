package org.example.spring_server.repository;

import org.example.spring_server.entity.Playlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {
    Page<Playlist> findByUserId(Integer userId, Pageable pageable);
    Page<Playlist> findByIsPublicTrue(Pageable pageable);
}