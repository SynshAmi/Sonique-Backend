package com.synshami.sonique.entity;

import com.synshami.sonique.enums.TimeWindow;
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

    // ==========================
    // Lifetime Statistics
    // ==========================

    private Integer totalTracks;
    private Integer uniqueTracks;
    private Integer uniqueArtists;
    private Double totalListeningHours;

    // ==========================
    // Current Personality
    // Listening Style
    // ==========================

    private Double explorationScore;
    private Double artistDiversityScore;
    private Double averageTrackAge;

    @Enumerated(EnumType.STRING)
    private TimeWindow dominantTimeWindow;

    // ==========================
    // Current Personality
    // Taste
    // ==========================

    private String topArtistName;
    private String topSongName;
    private String topGenres;
    private String topMusicalTraits;
    private String topVocalCharacteristic;

    private LocalDateTime lastUpdated;
}