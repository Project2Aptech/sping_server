package org.example.spring_server.entity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
@Entity
@Table(name = "playlist_follows")
@Getter @Setter @NoArgsConstructor
public class PlaylistFollow {

    @EmbeddedId
    private PlaylistFollowId id = new PlaylistFollowId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt = LocalDateTime.now();

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    @EqualsAndHashCode
    public static class PlaylistFollowId implements Serializable {
        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "playlist_id")
        private Integer playlistId;
    }
}