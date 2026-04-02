package com.synshami.sonique.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    private Long userId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Personality Vector
    private Double eraScore;
    private Double energyScore;
    private Double explorationScore;
    private Double genreDiversityScore;
    private Double timeBiasScore;

    // Readable identity
    private String dominantGenre;
    private String dominantTimeWindow;
    private String listenerType;

    // Stats
    private String topArtistName;
    private String topSongName;
    private String mostUniqueRecentSongName;
    private Integer mostUniqueRecentSongPopularity;
    private Integer totalTracksAnalyzed;

    private LocalDateTime lastUpdated;
}