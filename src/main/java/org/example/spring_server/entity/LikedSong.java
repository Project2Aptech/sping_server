package org.example.spring_server.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "liked_songs")
@Getter @Setter @NoArgsConstructor
public class LikedSong {

    @EmbeddedId
    private LikedSongId id = new LikedSongId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "liked_at", nullable = false)
    private LocalDateTime likedAt = LocalDateTime.now();

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    @EqualsAndHashCode
    public static class LikedSongId implements Serializable {
        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "song_id")
        private Integer songId;
    }
}