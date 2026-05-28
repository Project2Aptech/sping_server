package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.HistoryDTO;
import org.example.spring_server.entity.History;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.User;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.repository.HistoryRepository;
import org.example.spring_server.repository.SongRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class HistoryService {

    private final HistoryRepository playHistoryRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<HistoryDTO.HistoryResponse> findByUser(Integer userId, Pageable pageable) {
        return playHistoryRepository.findByUserIdOrderByPlayedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public HistoryDTO.HistoryResponse record(
            Integer userId, Integer songId, HistoryDTO.HistoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        History history = new History();
        history.setUser(user);
        history.setSong(song);
        history.setSecondsPlayed(request.secondsPlayed() != null ? request.secondsPlayed() : 0);
        history.setDeviceType(request.deviceType());

        return toResponse(playHistoryRepository.save(history));
    }

    public void clearHistory(Integer userId) {
        playHistoryRepository.deleteByUserId(userId);
    }

    private HistoryDTO.HistoryResponse toResponse(History h) {
        return new HistoryDTO.HistoryResponse(
                h.getId(),
                h.getSong().getId(),
                h.getSong().getTitle(),
                h.getSong().getCoverUrl(),
                h.getSecondsPlayed(),
                h.getDeviceType(),
                h.getPlayedAt()
        );
    }
}