package com.synshami.sonique.dto.profile;

import com.synshami.sonique.enums.TimeWindow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    // User
    private String username;
    private String displayName;

    // Lifetime Statistics
    private Integer totalTracks;
    private Integer uniqueTracks;
    private Integer uniqueArtists;
    private Double totalListeningHours;

    // Current Personality - Listening Style
    private Double explorationScore;
    private Double artistDiversityScore;
    private Double averageTrackAge;
    private TimeWindow dominantTimeWindow;

    // Current Personality - Taste
    private String topArtistName;
    private String topSongName;
    private String topGenres;
    private String topMusicalTraits;
    private String topVocalCharacteristic;

    // Metadata
    private LocalDateTime lastUpdated;
}