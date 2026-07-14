package com.synshami.sonique.service.profile;

import com.synshami.sonique.dto.profile.UserProfileResponse;
import com.synshami.sonique.entity.UserProfile;
import com.synshami.sonique.exception.ResourceNotFoundException;
import com.synshami.sonique.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository userProfileRepository;

    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        return UserProfileResponse.builder()
                .username(profile.getUser().getUsername())
                .displayName(profile.getUser().getDisplayName())

                .totalTracks(profile.getTotalTracks())
                .uniqueTracks(profile.getUniqueTracks())
                .uniqueArtists(profile.getUniqueArtists())
                .totalListeningHours(profile.getTotalListeningHours())

                .explorationScore(profile.getExplorationScore())
                .artistDiversityScore(profile.getArtistDiversityScore())
                .averageTrackAge(profile.getAverageTrackAge())
                .dominantTimeWindow(profile.getDominantTimeWindow())

                .topArtistName(profile.getTopArtistName())
                .topSongName(profile.getTopSongName())
                .topGenres(profile.getTopGenres())
                .topMusicalTraits(profile.getTopMusicalTraits())
                .topVocalCharacteristic(profile.getTopVocalCharacteristic())

                .lastUpdated(profile.getLastUpdated())
                .build();
    }
}
