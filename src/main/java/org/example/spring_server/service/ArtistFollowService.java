package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.ArtistFollowDTO;
import org.example.spring_server.entity.ArtistFollow;
import org.example.spring_server.entity.User;
import org.example.spring_server.exception.DuplicateResourceException;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.repository.ArtistFollowRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArtistFollowService {

    private final ArtistFollowRepository artistFollowRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ArtistFollowDTO.ArtistFollowResponse> findFollowing(Integer userId, Pageable pageable) {
        return artistFollowRepository.findByUserId(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean isFollowing(Integer userId, Integer artistId) {
        return artistFollowRepository.existsByUserIdAndArtistId(userId, artistId);
    }

    @Transactional(readOnly = true)
    public long getFollowerCount(Integer artistId) {
        return artistFollowRepository.countByArtistId(artistId);
    }

    public void follow(Integer userId, Integer artistId) {
        if (userId.equals(artistId))
            throw new IllegalArgumentException("Cannot follow yourself");

        if (artistFollowRepository.existsByUserIdAndArtistId(userId, artistId))
            throw new DuplicateResourceException("Already following this artist");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        User artist = userRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        ArtistFollow follow = new ArtistFollow();
        follow.setUser(user);
        follow.setArtist(artist);
        artistFollowRepository.save(follow);
    }

    public void unfollow(Integer userId, Integer artistId) {
        if (!artistFollowRepository.existsByUserIdAndArtistId(userId, artistId))
            throw new ResourceNotFoundException("Not following this artist");
        artistFollowRepository.deleteByUserIdAndArtistId(userId, artistId);
    }

    @Transactional(readOnly = true)
    public Page<ArtistFollowDTO.FollowerResponse> findFollowers(Integer artistId, Pageable pageable) {
        return artistFollowRepository.findByArtistId(artistId, pageable)
                .map(this::toFollowerResponse);
    }

    private ArtistFollowDTO.FollowerResponse toFollowerResponse(ArtistFollow af) {
        return new ArtistFollowDTO.FollowerResponse(
                af.getUser().getId(),
                af.getUser().getDisplayName(),
                af.getUser().getAvatarUrl(),
                af.getFollowedAt()
        );
    }

    private ArtistFollowDTO.ArtistFollowResponse toResponse(ArtistFollow af) {
        return new ArtistFollowDTO.ArtistFollowResponse(
                af.getArtist().getId(),
                af.getArtist().getDisplayName(),
                af.getArtist().getAvatarUrl(),
                af.getFollowedAt()
        );
    }
}