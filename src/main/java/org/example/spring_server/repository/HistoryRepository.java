package org.example.spring_server.repository;

import org.example.spring_server.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
    Page<History> findByUserIdOrderByPlayedAtDesc(Integer userId, Pageable pageable);
    void deleteByUserId(Integer userId);
}