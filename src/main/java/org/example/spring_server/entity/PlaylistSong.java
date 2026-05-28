package org.example.spring_server.entity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
@Entity
@Table(name = "playlist_songs")
@Getter @Setter @NoArgsConstructor
public class PlaylistSong {

    @EmbeddedId
    private PlaylistSongId id = new PlaylistSongId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playlistId")
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("songId")
    @JoinColumn(name = "song_id")
    private Song song;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Embeddable
    @Getter @Setter @NoArgsConstructor
    @EqualsAndHashCode
    public static class PlaylistSongId implements Serializable {
        @Column(name = "playlist_id")
        private Integer playlistId;

        @Column(name = "song_id")
        private Integer songId;
    }
}