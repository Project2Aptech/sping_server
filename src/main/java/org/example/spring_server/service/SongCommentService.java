package org.example.spring_server.service;

import lombok.RequiredArgsConstructor;
import org.example.spring_server.dto.SongCommentDTO;
import org.example.spring_server.entity.Song;
import org.example.spring_server.entity.SongComment;
import org.example.spring_server.entity.User;
import org.example.spring_server.exception.ResourceNotFoundException;
import org.example.spring_server.repository.SongCommentRepository;
import org.example.spring_server.repository.SongRepository;
import org.example.spring_server.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SongCommentService {

    private final SongCommentRepository songCommentRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SongCommentDTO.CommentResponse> findBySong(Integer songId, Pageable pageable) {
        return songCommentRepository
                .findBySongIdAndParentCommentIsNullAndIsDeletedFalse(songId, pageable)
                .map(this::toResponse);
    }

    public SongCommentDTO.CommentResponse create(
            Integer userId, Integer songId, SongCommentDTO.CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new ResourceNotFoundException("Song not found: " + songId));

        SongComment comment = new SongComment();
        comment.setUser(user);
        comment.setSong(song);
        comment.setContent(request.content());

        if (request.parentCommentId() != null) {
            SongComment parent = songCommentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
            comment.setParentComment(parent);
        }

        return toResponse(songCommentRepository.save(comment));
    }

    public void delete(Integer userId, Long commentId) {
        SongComment comment = songCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found: " + commentId));

        if (!comment.getUser().getId().equals(userId))
            throw new org.springframework.security.access.AccessDeniedException("Not your comment");

        comment.setDeleted(true);
        songCommentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public Page<SongCommentDTO.CommentResponse> findReplies(Long commentId, Pageable pageable) {
        if (!songCommentRepository.existsById(commentId))
            throw new ResourceNotFoundException("Comment not found: " + commentId);
        return songCommentRepository.findByParentCommentIdAndIsDeletedFalse(commentId, pageable)
                .map(this::toResponse);
    }

    private SongCommentDTO.CommentResponse toResponse(SongComment c) {
        return new SongCommentDTO.CommentResponse(
                c.getId(),
                c.getSong().getId(),
                c.getUser().getId(),
                c.getUser().getUsername(),
                c.getParentComment() != null ? c.getParentComment().getId() : null,
                c.getContent(),
                c.isDeleted(),
                c.getCreatedAt()
        );
    }
}