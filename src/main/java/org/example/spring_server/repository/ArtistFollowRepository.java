package org.example.spring_server.repository;

import org.example.spring_server.entity.ArtistFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistFollowRepository extends JpaRepository<ArtistFollow, ArtistFollow.ArtistFollowId> {
    boolean existsByUserIdAndArtistId(Integer userId, Integer artistId);
    void deleteByUserIdAndArtistId(Integer userId, Integer artistId);
    Page<ArtistFollow> findByUserId(Integer userId, Pageable pageable);
    long countByArtistId(Integer artistId);
    Page<ArtistFollow> findByArtistId(Integer artistId, Pageable pageable);
}