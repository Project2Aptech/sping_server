package org.example.spring_server.repository;

import org.example.spring_server.entity.PlaylistFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistFollowRepository extends JpaRepository<PlaylistFollow, PlaylistFollow.PlaylistFollowId> {
    boolean existsByUserIdAndPlaylistId(Integer userId, Integer playlistId);
    void deleteByUserIdAndPlaylistId(Integer userId, Integer playlistId);
    Page<PlaylistFollow> findByUserId(Integer userId, Pageable pageable);
}