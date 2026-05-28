package org.example.spring_server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.spring_server.enums.enumeration;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "songs")
@Getter @Setter @NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    private User artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds = 0;

    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "play_count", nullable = false)
    private Long playCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private enumeration.SongStatus status = enumeration.SongStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_account_type", nullable = false)
    private enumeration.AccountType requiredAccountType = enumeration.AccountType.NORMAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }
}
