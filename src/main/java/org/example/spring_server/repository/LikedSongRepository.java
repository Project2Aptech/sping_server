package org.example.spring_server.repository;

import org.example.spring_server.entity.LikedSong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikedSongRepository extends JpaRepository<LikedSong, LikedSong.LikedSongId> {
    boolean existsByUserIdAndSongId(Integer userId, Integer songId);
    void deleteByUserIdAndSongId(Integer userId, Integer songId);
    Page<LikedSong> findByUserId(Integer userId, Pageable pageable);
}