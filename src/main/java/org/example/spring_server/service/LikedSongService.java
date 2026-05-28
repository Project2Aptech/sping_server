package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.LikedSongDTO;
import org.example.spring_server.entity.LikedSong;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.User;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.repository.LikedSongRepository;
import org.example.spring_server.repository.SongRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikedSongService {

    private final LikedSongRepository likedSongRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<LikedSongDTO.LikedSongResponse> findByUser(Integer userId, Pageable pageable) {
        return likedSongRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Integer userId, Integer songId) {
        return likedSongRepository.existsByUserIdAndSongId(userId, songId);
    }

    public void like(Integer userId, Integer songId) {
        if (likedSongRepository.existsByUserIdAndSongId(userId, songId))
            throw new DuplicateResourceException("Song already liked");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        LikedSong likedSong = new LikedSong();
        likedSong.setUser(user);
        likedSong.setSong(song);
        likedSongRepository.save(likedSong);
    }

    public void unlike(Integer userId, Integer songId) {
        if (!likedSongRepository.existsByUserIdAndSongId(userId, songId))
            throw new ResourceNotFoundException("Song not liked");
        likedSongRepository.deleteByUserIdAndSongId(userId, songId);
    }

    private LikedSongDTO.LikedSongResponse toResponse(LikedSong ls) {
        return new LikedSongDTO.LikedSongResponse(
                ls.getSong().getId(),
                ls.getSong().getTitle(),
                ls.getSong().getCoverUrl(),
                ls.getSong().getArtist().getId(),
                ls.getSong().getArtist().getDisplayName(),
                ls.getLikedAt()
        );
    }
}