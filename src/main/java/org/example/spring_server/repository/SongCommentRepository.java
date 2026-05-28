package org.example.spring_server.repository;

import org.example.spring_server.entity.SongComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongCommentRepository extends JpaRepository<SongComment, Long> {
    Page<SongComment> findBySongIdAndIsDeletedFalse(Integer songId, Pageable pageable);
    Page<SongComment> findBySongIdAndParentCommentIsNullAndIsDeletedFalse(Integer songId, Pageable pageable);
    Page<SongComment> findByParentCommentIdAndIsDeletedFalse(Long parentCommentId, Pageable pageable);
}