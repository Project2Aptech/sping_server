package org.example.spring_server.entity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
@Entity
@Table(name = "artist_follows")
@Getter @Setter @NoArgsConstructor
public class ArtistFollow {

    @EmbeddedId
    private ArtistFollowId id = new ArtistFollowId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("artistId")
    @JoinColumn(name = "artist_id")
    private User artist;

    @Column(name = "followed_at", nullable = false)
    private LocalDateTime followedAt = LocalDateTime.now();

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    @EqualsAndHashCode
    public static class ArtistFollowId implements Serializable {
        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "artist_id")
        private Integer artistId;
    }
}